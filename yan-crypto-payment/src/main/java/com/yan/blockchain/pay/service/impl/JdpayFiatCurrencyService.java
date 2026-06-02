package com.yan.blockchain.pay.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.blockchain.pay.config.FiatCurrencyConfig;
import com.yan.blockchain.pay.model.FiatCurrencyIn;
import com.yan.blockchain.pay.model.FiatCurrencyOut;
import com.yan.blockchain.pay.req.*;
import com.yan.blockchain.pay.service.FiatCurrencyService;
import com.yan.blockchain.pay.utils.JdpayFiatCurrencyUtils;
import com.yan.blockchain.pay.vo.FiatCurrencyInResult;
import com.yan.blockchain.pay.vo.FiatCurrencyQueryResult;
import com.yan.xpay.domain.MerchantAssets;
import com.yan.xpay.domain.SimpleTransfer;
import com.yan.xpay.domain.bo.FiatcurrencyOrderBo;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.enums.AssetOperType;
import com.yan.xpay.service.IFiatcurrencyOrderService;
import com.yan.xpay.service.IMerchantAssetsService;
import com.yan.xpay.utils.WebhookSignUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service("jdpayFiatCurrencyService")
@RequiredArgsConstructor
public class JdpayFiatCurrencyService implements FiatCurrencyService {

	private final IFiatcurrencyOrderService fiatcurrencyOrderService;
	private final IMerchantAssetsService merchantAssetsService;

	private static final String REDIS_KEY_FIAT_CURRENCY = "xpay:fiat_currency:jdpay";
	private static final ObjectMapper mapper = new ObjectMapper();

	private final FiatCurrencyConfig fiatCurrencyConfig;

	@Override
	public String fiatCurrencyIn(FiatCurrencyInReq req, MerchantVo merchant) {
		String res = JdpayFiatCurrencyUtils.jdpayFiatCurrencyIn(req, fiatCurrencyConfig.getJdpay());
		if(JSONUtil.isTypeJSON(res)) {
			JSONObject json = JSONUtil.parseObj(res);
			String code = json.getStr("code", "500");
			if(code.equals("200")) {
				JSONObject obj = JSONUtil.parseObj(req);
				obj.set("secret", merchant.getWebhookSecret());
				RedisUtils.setCacheMapValue(REDIS_KEY_FIAT_CURRENCY, req.getOrderNo(), obj.toString());
			}
		}
		return res;
	}
	@Override
	public String fiatCurrencyOut(FiatCurrencyOutReq req,MerchantVo merchant) {
		String res = JdpayFiatCurrencyUtils.jdpayFiatCurrencyOut(req, fiatCurrencyConfig.getJdpay());
		if(JSONUtil.isTypeJSON(res)) {
			JSONObject json = JSONUtil.parseObj(res);
			String code = json.getStr("code", "500");
			if(code.equals("200")) {
				JSONObject obj = JSONUtil.parseObj(req);
				obj.set("secret", merchant.getWebhookSecret());
				RedisUtils.setCacheMapValue(REDIS_KEY_FIAT_CURRENCY, req.getOrderNo(), obj.toString());
			}
		}
		return res;
	}

	@Override
	public FiatCurrencyInResult fiatCurrencyInV2(FiatcurrencyOrderBo bo) {
		FiatCurrencyInResult result = new FiatCurrencyInResult();
		FiatCurrencyIn fiatCurrencyIn = new FiatCurrencyIn();
		fiatCurrencyIn.setName(bo.getPayerName());
		fiatCurrencyIn.setCurrency(bo.getCurrency());
		fiatCurrencyIn.setEmail(bo.getPayerEmail());
		fiatCurrencyIn.setPhone(bo.getPayerPhone());
		fiatCurrencyIn.setAmount(bo.getAmount().toPlainString());
		fiatCurrencyIn.setOrder_no(bo.getOrderNo());
		fiatCurrencyIn.setPay_code(bo.getPayerCode());
		String res = JdpayFiatCurrencyUtils.jdpayFiatCurrencyInV2(fiatCurrencyIn, fiatCurrencyConfig.getJdpay());
		bo.setThirdPartyResponse(res);
		if(JSONUtil.isTypeJSON(res)) {
			JSONObject json = JSONUtil.parseObj(res);
			String code = json.getStr("code", "500");
			if(code.equals("200")) {
				fiatcurrencyOrderService.saveFiatCurrency(bo);
				result.setOrderData(json.getJSONObject("data").getStr("order_data", ""));
				result.setDeepLink(json.getJSONObject("data").getStr("deep_link", ""));
				result.setAmount(bo.getAmount().toPlainString());
				result.setOrderNo(bo.getOrderNo());
				return result;
			}
			throw new ServiceException(json.getStr("msg"));
		}
		log.error("fiatCurrencyInV2 error {}", res);
		return result;
	}

