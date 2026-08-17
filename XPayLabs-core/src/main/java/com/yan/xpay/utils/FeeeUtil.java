package com.yan.xpay.utils;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeeeUtil {
	private static final String USER_AGENT = "XPayLabs";
	/**
	 * https://feee.io/
	 */
	public static Integer leaseEnergy(String address, Long energy, String apiKey) {
		JSONObject paramMap = new JSONObject();
		paramMap.set("resource_type", 1);
		paramMap.set("receive_address", address);
		paramMap.set("resource_value", energy > 32000 ? energy : 32000);
		paramMap.set("rent_duration", 1);
		paramMap.set("rent_time_unit", "h");
		String result = HttpRequest.post("https://feee.io/open/v2/order/submit")
			.header(Header.USER_AGENT, USER_AGENT)// 头信息，多个头信息多次调用此方法即可
				.header("key", apiKey).header(Header.CONTENT_TYPE, "application/json")
				.body(paramMap.toString()).timeout(20000)// 超时，毫秒
				.execute().body();
		log.info("能量租赁.. {}", result);
		return JSONUtil.parseObj(result).getInt("code", -1);
	}

	public static Double price(Long resourceValue, int rentDuration, String rentTimeUnit, String apiKey) {
		String result = HttpRequest
				.get("https://feee.io/open/v2/order/price?resource_value=" + resourceValue + "&rent_time_unit="
						+ rentTimeUnit + "&rent_duration=" + rentDuration)
			.header(Header.USER_AGENT, USER_AGENT)// 头信息，多个头信息多次调用此方法即可
				.header("key", apiKey).header(Header.CONTENT_TYPE, "application/json")
				.timeout(20000)// 超时，毫秒
				.execute().body();
		log.info("能量价格 {}", result);
		return JSONUtil.parseObj(result).getJSONObject("data").getDouble("pay_amount", 1000.00);
	}

	public static Long estimateEnergy(String fromAddress, String toAddress, String apiKey) {
		String result = HttpRequest
			.get("https://feee.io/open/v2/order/estimate_energy?from_address=" + fromAddress + "&to_address="
				+ toAddress)
			.header(Header.USER_AGENT, USER_AGENT)// 头信息，多个头信息多次调用此方法即可
			.header("key", apiKey).header(Header.CONTENT_TYPE, "application/json")
			.timeout(20000)// 超时，毫秒
			.execute().body();
		log.info("能量预估 fromAddress {} toAddress {} result {}", fromAddress, toAddress, result);
		return JSONUtil.parseObj(result).getJSONObject("data").getLong("energy_used");
	}

	public static JSONObject accountInfo(String apiKey){
		String result = HttpRequest
			.get("https://feee.io/open/v2/api/query")
			.header(Header.USER_AGENT, USER_AGENT)// 头信息，多个头信息多次调用此方法即可
			.header("key", apiKey).header(Header.CONTENT_TYPE, "application/json")
			.timeout(20000)// 超时，毫秒
			.execute().body();
		JSONObject data = JSONUtil.parseObj(result).getJSONObject("data");
		JSONObject res = new JSONObject();
		res.set("trxBalance", data.getStr("trx_money"));//平台余额
		res.set("boundAddress", data.getStr("trx_address"));//账户绑定的钱包
		res.set("rechargeAddress", data.getStr("recharge_address"));//平台充值钱包
		return res;
	}
}
