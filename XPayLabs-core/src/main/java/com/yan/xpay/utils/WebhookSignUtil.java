package com.yan.xpay.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.yan.xpay.exception.SignedException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.common.redis.utils.RedisUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebhookSignUtil {

	private static String ALGORITHM = "HmacSHA256";
	private static long TIME_DIFF_MAX = 30;
	private static final String PREFIX = "yan:signature:";

	public static void main(String[] args) {

		/**
		 * nodejs verify
		 *
		 *const crypto = require('crypto');
		 *
		 * //Verify the signature of incoming webhook data
		 * function verifySignature(secret, data) {
		 *     // Check required fields
		 *     if (!data.timestamp || !data.nonce || !data.sign) {
		 *         throw new Error('Missing required fields');
		 *     }
		 *
		 *     // Validate timestamp
		 *     const TIME_DIFF_MAX = 30; // Maximum allowed time difference in seconds
		 *     const timestamp = parseInt(data.timestamp);
		 *     const currentTime = Math.floor(Date.now() / 1000);
		 *     if (Math.abs(currentTime - timestamp) > TIME_DIFF_MAX) {
		 *         throw new Error('Timestamp expired');
		 *     }
		 *
		 *     // Verify the signature
		 *     const receivedSign = data.sign;
		 *     const calculatedSign = generateSignature(secret, data);
		 *
		 *     return receivedSign === calculatedSign;
		 * }
		 *
		 * //Generate HMAC-SHA256 signature
		 * function generateSignature(secret, data) {
		 *     // Create a copy of data and remove the signature field
		 *     const dataToSign = { ...data };
		 *     delete dataToSign.sign;
		 *
		 *     // Sort keys alphabetically
		 *     const sortedKeys = Object.keys(dataToSign).sort();
		 *
		 *     // Concatenate parameters in key=value format
		 *     let plainText = '';
		 *     for (const key of sortedKeys) {
		 *         plainText += `${key}=${dataToSign[key]}&`;
		 *     }
		 *     plainText = plainText.slice(0, -1); // Remove the last '&'
		 *
		 *     // Calculate HMAC-SHA256 signature
		 *     const hmac = crypto.createHmac('sha256', secret);
		 *     hmac.update(plainText);
		 *     return hmac.digest('hex');
		 * }
		 *
		 *
		 *
		 * function main() {
		 *     const secret = "";
		 *     const testParams = { "sign": "0d1b635654b20b07f5ca95c91d36bd5d96c2b6ab8a229cf1627bbe42838654c0", "timestamp": 1752633386, "nonce": "9e440806de4a4dc4ae6a81a8779f3a01", "chain": "TRON", "symbol": "USDT", "amount": 0.123456, "confirmedNum": 0 }
		 *
		 *     try {
		 *         const isValid = verifySignature(secret, testParams);
		 *         console.log('✅  Signature verification successful');
		 *     } catch (e) {
		 *         console.error('❌  Signature verification failed:', e.message);
		 *     }
		 * }
		 * main()
		 */

	}

	public static boolean verifySign (String secret,  Map<String, Object> params) {
		if(CollUtil.isEmpty(params)) throw new SignedException.NullParam("Signature parameter is empty");

		/**
		 * {
		 "appId": "yan123",
		 "data": "{"name":"yan","userId":"test"}",
		 "nonce": -2028703096,
		 "timestamp": 1597415679
		 }
		 */
//		if(ObjectUtil.isNull(params.get("appId"))) throw new SignedException.NullParam("appId is null");
		if(ObjectUtil.isNull(params.get("timestamp"))) throw new SignedException.NullParam("timestamp is null");
		if(ObjectUtil.isNull(params.get("nonce"))) throw new SignedException.NullParam("nonce is null");
		if(ObjectUtil.isNull(params.get("sign"))) throw new SignedException.NullParam("sign is null");


		long timestamp = Long.valueOf(params.get("timestamp").toString());
		String nonce = params.get("nonce").toString();
		String sign = (String) params.get("sign");
//		String appId = (String) params.get("appId");

		isTimeDiffLarge(timestamp);

		isReplayAttack("", timestamp, nonce, sign);

		String signature = getSignature(secret, params);
		//        System.out.println("signature= "+signature);

		//  If the signatures are inconsistent, throw an exception
		if (!sign.equals(signature))
			throw new SignedException.SignatureError("Signature error "+sign);
		return true;
	}


	/**
	 * If the time difference between the server and the client is too large, throw an exception
	 *
	 * @param timestamp
	 */
	public static void isTimeDiffLarge(long timestamp) {
		long diff = timestamp - Instant.now().getEpochSecond();
		if (Math.abs(diff) > TIME_DIFF_MAX) {
			throw new SignedException.TimestampError("The difference in timestamps " + diff);
		}
	}


	/**
	 * Judge whether it is a replay attack by time stamp and nonce
	 */
	public static void isReplayAttack(String appId, long timestamp, String nonce, String signature) {
		String key = PREFIX + appId + "_" + timestamp + "_" + nonce;
		Object obj = RedisUtils.getCacheObject(key);
		if (obj != null && signature.equals(obj.toString()))
			throw new SignedException.ReplayAttack(appId, timestamp, nonce);
		else
			RedisUtils.setCacheObject(key, signature, Duration.ofSeconds(TIME_DIFF_MAX));
	}


	/**
	 * Signature calculation and verification
	 *
	 * @param appId
	 * @param map
	 */
	public static String getSignature(String appId, Map<String, Object> map) {

		String appSecret = appId;

		map.remove("sign");

		//  Sort the parameters by ascending ASCII
		Map<String, Object> sortedParams = sortMapRecursively(map);

		//  Splice the parameters
		//  e.g. "key1=value1&key2=value2"
		String signatureString = sortedParams.entrySet().stream()
			.map(entry -> formatParameter(entry.getKey(),  entry.getValue()))
			.collect(Collectors.joining("&"));

		//  The plain text is encrypted by algorithm and converted to Base64
		SecretKeySpec secretKeySpec = new SecretKeySpec(appSecret.getBytes(), ALGORITHM);
		Mac mac = null;
		try {
			mac = Mac.getInstance(ALGORITHM);
			mac.init(secretKeySpec);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			throw new SignedException.NoSuchAlgorithmException(e.getMessage());
		} catch (InvalidKeyException e) {
			throw new SignedException.InvalidKeyException(e.getMessage());
		}
		byte[] bytes = mac.doFinal(signatureString.toString().getBytes());
		String result = Convert.toHex(bytes);

		return result;
	}

	private static Map<String, Object> sortMapRecursively(Map<String, Object> map) {
		Map<String, Object> sortedMap = new TreeMap<>();
		map.forEach((key,  value) -> {
			if (value instanceof Map) {
				sortedMap.put(key,  sortMapRecursively((Map<String, Object>) value));
			} else if (value instanceof Collection) {
				List<Object> sortedList = ((Collection<?>) value).stream()
					.map(item -> item instanceof Map ?
						sortMapRecursively((Map<String, Object>) item) : item)
					.collect(Collectors.toList());
				sortedMap.put(key,  sortedList);
			} else {
				sortedMap.put(key,  value);
			}
		});
		return sortedMap;
	}

	private static String formatParameter(String key, Object value) {
		if (value == null) {
			return key + "=";
		}
		return key + "=" + (value instanceof Map ? "{" + formatMap((Map<?, ?>) value) + "}" : value);
	}

	private static String formatMap(Map<?, ?> map) {
		return map.entrySet().stream()
			.filter(e -> e.getValue()  != null)  // 过滤掉值为null的条目
			.sorted(Comparator.comparing(e -> String.valueOf(e.getKey())))  // Sort by key as string
			.map(e -> {
				Object value = e.getValue();
				if (value instanceof Map) {
					return e.getKey() + "={" + formatMap((Map<?, ?>) value) + "}";
				} else if (value instanceof Collection) {
					return e.getKey() + "=[" + formatCollection((Collection<?>) value) + "]";
				} else {
					return e.getKey() + "=" + value;
				}
			})
			.collect(Collectors.joining(","));
	}

	private static String formatCollection(Collection<?> collection) {
		return collection.stream()
			.map(item -> {
				if (item instanceof Map) {
					return "{" + formatMap((Map<?, ?>) item) + "}";
				} else {
					return String.valueOf(item);
				}
			})
			.collect(Collectors.joining(","));
	}

	public static String generateHmacSha256Key() {
		SecureRandom secureRandom = new SecureRandom();
		byte[] key = new byte[48]; // 48字节 → 64字符Base64
		secureRandom.nextBytes(key);
		return Base64.getEncoder().encodeToString(key);
	}
}

