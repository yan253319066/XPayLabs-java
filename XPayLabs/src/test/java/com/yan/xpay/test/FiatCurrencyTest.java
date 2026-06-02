package com.yan.xpay.test;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.blockchain.pay.constant.JdpayConstant;
import com.yan.blockchain.pay.controller.FiatCurrencyController;
import com.yan.blockchain.pay.req.FiatCurrencyInReq;
import com.yan.blockchain.pay.req.FiatCurrencyOutReq;
import com.yan.blockchain.pay.req.FiatCurrencyQueryInReq;
import com.yan.blockchain.pay.req.NotifyInReq;
import com.yan.xpay.utils.WebhookSignUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Map;

@Slf4j
@SpringBootTest
public class FiatCurrencyTest {
	@Autowired
	FiatCurrencyController fiatCurrencyController;

	public static void main(String[] args) {

	}

	private void fiatCurrencyIn() {
		String orderId = IdUtil.fastSimpleUUID();
		FiatCurrencyInReq fiatCurrencyInReq = new FiatCurrencyInReq();
		fiatCurrencyInReq.setOrderNo(orderId);
		fiatCurrencyInReq.setCurrency("INR");
		fiatCurrencyInReq.setAmount("1.12");
		fiatCurrencyInReq.setNotifyUrl("--");
		fiatCurrencyInReq.setPayCode("8041");
		fiatCurrencyInReq.setNonce(IdUtil.fastSimpleUUID());
		fiatCurrencyInReq.setTimestamp(Instant.now().getEpochSecond());
		String token = "";
		String appid = "";
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		fiatCurrencyInReq.setSign(WebhookSignUtil.getSignature(appid, mapper.convertValue(fiatCurrencyInReq, Map.class)));
		String response = HttpRequest.post("http://localhost:8077/v1/order/fiatCurrencyIn").header("X-API-TOKEN",token).contentType("application/json").body(JSONUtil.toJsonStr(fiatCurrencyInReq)).execute().body();
		log.info(response);
	}

//	@Test
	public void fiatCurrencyTest(){
//		log.info(fiatCurrencyService.fiatCurrencyBalance());

//		String orderId = IdUtil.fastSimpleUUID();
//		FiatCurrencyInReq fiatCurrencyInReq = new FiatCurrencyInReq();
//		fiatCurrencyInReq.setOrderNo(orderId);
//		fiatCurrencyInReq.setCurrency("INR");
//		fiatCurrencyInReq.setAmount("1.12");
//		fiatCurrencyInReq.setNotifyUrl("--");
//		fiatCurrencyInReq.setPayCode("8041");
//		String res = fiatCurrencyController.fiatCurrencyIn(fiatCurrencyInReq);
//		log.info(res);
//		log.info(orderId);

		FiatCurrencyQueryInReq fiatCurrencyQueryInReq = new FiatCurrencyQueryInReq();
		fiatCurrencyQueryInReq.setOrderNo("323d88f5dbfb4c1fb041436cc66b3d65");
		String s = fiatCurrencyController.fiatCurrencyQueryIn(fiatCurrencyQueryInReq);
		log.info(s);

//		FiatCurrencyOutReq fiatCurrencyOutReq = new FiatCurrencyOutReq();
//		fiatCurrencyOutReq.setCurrency("INR");
//		fiatCurrencyOutReq.setAccount("account-123456");
//		fiatCurrencyOutReq.setAmount("2.22");
//		fiatCurrencyOutReq.setName("name");
//		fiatCurrencyOutReq.setNotifyUrl("--");
//		fiatCurrencyOutReq.setOrderNo(IdUtil.fastSimpleUUID());
//		fiatCurrencyOutReq.setBankCode("BCA");
//		String res = fiatCurrencyController.fiatCurrencyOut(fiatCurrencyOutReq);
//		log.info(res);
	}
}