	@Transactional
	@Override
	public boolean fiatCurrencyOutV2(FiatcurrencyOrderBo bo) {
		MerchantAssets assets = merchantAssetsService.getBalance(bo.getMerchantId(), bo.getCurrency());
		if(assets.getBalance().compareTo(bo.getAmount()) < 0) throw new ServiceException("Not sufficient funds");
		FiatCurrencyOut fiatCurrencyOut = new FiatCurrencyOut();
		fiatCurrencyOut.setCurrency(bo.getCurrency());
		fiatCurrencyOut.setEmail(bo.getPayeeEmail());
		fiatCurrencyOut.setAmount(bo.getAmount().toPlainString());
		fiatCurrencyOut.setName(bo.getPayeeName());
		fiatCurrencyOut.setPhone(bo.getPayeePhone());
		fiatCurrencyOut.setAccount(bo.getPayeeAccount());
		fiatCurrencyOut.setBank_code(bo.getPayeeCode());
		fiatCurrencyOut.setOrder_no(bo.getOrderNo());
		String res = JdpayFiatCurrencyUtils.jdpayFiatCurrencyOutV2(fiatCurrencyOut, fiatCurrencyConfig.getJdpay());
		bo.setThirdPartyResponse(res);
		if(JSONUtil.isTypeJSON(res)) {
			JSONObject json = JSONUtil.parseObj(res);
			String code = json.getStr("code", "500");
			if(code.equals("200")) {
				bo.setHandingFee(BigDecimal.ZERO);
				bo.setHandingRate(BigDecimal.ZERO);
				fiatcurrencyOrderService.saveFiatCurrency(bo);
				SimpleTransfer transfer = new SimpleTransfer();
				transfer.setSymbol(bo.getCurrency());
				transfer.setRate(bo.getHandingRate());
				transfer.setFee(bo.getHandingFee());
				transfer.setRemark("法币代付申请");
				transfer.setType(AssetOperType.FIAT_CURRENCY_PAYOUT_REQUEST);
				transfer.setMerchantId(bo.getMerchantId());
				transfer.setTransactionNo(bo.getOrderNo());
				transfer.setAmount(bo.getAmount());
				merchantAssetsService.transfer(transfer);
				return true;
			}
			throw new ServiceException(json.getStr("msg"));
		}
		log.error("fiatCurrencyOutV2 error {}", res);
		return false;
	}

	@Override
	public String fiatCurrencyQueryIn(FiatCurrencyQueryInReq req) {
		return JdpayFiatCurrencyUtils.jdpayQueryIn(req, fiatCurrencyConfig.getJdpay());
	}
	@Override
	public String fiatCurrencyQueryOut(FiatCurrencyQueryOutReq req) {
		return JdpayFiatCurrencyUtils.jdpayQueryOut(req, fiatCurrencyConfig.getJdpay());
	}

	@Override
	public FiatCurrencyQueryResult fiatCurrencyQueryInV2(String orderNo) {
		String res = JdpayFiatCurrencyUtils.jdpayQueryInV2(orderNo, fiatCurrencyConfig.getJdpay());
		FiatCurrencyQueryResult result = new FiatCurrencyQueryResult();
		if(JSONUtil.isTypeJSON(res)) {
			JSONObject obj = JSONUtil.parseObj(res);
			int code = obj.getInt("code");
			if(code == 200) {
				JSONObject data = obj.getJSONObject("data");
				result.setCurrency(data.getStr("currency"));
				result.setStatus(data.getStr("status"));
				result.setActualAmount(data.getStr("success_amount"));
				result.setAmount(data.getStr("order_amount"));
				result.setOrderNo(data.getStr("order_no"));
				result.setOriginal(res);
				return result;
			}
		}
		log.error("fiatCurrencyQueryInV2 error {}", res);
		return result;
	}

