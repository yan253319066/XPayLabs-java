package com.yan.blockchain.pay.eth.service;

import com.yan.xpay.enums.Chain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class NonceManager {
	private final EthService ethService;
	private static final String NONCE_KEY_PREFIX = "xpay:nonce";
	private final RedissonClient redisson;

	/**
	 * 生成复合Key：chain + address
	 */
	private String buildCompositeKey(Chain chain, String address) {
		return String.format("%s:%s:%s",  NONCE_KEY_PREFIX, chain.name(),  address.toLowerCase());
	}

	/**
	 * 获取下一个安全 nonce（原子操作）
	 */
	public BigInteger getNextNonce(Chain chain, String address) {
		String compositeKey = buildCompositeKey(chain, address);
		RLock lock = redisson.getLock(compositeKey  + ":lock");

		try {
			lock.lock();

			// 1. 获取链上最新 nonce
			BigInteger chainNonce = ethService.getNonce(chain,  address);

			// 2. 获取 Redis 缓存 nonce
			BigInteger cachedNonce = RedisUtils.getCacheObject(compositeKey);

			// 3. 计算安全 nonce
			BigInteger safeNonce = (cachedNonce == null)
				? chainNonce
				: chainNonce.max(cachedNonce);

			// 4. 预占下一个 nonce
			BigInteger nextNonce = safeNonce.add(BigInteger.ONE);
			RedisUtils.setCacheObject(compositeKey,  nextNonce);

			return safeNonce;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 确认 Nonce 使用状态
	 */
	public void confirmNonce(Chain chain, String address, BigInteger usedNonce) {
		String compositeKey = buildCompositeKey(chain, address);
		RLock lock = redisson.getLock(compositeKey  + ":lock");

		try {
			lock.lock();
			BigInteger current = RedisUtils.getCacheObject(compositeKey);

			if (current == null) {
				resetNonceFromChain(chain, address);
				return;
			}

			if (usedNonce.add(BigInteger.ONE).equals(current))  {
				return; // 连续使用，正常情况
			}

			log.error("Nonce  out of sync for {}/{} (used={}, expected={})",
				chain, address, usedNonce, current.subtract(BigInteger.ONE));

			resetNonceFromChain(chain, address);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 强制同步链上 Nonce
	 */
	public void resetNonceFromChain(Chain chain, String address) {
		String compositeKey = buildCompositeKey(chain, address);
		RLock lock = redisson.getLock(compositeKey  + ":lock");

		try {
			lock.lock();
			BigInteger chainNonce = ethService.getNonce(chain,  address);
			RedisUtils.setCacheObject(compositeKey,  chainNonce);
			log.info("Reset  nonce for {}/{} to {}", chain, address, chainNonce);
		} catch (Exception e) {
			log.error("Reset  nonce failed for {}/{}", chain, address, e);
			throw new RuntimeException("Nonce reset failed", e);
		} finally {
			lock.unlock();
		}
	}
}
