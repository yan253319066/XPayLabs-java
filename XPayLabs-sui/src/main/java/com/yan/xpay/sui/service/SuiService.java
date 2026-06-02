package com.yan.xpay.sui.service;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yan.xpay.domain.CryptoAccount;
import com.yan.xpay.sui.config.SuiConfig;
import com.yan.xpay.sui.model.SuiBlock;
import com.yan.xpay.sui.model.SuiTransaction;
import com.yan.xpay.sui.model.SuiTransactionBlocks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class SuiService {
	private static String suiServiceUrl; // SUI服务地址

	// 加密配置 (应存储在安全的地方，如环境变量或配置服务器)
	private static String ENCRYPTION_KEY; // 请替换为实际的32字符密钥

	@Autowired
	private void setSuiProperties(SuiConfig suiConfig) {
		// 将配置值赋值给静态变量
		suiServiceUrl = suiConfig.getServiceUrl();
		ENCRYPTION_KEY = suiConfig.getEncryptionKey();
		log.info("初始化 SUI 配置：serviceUrl={}", suiServiceUrl);
	}

	/**
	 * Encrypt using AES-256-CBC (consistent with Node.js side)
	 */
	private static String encrypt(String plainText) {
		try {
			// Use SHA-256 hash of ENCRYPTION_KEY to ensure it is 32 bytes
			byte[] keyBytes = ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8);
			if (keyBytes.length != 32) {
				// If not 32 bytes, use hash to ensure correct length
				java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
				keyBytes = sha.digest(keyBytes);
			}

			SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			// Generate 16-byte random IV
			byte[] iv = new byte[16];
			new SecureRandom().nextBytes(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

			// Encrypt data
			byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

			// Combine IV and encrypted data
			byte[] combined = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

			// Return Base64 encoded result
			return Base64.getEncoder().encodeToString(combined);
		} catch (Exception e) {
			throw new RuntimeException("Encryption failed: " + e.getMessage());
		}
	}

	/**
	 * 获取账户余额
	 * @param address SUI地址
	 * @param coinType 代币类型
	 * @param network 网络类型 (testnet/mainnet/devnet/localnet)
	 * @return 余额
	 */
	public static BigDecimal getBalance(String address, String coinType, String network) {
		String url = suiServiceUrl + "/balance/" + address + "?network=" + network;
		if(StrUtil.isNotBlank(coinType))
			url += "&coinType=" + coinType;
		try (HttpResponse response = HttpRequest.get(url).execute()) {
			if (response.getStatus() == 200) {
				// 使用Hutool解析JSON响应
				String body = response.body();
				JSONObject obj = JSONUtil.parseObj(body);
				return obj.getBigDecimal("balance");
			}
		}
		throw new ServiceException("获取账户余额失败");
	}

	/**
	 * 转账SUI代币
	 * @param privateKeyHex 发送方私钥 (十六进制格式)
	 * @param recipient 接收方地址
	 * @param amount 转账金额(区块链单位)
	 * @param network 网络类型 (testnet/mainnet/devnet/localnet)
	 * @return 交易ID
	 */
	public static String transferSUI(String privateKeyHex, String recipient, BigInteger amount, String network) {
		// 使用Hutool加密私钥
		String encryptedPrivateKey = encrypt(privateKeyHex);

		String url = suiServiceUrl + "/transfer-sui-with-key";

		// 构建请求参数
		Map<String, Object> request = new HashMap<>();
		request.put("encryptedPrivateKey", encryptedPrivateKey);
		request.put("recipient", recipient);
		request.put("amount", amount);
		request.put("network", network);

		// 发送POST请求
		HttpResponse response = HttpRequest.post(url)
			.header("Content-Type", "application/json")
			.body(JSONUtil.toJsonStr(request))
			.execute();

		if (response.getStatus() == 200) {
			String body = response.body();
			Map<String, Object> resultMap = JSONUtil.parseObj(body);
			Boolean success = (Boolean) resultMap.get("success");
			if (success != null && success) {
				return (String) resultMap.get("txid");
			}
		}
		return null;
	}

	/**
	 * 转账自定义代币
	 * @param privateKeyHex 发送方私钥 (十六进制格式)
	 * @param recipient 接收方地址
	 * @param amount 转账数量
	 * @param coinType 代币类型
	 * @param network 网络类型 (testnet/mainnet/devnet/localnet)
	 * @return 交易ID
	 */
	public static String transferToken(String privateKeyHex, String recipient, BigInteger amount, String coinType, String network) {
		// 使用Hutool加密私钥
		String encryptedPrivateKey = encrypt(privateKeyHex);

		String url = suiServiceUrl + "/transfer-token-with-key";

		// 构建请求参数
		Map<String, Object> request = new HashMap<>();
		request.put("encryptedPrivateKey", encryptedPrivateKey);
		request.put("recipient", recipient);
		request.put("amount", amount);
		request.put("coinType", coinType);
		request.put("network", network);

		// 发送POST请求
		HttpResponse response = HttpRequest.post(url)
			.header("Content-Type", "application/json")
			.body(JSONUtil.toJsonStr(request))
			.execute();

		if (response.getStatus() == 200) {
			String body = response.body();
			Map<String, Object> resultMap = JSONUtil.parseObj(body);
			Boolean success = (Boolean) resultMap.get("success");
			if (success != null && success) {
				return (String) resultMap.get("txid");
			}
		}
		return null;
	}

	/**
	 * 查询交易区块
	 * @param network 网络类型 (testnet/mainnet/devnet/localnet)
	 * @param limit 返回记录数限制
	 * @param cursor 游标，用于分页查询
	 * @param query 查询条件 (JSON字符串格式)
	 * @return 交易区块列表的JSON字符串
	 *
	 * query参数示例：
	 * 1. 查询来自特定地址的交易：
	 *    {"FromAddress": "0x123..."}
	 * 2. 查询发送到特定地址的交易：
	 *    {"ToAddress": "0x456..."}
	 * 3. 查询涉及某个地址的所有交易：
	 *    {"FromOrToAddress": "0x789..."}
	 * 4. 查询特定类型的交易：
	 *    {"Checkpoint": checkpointSequenceNumber}
	 * 5. 查询移动模块相关的交易：
	 *    {"MoveModule": {"package": "0x...", "module": "moduleName"}}
	 * 6. 查询移动函数相关的交易：
	 *    {"MoveFunction": {"package": "0x...", "module": "moduleName", "function": "functionName"}}
	 */
	public static SuiTransactionBlocks queryTransactionBlocks(String network, int limit, String cursor, String query) {
		String url = suiServiceUrl + "/transaction-blocks/" +
			"?network=" + network + "&limit=" + limit;
		if (cursor != null && !cursor.isEmpty()) {
			url += "&cursor=" + cursor;
		}

		// 添加自定义查询条件
		if (query != null && !query.isEmpty()) {
			// 解析query参数并添加到URL中
			Map<String, Object> queryMap = JSONUtil.parseObj(query);
			for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
				url += "&" + entry.getKey() + "=" + entry.getValue();
			}
		}

		HttpResponse response = HttpRequest.get(url).execute();
		if (response.getStatus() == 200) {
			return JSONUtil.toBean(response.body(), SuiTransactionBlocks.class);
		}

		return null;
	}

	// 添加一个重载方法，支持cursor参数
	public static SuiTransactionBlocks queryTransactionBlocks(String network, int limit, String cursor) {
		return queryTransactionBlocks(network, limit, cursor, null);
	}

	// 添加一个重载方法，保持向后兼容
	public static SuiTransactionBlocks queryTransactionBlocks(String network, int limit) {
		return queryTransactionBlocks(network, limit, null, null);
	}

	/**
	 * 分页获取所有交易
	 */
	public static List<SuiTransaction> queryAllTransactionBlocks(String network, JSONObject query, int limit) {
		List<SuiTransaction> allTxs = new ArrayList<>();
		String cursor = null;

		while (true) {
			SuiTransactionBlocks resp = queryTransactionBlocks(network, limit, cursor, query.toString());
			if (resp == null || resp.getData() == null) return allTxs;
			if(!resp.getData().isEmpty())
				allTxs.addAll(resp.getData());

			// 获取下一页 cursor
			if(resp.isHasNextPage())
				cursor = resp.getNextCursor();
			else break;
		}

		return allTxs;
	}

	/**
	 * 获取交易区块详情
	 * @param digest 交易摘要
	 * @param network 网络类型 (testnet/mainnet/devnet/localnet)
	 * @return 交易区块详情的JSON字符串
	 */
	public static SuiTransaction getTransactionBlock(String digest, String network) {
		String url = suiServiceUrl + "/transaction-block/" + digest + "?network=" + network;
		HttpResponse response = HttpRequest.get(url).execute();
		if (response.getStatus() == 200) {
			return JSONUtil.toBean(response.body(), SuiTransaction.class);
		}
		throw new ServiceException("获取交易区块详情失败");
	}

	/**
	 * 获取最新的检查点序列号
	 * @param network 网络类型 (testnet/mainnet/devnet/localnet)
	 * @return 最新检查点序列号
	 */
	public static BigInteger getLatestCheckpointSequenceNumber(String network) {
		String url = suiServiceUrl + "/checkpoint/latest?network=" + network;
		HttpResponse response = HttpRequest.get(url).execute();
		if (response.getStatus() == 200) {
			return JSONUtil.parseObj(response.body()).getBigInteger("sequenceNumber");
		}
		throw new ServiceException("获取最新检查点序列号失败");
	}

	/**
	 * 获取特定检查点信息
	 * @param checkpointId 检查点ID或序列号
	 * @param network 网络类型 (testnet/mainnet/devnet/localnet)
	 * @return 检查点信息的JSON字符串
	 */
	public static SuiBlock getCheckpoint(BigInteger checkpointId, String network) {
		String url = suiServiceUrl + "/checkpoint/" + checkpointId + "?network=" + network;
		HttpResponse response = HttpRequest.get(url).execute();
		if (response.getStatus() == 200) {
			return JSONUtil.toBean(response.body(), SuiBlock.class);
		}
		throw new ServiceException("获取检查点信息失败");
	}

	/**
	 * 获取检查点列表
	 * @param network 网络类型 (testnet/mainnet/devnet/localnet)
	 * @param limit 返回记录数限制
	 * @param cursor 游标，用于分页查询
	 * @param descendingOrder 是否降序排列
	 * @return 检查点列表的JSON字符串
	 */
	public static String getCheckpoints(String network, int limit, String cursor, boolean descendingOrder) {

		String url = suiServiceUrl + "/checkpoints?network=" + network + "&limit=" + limit;
		if (cursor != null && !cursor.isEmpty()) {
			url += "&cursor=" + cursor;
		}
		url += "&descendingOrder=" + descendingOrder;

		HttpResponse response = HttpRequest.get(url).execute();
		if (response.getStatus() == 200) {
			return response.body();
		}
		return null;

	}

	// 添加一个重载方法，使用默认参数
	public String getCheckpoints(String network, int limit) {
		return getCheckpoints(network, limit, null, true);
	}

	/**
	 * 预估代币转账Gas费用
	 * @param sender 发送方地址
	 * @param recipient 接收方地址
	 * @param amount 转账数量
	 * @param coinType 代币类型
	 * @param network 网络类型 (testnet/mainnet/devnet/localnet)
	 * @return Gas费用估算结果的JSON字符串
	 */
	public static BigDecimal estimateTokenTransferGasFee(String sender, String recipient, BigInteger amount, String coinType, String network) {

		String url = suiServiceUrl + "/estimate-token-transfer-gas-fee";

		// 构建请求参数
		Map<String, Object> request = new HashMap<>();
		request.put("sender", sender);
		request.put("recipient", recipient);
		request.put("amount", amount);
		request.put("coinType", coinType);
		request.put("network", network);

		// 发送POST请求
		HttpResponse response = HttpRequest.post(url)
			.header("Content-Type", "application/json")
			.body(JSONUtil.toJsonStr(request))
			.execute();

		if (response.getStatus() == 200) {
			JSONObject obj = JSONUtil.parseObj(response.body());
			return obj.getBigDecimal("netGasCost");
		}else {
			log.error(response.body());
		}

		throw new ServiceException("获取token Gas费用估算结果失败");

	}

	/**
	 * 预估SUI转账Gas费用
	 * @param sender 发送方地址
	 * @param recipient 接收方地址
	 * @param amount 转账金额
	 * @param network 网络类型 (testnet/mainnet/devnet/localnet)
	 * @return Gas费用估算结果的JSON字符串
	 */
	public static BigDecimal estimateTransferGasFee(String sender, String recipient, long amount, String network) {

		String url = suiServiceUrl + "/estimate-transfer-gas-fee";

		// 构建请求参数
		Map<String, Object> request = new HashMap<>();
		request.put("sender", sender);
		request.put("recipient", recipient);
		request.put("amount", amount);
		request.put("network", network);

		// 发送POST请求
		HttpResponse response = HttpRequest.post(url)
			.header("Content-Type", "application/json")
			.body(JSONUtil.toJsonStr(request))
			.execute();

		if (response.getStatus() == 200) {
			JSONObject obj = JSONUtil.parseObj(response.body());
			return obj.getBigDecimal("netGasCost");
		}
		throw new ServiceException("获取sui Gas费用估算结果失败");

	}

	/**
	 * 基于 Hutool + BouncyCastle 实现的 Sui 地址生成
	 * 兼容 Slush / Suiet / Ethos 钱包
	 */
	public static CryptoAccount generateSuiAccount() {
		// 1️⃣ 生成 Ed25519 密钥对
		SecureRandom random = new SecureRandom();
		Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(random);
		Ed25519PublicKeyParameters publicKey = privateKey.generatePublicKey();

		byte[] priKey = privateKey.getEncoded(); // 32 bytes
		byte[] pubKey = publicKey.getEncoded();  // 32 bytes

		// 2️⃣ 在公钥前加签名算法标识字节（0x00 表示 Ed25519）
		byte[] pkWithFlag = new byte[pubKey.length + 1];
		pkWithFlag[0] = 0x00;
		System.arraycopy(pubKey, 0, pkWithFlag, 1, pubKey.length);

		// 3️⃣ 计算 Blake2b-256 哈希
		Blake2bDigest digest = new Blake2bDigest(256);
		digest.update(pkWithFlag, 0, pkWithFlag.length);
		byte[] hash = new byte[32];
		digest.doFinal(hash, 0);

		// 4️⃣ 拼接 0x + Hex 地址
		String address = "0x" + HexUtil.encodeHexStr(hash);

//		result.put("privateKeyHex", HexUtil.encodeHexStr(priKey));
//		result.put("publicKeyHex", HexUtil.encodeHexStr(pubKey));
//		result.put("address", address);
		CryptoAccount account = new CryptoAccount(address, HexUtil.encodeHexStr(priKey));
		return account;
	}

	public static void main(String[] args) {
//		CryptoAccount account = SuiService.generateSuiAccount();
//		System.out.println(account.getAddress());
//		System.out.println(account.getPrivateKey());

	}
}
