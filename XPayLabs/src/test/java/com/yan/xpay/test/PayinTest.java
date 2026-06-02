package com.yan.xpay.test;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.xpay.domain.req.CreateCollectionOrderReq;
import com.yan.xpay.domain.req.ReqPayload;
import com.yan.xpay.domain.vo.ApiKeyVo;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.service.IMerchantService;
import com.yan.xpay.service.IPaymentOrderService;
import com.yan.xpay.utils.WebhookSignUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
public class PayinTest {
	@Autowired
	private IPaymentOrderService orderService;
	@Autowired
	private IMerchantService merchantService;

	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testCreateCollection() {
		ApiKeyVo apiKeyVo = merchantService.merchantApiKey("XpayTestV3");
		ReqPayload<CreateCollectionOrderReq> req = new ReqPayload<>();
		CreateCollectionOrderReq request = new CreateCollectionOrderReq();
		request.setAmount(BigDecimal.ONE);
		request.setOrderId(UUID.randomUUID().toString());
		request.setSymbol("USDT");
		request.setChain(Chain.TRON_TEST);
		req.setData(request);

		req.setTimestamp(Instant.now().getEpochSecond());
		req.setNonce(IdUtil.fastSimpleUUID());
		Map<String, Object> map = mapper.convertValue(req, Map.class);
		String sign = WebhookSignUtil.getSignature(apiKeyVo.getWebhookSecret(), map);
		req.setSign(sign);

		String token = apiKeyVo.getApiKey();
		String response = HttpRequest.post("http://localhost:8077/v1/order/createCollection").header("X-API-TOKEN",token).contentType("application/json").body(JSONUtil.toJsonStr(req)).execute().body();
		log.info("---- {}", response);
	}

//	@Test
	public void testConcurrentCreateCollection() throws InterruptedException {
		final int requestCount = 5;
		CountDownLatch latch = new CountDownLatch(requestCount);
		Set<String> addresses = new ConcurrentHashSet<>();
		ApiKeyVo apiKeyVo = merchantService.merchantApiKey("XpayTestV3");
		// 模拟并发请求
		for (int i = 0; i < requestCount; i++) {
			new Thread(() -> {
				try {
					ReqPayload<CreateCollectionOrderReq> req = new ReqPayload<>();
					CreateCollectionOrderReq request = new CreateCollectionOrderReq();
					request.setAmount(BigDecimal.ONE);
					request.setOrderId(UUID.randomUUID().toString());
					request.setSymbol("USDT");
					request.setChain(Chain.TRON_TEST);
					req.setData(request);

					req.setTimestamp(Instant.now().getEpochSecond());
					req.setNonce(IdUtil.fastSimpleUUID());
					Map<String, Object> map = mapper.convertValue(req, Map.class);
					String sign = WebhookSignUtil.getSignature(apiKeyVo.getWebhookSecret(), map);
					req.setSign(sign);

					String token = apiKeyVo.getApiKey();
					String response = HttpRequest.post("http://localhost:8077/v1/order/createCollection").header("X-API-TOKEN",token).contentType("application/json").body(JSONUtil.toJsonStr(req)).execute().body();
					log.info("---- {}", response);
					addresses.add(JSONUtil.parseObj(response).getJSONObject("data").getStr("address"));
				} finally {
					latch.countDown();
				}
			}).start();
		}

		latch.await(10,  TimeUnit.SECONDS);
		assertEquals(requestCount, addresses.size());
	}
}
