package com.yan.xpay.utils;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.yan.xpay.constant.RedisConstant;
import com.yan.xpay.domain.CryptoPrice;
import com.yan.xpay.enums.Chain;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Hex;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

@Slf4j
public class FeeUtils {
	/**
	 *  充值，代收
	 *  获取平台手续费 (如果rate不为0，最低收取0.0001)
	 * @param amount
	 * @param rate
	 * @return
	 */
	public static BigDecimal getPlatformFee(BigDecimal amount, BigDecimal rate) {
		if(rate.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
		BigDecimal fee = rate.multiply(amount).divide(new BigDecimal("100"), 6, RoundingMode.DOWN);
		return fee.compareTo(new BigDecimal("0.0001")) > 0 ? fee : new BigDecimal("0.0001");
	}

	/**
	 * 付款，提现
	 * @param baseCoin isNativeToken 是否链上主币
	 * @param chain
	 * @param symbol
	 * @param amount
	 * @param rate
	 * @param toAddress
	 * @return
	 */
	public static BigDecimal getPlatformFee(Boolean baseCoin, Chain chain, String symbol, BigDecimal amount, BigDecimal rate, String toAddress) {
		if(chain == Chain.TRON || chain == Chain.TRON_TEST) {
			return getTronPlatformFee(baseCoin, chain, symbol, amount, rate, toAddress);
		}else {
			return getPlatformFee(baseCoin, chain, symbol, amount, rate);
		}
	}

	/**
	 * 付款，提现 (标准通用系列)
	 * 获取平台手续费 (包括GAS费用)
	 * @param isNativeToken 是否链上主币
	 * @param chain
	 * @param symbol
	 * @param amount
	 * @param rate
	 * @return
	 */
	public static BigDecimal getPlatformFee(Boolean isNativeToken, Chain chain, String symbol, BigDecimal amount, BigDecimal rate) {
		BigDecimal fee = BigDecimal.ZERO;
		if(rate.compareTo(BigDecimal.ZERO) > 0)
			fee = getPlatformFee(amount, rate);
		BigDecimal totalFee = BigDecimal.ZERO;
		BigDecimal gas = RedisUtils.getCacheMapValue(RedisConstant.GAS_KEY, chain.name());
		if(gas == null || gas.compareTo(BigDecimal.ZERO) <= 0) {
			log.info("Gas not found {} {}", chain, symbol);
			throw new ServiceException("Gas not found");
		}
		if(isNativeToken) {
			totalFee = fee.add(gas);
			log.info("平台手续费 {} {} fee {} gas {} totalFee {}", chain, symbol, fee, gas, totalFee);
		}else {
			CryptoPrice cryptoPrice = RedisUtils.getCacheMapValue(RedisConstant.CRYPTO_PRICE_KEY, symbol.toLowerCase());
			if(cryptoPrice == null || cryptoPrice.getCurrentPrice().compareTo(BigDecimal.ZERO) <= 0){
				log.info("Crypto price not found {}", symbol);
				throw new ServiceException("Crypto price not found");
			}
			totalFee = cryptoPrice.getCurrentPrice().multiply(gas).add(fee);
			log.info("平台手续费 {} {} fee {} gas {} totalFee {}", chain, symbol, fee, cryptoPrice.getCurrentPrice().multiply(gas), totalFee);
		}
		return totalFee.setScale(6, RoundingMode.HALF_UP);
	}

	/**
	 * 付款，提现
	 * tron计算平台手续费（包括GAS费）
	 * @param isNativeToken 是否链上主币
	 * @param chain
	 * @param symbol
	 * @param amount
	 * @param rate
	 * @param toAddress
	 * @return
	 */
	public static BigDecimal getTronPlatformFee(Boolean isNativeToken, Chain chain, String symbol, BigDecimal amount, BigDecimal rate, String toAddress) {
		BigDecimal fee = BigDecimal.ZERO;
		if(rate.compareTo(BigDecimal.ZERO) > 0)
			fee = getPlatformFee(amount, rate);
		BigDecimal totalFee = BigDecimal.ZERO;
		BigDecimal gas;
		if(isNativeToken) {
			gas = calculateGas(toAddress, false);
			totalFee = fee.add(gas);
			log.info("平台手续费 {} {} fee {} gas {} totalFee {}", chain, symbol, fee, gas, totalFee);
		}else {//是USDT
			gas = calculateGas(toAddress, true);
			CryptoPrice cryptoPrice = RedisUtils.getCacheMapValue(RedisConstant.CRYPTO_PRICE_KEY, "trx");
			if(cryptoPrice == null || cryptoPrice.getCurrentPrice().compareTo(BigDecimal.ZERO) <= 0){
				log.info("Crypto price not found {}", symbol);
				throw new ServiceException("Crypto price not found");
			}
			totalFee = cryptoPrice.getCurrentPrice().multiply(gas).add(fee);
			log.info("平台手续费 {} {} fee {} gas {} totalFee {}", chain, symbol, fee, cryptoPrice.getCurrentPrice().multiply(gas), totalFee);
		}

		return totalFee.setScale(6, RoundingMode.HALF_UP);
	}

	/**
	 * 估算能量
	 * @param toAddress
	 * @return
	 */
	public static Long estimateEnergy(String toAddress) {
		String toAddressHex;
		try {
			toAddressHex = Hex.toHexString(base58ToBytes(toAddress)); // 41开头
		} catch (RuntimeException e) {
			throw new ServiceException("Address conversion error");
		}
		System.out.println("estimateEnergy toAddressHex "+ toAddressHex);
		// ---------------------
		// Step 1. 去掉 0x 前缀并补齐为 32 字节
		// ---------------------
		String addressNoPrefix = toAddressHex.replace("0x", "");
		String addressPadded = String.format("%064x", new BigInteger(addressNoPrefix, 16));
		String amountPadded = String.format("%064x", new BigInteger("50"));

		// ---------------------
		// Step 2. 拼接 parameter
		// ---------------------
		String parameter = addressPadded + amountPadded;

		//		Function transfer = new Function("transfer", Arrays.asList(new Address(toAddress), new Uint256(amount)), Arrays.asList(new TypeReference<Bool>() {
		//		}));
		//		log.info("parameter {}", parameter);
		//		parameter = FunctionEncoder.encode(transfer).substring(10);
		//		log.info(parameter);
		//		log.info(DefaultFunctionEncoder.encode(transfer));
		String body = "{\n" +
			"  \"owner_address\": \"TCyFHUZg2Y373uCYyx6QVbQJc4LZeaDre1\",\n" +
			"  \"contract_address\": \"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\",\n" +
			"  \"function_selector\": \"transfer(address,uint256)\",\n" +
			"  \"parameter\": \""+parameter+"\",\n" +
			"  \"visible\": true\n" +
			"}";

		HttpResponse response = cn.hutool.http.HttpRequest.post("https://api.trongrid.io/walletsolidity/triggerconstantcontract")
			.contentType("application/json")
			.body(body)
			.execute();
		//		log.info("{}", response.body());
		return JSONUtil.parseObj(response.body()).getLong("energy_used");
	}

	public static BigDecimal calculateGas(String toAddress, boolean isContract){
		// 能量价格
		HttpResponse response = cn.hutool.http.HttpRequest.post("https://api.trongrid.io/wallet/getenergyprices")
			.contentType("application/json")
			.execute();
		//		log.info("{}", response.body());
		String[] allPriceEntries = JSONUtil.parseObj(response.body()).getStr("prices").split(",");
		String currentPriceEntry = allPriceEntries[allPriceEntries.length - 1];
		String[] priceParts = currentPriceEntry.split(":");
		//		log.info("priceParts {}", (Object) priceParts);
		BigDecimal energyPrice = new BigDecimal(priceParts[1]);
		//		log.info("energyPrice {}", energyPrice);
		// 带宽价格（固定值：1 能量 = 1000 SUN）
		BigDecimal currentBandwidthPrice = new BigDecimal("1000");

		// 计算手续费
		if(isContract) {
			// USDT 转账：消耗带宽 + 能量
			int usdtBandwidth = 345;
			int usdtEnergy = estimateEnergy(toAddress).intValue(); // 您提供的正确值

			BigDecimal usdtBandwidthCost = currentBandwidthPrice
				.multiply(new BigDecimal(usdtBandwidth))
				.divide(new BigDecimal("1000000"), 6, RoundingMode.HALF_UP);
			BigDecimal usdtEnergyCost = energyPrice
				.multiply(new BigDecimal(usdtEnergy))
				.divide(new BigDecimal("1000000"), 6, RoundingMode.HALF_UP);

			BigDecimal totalUsdtFee = usdtBandwidthCost.add(usdtEnergyCost);
			return totalUsdtFee;
		}else {
			// TRX 转账：只消耗带宽
			BigDecimal trxFee = currentBandwidthPrice
				.multiply(new BigDecimal("265"))
				.divide(new BigDecimal("1000000"), 6, RoundingMode.HALF_UP);
			return trxFee;
		}
	}

	public static byte[] base58ToBytes(String s) {
		byte[] concat = base58ToRawBytes(s);
		byte[] data = Arrays.copyOf(concat, concat.length - 4);
		byte[] hash = Arrays.copyOfRange(concat, concat.length - 4, concat.length);
		SHA256.Digest digest = new SHA256.Digest();
		digest.update(data);
		byte[] hash0 = digest.digest();
		digest.reset();
		digest.update(hash0);
		byte[] rehash = Arrays.copyOf(digest.digest(), 4);
		if (!Arrays.equals(rehash, hash)) {
			throw new IllegalArgumentException("Checksum mismatch");
		} else {
			return data;
		}
	}
	private static final BigInteger ALPHABET_SIZE = BigInteger.valueOf((long)"123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".length());
	static byte[] base58ToRawBytes(String s) {
		BigInteger num = BigInteger.ZERO;

		for(int i = 0; i < s.length(); ++i) {
			num = num.multiply(ALPHABET_SIZE);
			int digit = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".indexOf(s.charAt(i));
			if (digit == -1) {
				throw new IllegalArgumentException("Invalid character for Base58Check");
			}

			num = num.add(BigInteger.valueOf((long)digit));
		}

		byte[] b = num.toByteArray();
		if (b[0] == 0) {
			b = Arrays.copyOfRange(b, 1, b.length);
		}

		try {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();

			for(int i = 0; i < s.length() && s.charAt(i) == "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".charAt(0); ++i) {
				buf.write(0);
			}

			buf.write(b);
			return buf.toByteArray();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
}