	@Override
	public FiatCurrencyQueryResult fiatCurrencyQueryOutV2(String orderNo) {
		String res = JdpayFiatCurrencyUtils.jdpayQueryOutV2(orderNo, fiatCurrencyConfig.getJdpay());
		FiatCurrencyQueryResult result = new FiatCurrencyQueryResult();
		if(JSONUtil.isTypeJSON(res)) {
			JSONObject obj = JSONUtil.parseObj(res);
			int code = obj.getInt("code");
			if(code == 200) {
				JSONObject data = obj.getJSONObject("data");
				result.setCurrency(data.getStr("currency"));
				result.setStatus(data.getStr("status"));
				result.setActualAmount(data.getStr("success_amount"));
				result.setAmount(data.getStr("order_amount"));
				result.setOrderNo(data.getStr("order_no"));
				result.setOriginal(res);
				return result;
			}
		}
		log.error("fiatCurrencyQueryOutV2 error {}", res);
		return result;
	}

	@Override
	public String fiatCurrencyNotifyIn(NotifyInReq req) {
		log.info("notifyIn req {}", JSONUtil.toJsonStr(req));
		boolean b = JdpayFiatCurrencyUtils.verifySign(req, req.getSign(), fiatCurrencyConfig.getJdpay().getPublicKey());
		if(!b) {
			log.error("签名错误 notifyIn jdpay sing {}", req.getSign());
			return "error";
		}

		String redisStr = RedisUtils.getCacheMapValue(REDIS_KEY_FIAT_CURRENCY, req.getOrder_no());
		String notifyUrl = JSONUtil.parseObj(redisStr).getStr("notifyUrl");
		if(StrUtil.isBlank(notifyUrl)) {
			log.error("notifyInUrl is null order_no {}", req.getOrder_no());
			return "error";
		}
		log.info("notifyIn notifyInUrl {}", notifyUrl);
		String secret = JSONUtil.parseObj(redisStr).getStr("secret");
		JSONObject obj = JSONUtil.parseObj(req);
		obj.set("timestamp", Instant.now().getEpochSecond());
		obj.set("nonce", IdUtil.fastSimpleUUID());
		obj.set("sign", WebhookSignUtil.getSignature(secret, mapper.convertValue(obj, Map.class)));
		String params = null;
		try {
			params = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		log.info("notifyIn params {}", params);
		String response = HttpRequest.post(notifyUrl).contentType("application/json").body(params).execute().body();
		log.info("回调商家接口 notifyIn response {}", response);
		if("SUCCESS".equals(req.getStatus())  || "FAIL".equals(req.getStatus()))  {
			RedisUtils.delCacheMapValue(REDIS_KEY_FIAT_CURRENCY,  req.getOrder_no());
		}

		return "ok";
	}

	@Override
	public String fiatCurrencyNotifyOut(NotifyOutReq req) {
		log.info("notifyOut req {}", JSONUtil.toJsonStr(req));
		boolean b = JdpayFiatCurrencyUtils.verifySign(req, req.getSign(), fiatCurrencyConfig.getJdpay().getPublicKey());
		if(!b) {
			log.error("签名错误 notifyOut jdpay sing {}", req.getSign());
			return "error";
		}
		String redisStr = RedisUtils.getCacheMapValue(REDIS_KEY_FIAT_CURRENCY, req.getOrder_no());
		String notifyUrl = JSONUtil.parseObj(redisStr).getStr("notifyUrl");
		if(StrUtil.isBlank(notifyUrl)) {
			log.error("notifyOutUrl is null order_no {}", req.getOrder_no());
			return "error";
		}
		log.info("notifyOut notifyOutUrl {}", notifyUrl);
		String secret = JSONUtil.parseObj(redisStr).getStr("secret");
		JSONObject obj = JSONUtil.parseObj(req);
		obj.set("timestamp", Instant.now().getEpochSecond());
		obj.set("nonce", IdUtil.fastSimpleUUID());
		obj.set("sign", WebhookSignUtil.getSignature(secret, mapper.convertValue(obj, Map.class)));
		String params = null;
		try {
			params = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		log.info("notifyOut params {}", params);
		String response = HttpRequest.post(notifyUrl).contentType("application/json").body(params).execute().body();
		log.info("回调商家接口 notifyOut response {}", response);
		if("SUCCESS".equals(req.getStatus())  || "FAIL".equals(req.getStatus()))  {
			RedisUtils.delCacheMapValue(REDIS_KEY_FIAT_CURRENCY,  req.getOrder_no());
		}
		return "ok";
	}

	@Override
	public String fiatCurrencyBalance() {
		return JdpayFiatCurrencyUtils.jdpayMerchantBalance(fiatCurrencyConfig.getJdpay());
	}
}
