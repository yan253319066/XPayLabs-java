package com.yan.blockchain.pay.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.yan.blockchain.pay.config.FiatCurrencyConfig;
import com.yan.blockchain.pay.constant.JdpayConstant;
import com.yan.blockchain.pay.model.FiatCurrencyIn;
import com.yan.blockchain.pay.model.FiatCurrencyOut;
import com.yan.blockchain.pay.model.FiatCurrencyQueryIn;
import com.yan.blockchain.pay.model.FiatCurrencyQueryOut;
import com.yan.blockchain.pay.req.FiatCurrencyInReq;
import com.yan.blockchain.pay.req.FiatCurrencyOutReq;
import com.yan.blockchain.pay.req.FiatCurrencyQueryInReq;
import com.yan.blockchain.pay.req.FiatCurrencyQueryOutReq;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdpayFiatCurrencyUtils {

	public static String jdpayFiatCurrencyIn(FiatCurrencyInReq req, FiatCurrencyConfig.Jdpay jdpay) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,  false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		FiatCurrencyIn fiatCurrencyIn = mapper.convertValue(req, FiatCurrencyIn.class);
		fiatCurrencyIn.setMer_no(jdpay.getMerchantNo());
		fiatCurrencyIn.setName(JdpayConstant.name);
		fiatCurrencyIn.setEmail(JdpayConstant.email);
		if(StrUtil.isBlank(req.getPhone()))
			fiatCurrencyIn.setPhone(JdpayConstant.phone);
		fiatCurrencyIn.setNotify_url(jdpay.getNotifyInUrl());
		fiatCurrencyIn.setSign(signWithRsa(fiatCurrencyIn, jdpay.getDecodePrivateKey()));
		log.info("jdpayFiatCurrencyIn params:{}", JSONUtil.toJsonStr(fiatCurrencyIn));
		String response = HttpRequest.post(JdpayConstant.fiatCurrencyIn).contentType("application/json").body(JSONUtil.toJsonStr(fiatCurrencyIn)).execute().body();
		log.info("jdpayFiatCurrencyIn response:{}", response);
		return response;
	}
	public static String jdpayFiatCurrencyInV2(FiatCurrencyIn fiatCurrencyIn, FiatCurrencyConfig.Jdpay jdpay) {
		fiatCurrencyIn.setMer_no(jdpay.getMerchantNo());
		fiatCurrencyIn.setNotify_url(jdpay.getNotifyInUrl());
		fiatCurrencyIn.setSign(signWithRsa(fiatCurrencyIn, jdpay.getDecodePrivateKey()));
		log.info("jdpayFiatCurrencyInV2 params:{}", JSONUtil.toJsonStr(fiatCurrencyIn));
		String response = HttpRequest.post(JdpayConstant.fiatCurrencyIn).contentType("application/json").body(JSONUtil.toJsonStr(fiatCurrencyIn)).execute().body();
		log.info("jdpayFiatCurrencyInV2 response:{}", response);
		return response;
	}
	public static String jdpayFiatCurrencyOut(FiatCurrencyOutReq req, FiatCurrencyConfig.Jdpay jdpay) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,  false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		FiatCurrencyOut fiatCurrencyOut = mapper.convertValue(req, FiatCurrencyOut.class);
		fiatCurrencyOut.setMer_no(jdpay.getMerchantNo());
		fiatCurrencyOut.setEmail(JdpayConstant.email);
		if(StrUtil.isBlank(req.getPhone()))
			fiatCurrencyOut.setPhone(JdpayConstant.phone);
		fiatCurrencyOut.setNotify_url(jdpay.getNotifyOutUrl());
		fiatCurrencyOut.setSign(signWithRsa(fiatCurrencyOut, jdpay.getDecodePrivateKey()));
		log.info("jdpayFiatCurrencyOut params:{}", JSONUtil.toJsonStr(fiatCurrencyOut));
		String response = HttpRequest.post(JdpayConstant.fiatCurrencyOut).contentType("application/json").body(JSONUtil.toJsonStr(fiatCurrencyOut)).execute().body();
		log.info("jdpayFiatCurrencyOut response:{}", response);
		return response;
	}
	public static String jdpayFiatCurrencyOutV2(FiatCurrencyOut fiatCurrencyOut, FiatCurrencyConfig.Jdpay jdpay) {
		fiatCurrencyOut.setMer_no(jdpay.getMerchantNo());
		fiatCurrencyOut.setNotify_url(jdpay.getNotifyInUrl());
		fiatCurrencyOut.setSign(signWithRsa(fiatCurrencyOut, jdpay.getDecodePrivateKey()));
		log.info("jdpayFiatCurrencyOutV2 params:{}", JSONUtil.toJsonStr(fiatCurrencyOut));
		String response = HttpRequest.post(JdpayConstant.fiatCurrencyOut).contentType("application/json").body(JSONUtil.toJsonStr(fiatCurrencyOut)).execute().body();
		log.info("jdpayFiatCurrencyOutV2 response:{}", response);
		return response;
	}

	public static String jdpayQueryIn(FiatCurrencyQueryInReq req, FiatCurrencyConfig.Jdpay jdpay) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,  false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		FiatCurrencyQueryIn fiatCurrencyQueryIn = mapper.convertValue(req, FiatCurrencyQueryIn.class);
		fiatCurrencyQueryIn.setMer_no(jdpay.getMerchantNo());
		fiatCurrencyQueryIn.setSign(signWithRsa(fiatCurrencyQueryIn, jdpay.getDecodePrivateKey()));
		log.info("jdpayQueryIn params:{}", JSONUtil.toJsonStr(fiatCurrencyQueryIn));
		String response = HttpRequest.post(JdpayConstant.fiatCurrencyQueryIn).contentType("application/json").body(JSONUtil.toJsonStr(fiatCurrencyQueryIn)).execute().body();
		log.info("jdpayQueryIn response:{}", response);
		return response;
	}

	public static String jdpayQueryInV2(String orderNo, FiatCurrencyConfig.Jdpay jdpay) {
		FiatCurrencyQueryIn fiatCurrencyQueryIn = new FiatCurrencyQueryIn();
		fiatCurrencyQueryIn.setOrder_no(orderNo);
		fiatCurrencyQueryIn.setMer_no(jdpay.getMerchantNo());
		fiatCurrencyQueryIn.setSign(signWithRsa(fiatCurrencyQueryIn, jdpay.getDecodePrivateKey()));
		String response = HttpRequest.post(JdpayConstant.fiatCurrencyQueryIn).contentType("application/json").body(JSONUtil.toJsonStr(fiatCurrencyQueryIn)).execute().body();
		return response;
	}

	public static String jdpayQueryOut(FiatCurrencyQueryOutReq req, FiatCurrencyConfig.Jdpay jdpay) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,  false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		FiatCurrencyQueryOut fiatCurrencyQueryOut = mapper.convertValue(req, FiatCurrencyQueryOut.class);
		fiatCurrencyQueryOut.setMer_no(jdpay.getMerchantNo());
		fiatCurrencyQueryOut.setSign(signWithRsa(fiatCurrencyQueryOut, jdpay.getDecodePrivateKey()));
		log.info("jdpayQueryOut params:{}", JSONUtil.toJsonStr(fiatCurrencyQueryOut));
		String response = HttpRequest.post(JdpayConstant.fiatCurrencyQueryOut).contentType("application/json").body(JSONUtil.toJsonStr(fiatCurrencyQueryOut)).execute().body();
		log.info("jdpayQueryOut response:{}", response);
		return response;
	}

	public static String jdpayQueryOutV2(String orderNo, FiatCurrencyConfig.Jdpay jdpay) {
		FiatCurrencyQueryOut fiatCurrencyQueryOut = new FiatCurrencyQueryOut();
		fiatCurrencyQueryOut.setOrder_no(orderNo);
		fiatCurrencyQueryOut.setMer_no(jdpay.getMerchantNo());
		fiatCurrencyQueryOut.setSign(signWithRsa(fiatCurrencyQueryOut, jdpay.getDecodePrivateKey()));
		String response = HttpRequest.post(JdpayConstant.fiatCurrencyQueryOut).contentType("application/json").body(JSONUtil.toJsonStr(fiatCurrencyQueryOut)).execute().body();
		return response;
	}

	public static String jdpayMerchantBalance(FiatCurrencyConfig.Jdpay jdpay) {
		JSONObject params = new JSONObject();
		params.set("mer_no", jdpay.getMerchantNo());
		params.set("sign", signWithRsa(params, jdpay.getDecodePrivateKey()));
		log.info("jdpayMerchantBalance params:{}", JSONUtil.toJsonStr(params));
		String response = HttpRequest.post(JdpayConstant.merchantBalance).contentType("application/json").body(params.toString()).execute().body();
		log.info("jdpayMerchantBalance response:{}", response);
		return response;
	}

	public static boolean verifySign(Object param,String verifySign, String publicKey) {
		Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA, null, publicKey);
		String jsonString= JSONUtil.toJsonStr(param);
		JSONObject jsonObject = JSONUtil.parseObj(jsonString);
		jsonObject.remove("sign");
		String data= buildSignData(jsonObject);
		return  sign.verify(data.getBytes(StandardCharsets.UTF_8),base64ToBytes(verifySign));
	}

	public static String signWithRsa(Object param, String privateKey) {
		String jsonString = JSONUtil.toJsonStr(param);
		JSONObject jsonObject = JSONUtil.parseObj(jsonString);
		jsonObject.remove("sign");
		String text= buildSignData(jsonObject);
		Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA, privateKey, null);
		//签名
		byte[] data = text.getBytes(StandardCharsets.UTF_8);
		byte[] signed = sign.sign(data);
		String signedStr = bytesToBase64(signed);
		return signedStr;
	}

	/**
	 * 字节数组转Base64编码
	 *
	 * @param bytes 字节数组
	 * @return Base64编码
	 */
	private static String bytesToBase64(byte[] bytes) {
		byte[] encodedBytes = Base64.getEncoder().encode(bytes);
		return new String(encodedBytes, StandardCharsets.UTF_8);
	}

	/**
	 * Base64编码转字节数组
	 *
	 * @param base64Str Base64编码
	 * @return 字节数组
	 */
	private static byte[] base64ToBytes(String base64Str) {
		byte[] bytes = base64Str.getBytes(StandardCharsets.UTF_8);
		return Base64.getDecoder().decode(bytes);
	}

	private static String buildSignData(Map<String, Object> params) {
		params.remove("sign");
		List<String> keys=params.keySet().stream().collect(Collectors.toList());
		Collections.sort(keys);
		StringBuffer buf=new StringBuffer();
		for (String key : keys) {
			if(params.get(key)==null|| StrUtil.isBlank(params.get(key).toString())){
				continue;
			}
			buf.append("&");
			buf.append(key).append("=");
			buf.append(params.get(key));
		}
		return buf.toString().substring(1);
	}
}
