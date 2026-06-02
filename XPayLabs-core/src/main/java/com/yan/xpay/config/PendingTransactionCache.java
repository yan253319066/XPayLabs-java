package com.yan.xpay.config;

import cn.hutool.core.util.ObjectUtil;
import com.yan.xpay.domain.Transaction;
import com.yan.xpay.enums.Chain;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PendingTransactionCache {
	public final static String redis_address_key = "xpay:transaction:pending";

	public void setPendingTransaction(Chain chain, Transaction tx) {
		RedisUtils.setCacheMapValue(redis_address_key + chain, tx.getTxid(), tx);
	}

	public Transaction getPendingTransaction(Chain chain, String txid) {
		return RedisUtils.getCacheMapValue(redis_address_key + chain, txid);
	}

	public Map<String, Transaction> getPendingTransactionList(Chain chain) {
		return RedisUtils.getCacheMap(redis_address_key + chain);
	}

	public void deletePendingTransaction(Chain chain, String txid) {
		RedisUtils.delCacheMapValue(redis_address_key + chain, txid);
	}

	public boolean existPendingTransaction(Chain chain, String txid) {
		return ObjectUtil.isNotNull(getPendingTransaction(chain, txid));
	}

}
