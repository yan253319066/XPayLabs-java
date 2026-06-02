package com.yan.blockchain.pay.tron.service;

import cn.hutool.core.codec.Base58;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.yan.xpay.domain.CryptoAccount;
import com.yan.xpay.domain.PrivateKeyEncrypt;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.springframework.stereotype.Service;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.crypto.SECP256K1;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.core.contract.Contract;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class TronService {

	private final Map<com.yan.xpay.enums.Chain, ApiWrapper> apiWrappers;

	// -----------------------------
	// 配置限流参数
	// -----------------------------

	private final int rps = 5;
	private final int maxConcurrent = 1; // 最大并发
	private final Semaphore semaphore = new Semaphore(rps);
	private final Semaphore concurrencySemaphore = new Semaphore(maxConcurrent);
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@PostConstruct
	public void init() {
		scheduler.scheduleAtFixedRate(() -> {
			int permitsToRelease = rps - semaphore.availablePermits();
			if (permitsToRelease > 0) {
				semaphore.release(permitsToRelease);
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	// -----------------------------
	// 核心限流调用方法
	// -----------------------------
	public <T> T executeWithRateLimit(Supplier<T> rpcCall) {
		try {
			// 先获取并发许可
			concurrencySemaphore.acquire();
			// 获取许可，阻塞直到可用
			semaphore.acquire();
			T result = rpcCall.get();
			Thread.sleep(200);
			return result;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Tron RPC 调用被中断", e);
		} catch (Exception e) {
			throw new RuntimeException("Tron RPC 调用失败", e);
		} finally {
			concurrencySemaphore.release();
		}
	}

	@PreDestroy
	public void shutdown() {
		scheduler.shutdown();
	}

	//	private final TronRateLimitedExecutor limiter = new TronRateLimitedExecutor();
//
//	public <T> T executeWithRateLimit(Supplier<T> supplier) {
//		try {
//			return limiter.submit(supplier).get(); // 仍然同步等结果
//		} catch (Exception e) {
//			throw new RuntimeException("执行 Tron 接口限流任务失败", e);
//		}
//	}
//
//	public <T> T executeWithRateLimit(Supplier<T> supplier, int permits) {
//		try {
//			return limiter.submit(supplier, permits).get(); // 仍然同步等结果
//		} catch (Exception e) {
//			throw new RuntimeException("执行 Tron 接口限流任务失败", e);
//		}
//	}

//	@PreDestroy
//	public void shutdownLimiter() {
//		log.info("关闭 Tron 限流执行器");
//		limiter.shutdown();
//	}


//	public void executeWithRateLimit(Runnable runnable) {
//		executeWithRateLimit(() -> {
//			runnable.run();
//			return null;
//		});
//	}

	public static void main(String[] args) throws IllegalException {
		ApiWrapper wrapper = ApiWrapper.ofMainnet("");
//		Response.AccountResourceMessage resource = wrapper.getAccountResource("TUihujDaoQqHnyzqefg5PX49j6AyB9Jhtq");
//		log.info("{}",resource);
//		log.info("{}",resource.getFreeNetLimit());
//		long toNetRemaining = resource.getNetLimit() - resource.getNetUsed();
//		long toEnergyRemaining = resource.getEnergyLimit() - resource.getEnergyUsed();
//		log.info("{} {}",toNetRemaining, toEnergyRemaining);
//		log.info("{}", wrapper.getAccount("TGyjjt1esfqJWrPncpygq3QA43epY46V8D"));
//		TronService tronService = new TronService();

//
		System.out.println(wrapper.getAccount("TWMvPngdGHG6vDcE36vr7HVBqiPpdmmkEx").getAddress().size() > 0);
	}

	public static PrivateKeyEncrypt getPrivateKeyEncrypt(String hexPrivateKey){
		PrivateKeyEncrypt privateKeyEncrypt = new PrivateKeyEncrypt();
		privateKeyEncrypt.setAddress(getAddress(hexPrivateKey));
		String pwd = IdUtil.simpleUUID();
		privateKeyEncrypt.setEncrypt(pwd);
		byte[] key = SecureUtil.decode(pwd);
		AES aes = SecureUtil.aes(key);
		String keystore = aes.encryptHex(hexPrivateKey);
		privateKeyEncrypt.setKeystore(keystore);
		return privateKeyEncrypt;
	}

	public static String getAddress(String hexPrivateKey) {
		KeyPair keyPair = new KeyPair(hexPrivateKey);
		return keyPair.toBase58CheckAddress();
	}

	public Response.AccountResourceMessage getAccountResource(com.yan.xpay.enums.Chain chain, String address) {
		if(chain == com.yan.xpay.enums.Chain.TRON) {
			return executeWithRateLimit(
				() -> apiWrappers.get(chain).getAccountResource(address)
			);
		}else {
			return apiWrappers.get(chain).getAccountResource(address);
		}
	}

	public boolean isActivated(com.yan.xpay.enums.Chain chain, String address) {
		if(chain == com.yan.xpay.enums.Chain.TRON) {
			return executeWithRateLimit(
				() -> !apiWrappers.get(chain).getAccount(address).getAddress().isEmpty()
			);
		}else {
			return !apiWrappers.get(chain).getAccount(address).getAddress().isEmpty();
		}
	}

	public CryptoAccount generateTronAccount() {
		// 1. 生成密钥对
		SECP256K1.KeyPair keyPair = SECP256K1.KeyPair.generate();

		// 2. 获取未压缩公钥
		byte[] pubKey = keyPair.getPublicKey().getEncoded(); // false 表示未压缩

		// 3. 对公钥做 Keccak256 哈希
		byte[] hash = new Keccak.Digest256().digest(pubKey);

		// 4. 取后20字节（地址部分）
		byte[] addressBytes = new byte[21];
		addressBytes[0] = 0x41; // 主网地址前缀（TestNet 是 0xa0）
		System.arraycopy(hash, 12, addressBytes, 1, 20);

		// 5. 编码成 Base58 格式地址
		String base58Address = Base58Check.bytesToBase58(addressBytes);

		// 6. 获取私钥（hex 格式）
		String privateKeyHex = Hex.toHexString(keyPair.getPrivateKey().getEncoded());

		return new CryptoAccount(base58Address, privateKeyHex);

	}

	public Response.BlockExtention getBlockByNum(com.yan.xpay.enums.Chain chain, Long blockNumber) throws IllegalException {
		if(chain == com.yan.xpay.enums.Chain.TRON) {
			return executeWithRateLimit(
				() -> {
					try {
						return apiWrappers.get(chain).getBlockByNum(blockNumber);
					} catch (IllegalException e) {
						log.info("获取块失败 高度{} {}", blockNumber, e.getMessage());
						return null;
					}
				}
			);
		}else {
			return apiWrappers.get(chain).getBlockByNum(blockNumber);
		}
	}

	public Long getTrxBalance(com.yan.xpay.enums.Chain chain, String address){
		// 1. 查询TRX余额
		if(chain == com.yan.xpay.enums.Chain.TRON) {
			return executeWithRateLimit(
				() -> apiWrappers.get(chain).getAccountBalance(address)
			);
		}else {
			return apiWrappers.get(chain).getAccountBalance(address);
		}
	}

	public BigInteger getTrc20Balance(com.yan.xpay.enums.Chain chain, String ownerAddress, String contractAddress) {
		if(chain == com.yan.xpay.enums.Chain.TRON) {
			return executeWithRateLimit(()->{
				// 获取合约信息
				Contract contract = apiWrappers.get(chain).getContract(contractAddress);
				// 构建 TRC20 合约对象
				Trc20Contract token = new Trc20Contract(contract, ownerAddress, apiWrappers.get(chain));
				// 查询余额（原始 BigInteger 类型，为 USDT 的最小单位）
				BigInteger raw = token.balanceOf(ownerAddress);
				return raw;
			});
		}else {
			// 获取合约信息
			Contract contract = apiWrappers.get(chain).getContract(contractAddress);
			// 构建 TRC20 合约对象
			Trc20Contract token = new Trc20Contract(contract, ownerAddress, apiWrappers.get(chain));
			// 查询余额（原始 BigInteger 类型，为 USDT 的最小单位）
			BigInteger raw = token.balanceOf(ownerAddress);
			return raw;
		}
	}

	public String sendTrx(com.yan.xpay.enums.Chain chain, String toAddress, Long amount){
		return sendTrx(apiWrappers.get(chain), toAddress, amount);
	}

	public String sendTrx(ApiWrapper wrapper, String toAddress, Long amount){

		String fromAddress = wrapper.keyPair.toBase58CheckAddress();
		if(fromAddress.equalsIgnoreCase(toAddress)) {
			throw new RuntimeException("不能自己转给自己");
		}
		// 构建转账交易
		Response.TransactionExtention txn = executeWithRateLimit(
			() -> {
				try {
					return wrapper.transfer(fromAddress, toAddress, amount);
				} catch (IllegalException e) {
					throw new RuntimeException(e);
				}
			}
		);

		// 签名并广播
		Chain.Transaction signedTxn = wrapper.signTransaction(txn);
		String txId = executeWithRateLimit(()-> wrapper.broadcastTransaction(signedTxn));
		return txId;

	}

	public String sendTrc20(com.yan.xpay.enums.Chain chain, String contractAddress, String toAddress, Long amount){
		return sendTrc20(apiWrappers.get(chain), contractAddress, toAddress, amount);
	}

	/**
	 * 发送trc20
	 * @param wrapper
	 * @param contractAddress
	 * @param toAddress
	 * @param amount
	 * @return
	 */
	public String sendTrc20(ApiWrapper wrapper, String contractAddress, String toAddress, Long amount){
		Trc20Contract trc20Contract = executeWithRateLimit(
			() -> {
				String fromAddress = wrapper.keyPair.toBase58CheckAddress();
				if(fromAddress.equalsIgnoreCase(toAddress)) {
					throw new RuntimeException("不能自己转给自己");
				}
				// 获取合约
				Contract contract = wrapper.getContract(contractAddress);
				return new Trc20Contract(contract, fromAddress, wrapper);
			}
		);

		long feeLimit = 50_000_000L;
		int decimals = executeWithRateLimit(()->trc20Contract.decimals().intValue());
		String txId = executeWithRateLimit(()->trc20Contract.transfer(toAddress, amount, decimals, "", feeLimit));
		return txId;
	}

	public Chain.Transaction getTransactionById(com.yan.xpay.enums.Chain chain, String txId) {
		if(chain == com.yan.xpay.enums.Chain.TRON) {
			return executeWithRateLimit(
				() -> {
					try {
						return apiWrappers.get(chain).getTransactionById(txId);
					} catch (IllegalException e) {
						throw new RuntimeException(e);
					}
				}
			);
		}else {
			try {
				return apiWrappers.get(chain).getTransactionById(txId);
			} catch (IllegalException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public Response.TransactionInfo getTransactionInfoById(com.yan.xpay.enums.Chain chain, String txId) {
		if(chain == com.yan.xpay.enums.Chain.TRON) {
			return executeWithRateLimit(
				() -> {
					try {
						return apiWrappers.get(chain).getTransactionInfoById(txId);
					} catch (IllegalException e) {
						throw new RuntimeException(e);
					}
				}
			);
		}else {
			try {
				return apiWrappers.get(chain).getTransactionInfoById(txId);
			} catch (IllegalException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public Chain.Block getNowBlock(com.yan.xpay.enums.Chain chain){
		if(chain == com.yan.xpay.enums.Chain.TRON) {
			return executeWithRateLimit(
				() -> {
					try {
						return apiWrappers.get(chain).getNowBlock();
					} catch (IllegalException e) {
						throw new RuntimeException(e);
					}
				}
			);
		}else {
			try {
				return apiWrappers.get(chain).getNowBlock();
			} catch (IllegalException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 估算能量
	 * @param toAddress
	 * @return
	 */
	public static Long estimateEnergy(String toAddress) {
		String toAddressHex = Hex.toHexString(Base58Check.base58ToBytes(toAddress)); // 41开头
		log.info("toAddressHex {}", toAddressHex);
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

}

