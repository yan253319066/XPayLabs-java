package com.yan.blockchain.pay.utils;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//import cn.hutool.json.JSONUtil;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.*;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//
//import java.io.IOException;
//import java.math.BigInteger;
//import java.nio.charset.StandardCharsets;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.TreeMap;
//
//@Slf4j
//public class ItrxUtil {
//	private static final String API_KEY = "0982A11B18C14EFBB7A06A945E11192E";
//	private static final String API_SECRET = "AABB74262A1CAB3D4ED73A4671417B9EF40F080E762C3865F7D8018A867E2C96";
//
//	public static void main(String[] args) throws Exception {
////    	Integer i = leaseEnergyItrx("TT1vbxKm4s2xJPpvNshKNZeRTKGkbNH9rB", 32000L);
////    	System.out.println(i);
//		double p = price("1H", 32000L);
//		System.out.println(p);
//	}
//
//	public static double price(String period, Long energyAmount) {
//		OkHttpClient client = new OkHttpClient();
//		String url = "https://itrx.io/api/v1/frontend/order/price?period=" + period + "&energy_amount=" + energyAmount;
//		Request request = new Request.Builder().url(url).addHeader("API-KEY", API_KEY).build();
//		try {
//			Response response = client.newCall(request).execute();
//			String res = response.body().string();
//			log.info(res);
//			BigInteger sun = JSONUtil.parseObj(res).getBigInteger("total_price");
//			return Utils.fromSmallestUnit(sun, 6).doubleValue();
//		} catch (IOException e) {
//			throw new RuntimeException("", e);
//		}
//	}
//
//	public static Integer leaseEnergyItrx(String address, Long energy)
//			throws InvalidKeyException, NoSuchAlgorithmException, IOException {
//		OkHttpClient client = new OkHttpClient().newBuilder().build();
//		MediaType mediaType = MediaType.parse("application/json");
//		String timestamp = String.valueOf(Instant.now().getEpochSecond());
//
//		Map<String, Object> data = new HashMap<>();
//		data.put("energy_amount", energy);
//		data.put("period", "1H");
//		data.put("receive_address", address);
////        data.put("callback_url", "http://{mydomain}/callback");
////        data.put("out_trade_no", IdUtil.fastUUID());
//
//		// Sorting the keys
//		TreeMap<String, Object> sortedData = new TreeMap<>(data);
//		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
//		String json_data = gson.toJson(sortedData);
//
//		String message = timestamp + "&" + json_data;
//		String signature = encodeHmacSHA256(message, API_SECRET);
//
//		RequestBody body = RequestBody.create(json_data, mediaType);
//		Request request = new Request.Builder().url("https://itrx.io/api/v1/frontend/order").method("POST", body)
//				.addHeader("API-KEY", API_KEY).addHeader("TIMESTAMP", timestamp).addHeader("SIGNATURE", signature)
//				.addHeader("Content-Type", "application/json").build();
//		Response response = client.newCall(request).execute();
//		String res = response.body().string();
//		log.info(res);
//		return JSONUtil.parseObj(res).getInt("errno", -1);
//	}
//
//	private static String encodeHmacSHA256(String data, String key)
//			throws NoSuchAlgorithmException, InvalidKeyException {
//		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
//		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//		sha256_HMAC.init(secret_key);
//		byte[] bytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
//		StringBuilder hash = new StringBuilder();
//		for (byte b : bytes) {
//			String hex = Integer.toHexString(0xff & b);
//			if (hex.length() == 1)
//				hash.append('0');
//			hash.append(hex);
//		}
//		return hash.toString();
//	}
//}
