package com.yan.blockchain.pay.eth.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.json.JSONUtil;
import com.yan.xpay.domain.PrivateKeyEncrypt;
import com.yan.xpay.enums.Chain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.crypto.exception.CipherException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EthService {
	private final Web3jProviderService web3jProviderService;

	public static void main(String[] args) {

		PrivateKeyEncrypt p = EthService.getPrivateKeyEncrypt("");
		log.info("{}", p);
	}

	public static PrivateKeyEncrypt getPrivateKeyEncrypt(String hexPrivateKey){
		PrivateKeyEncrypt privateKeyEncrypt = new PrivateKeyEncrypt();
		String pwd = IdUtil.simpleUUID();
		privateKeyEncrypt.setEncrypt(pwd);
		byte[] key = SecureUtil.decode(pwd);
		AES aes = SecureUtil.aes(key);
		try {
			// 1. 转换私钥为 BigInteger
			BigInteger privateKey = Numeric.toBigInt(hexPrivateKey);
			// 2. 创建 ECKeyPair
			ECKeyPair keyPair = ECKeyPair.create(privateKey);
			WalletFile walletFile = Wallet.createStandard(pwd,  keyPair);
			String address = "0x" + walletFile.getAddress();
			privateKeyEncrypt.setAddress(address);
			String keystore = aes.encryptHex(JSONUtil.toJsonStr(walletFile));
			privateKeyEncrypt.setKeystore(keystore);
			return privateKeyEncrypt;
		} catch (CipherException e) {
			throw new RuntimeException(e);
		}
	}

	public String getCurrentWeb3jUrl(Chain chain) {
		return web3jProviderService.getCurrentWeb3jUrl(chain);
	}

	public EthBlock.Block getBlock(Chain chain, BigInteger blockNumber) throws IOException {
		return web3jProviderService.getNextWeb3j(chain).ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true)
			.send()
			.getBlock();
	}

	public BigInteger getNowBlockNumber(Chain chain){
		try {
			return web3jProviderService.getNextWeb3j(chain)
				.ethBlockNumber()
				.send()
				.getBlockNumber();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public BigInteger calculateTxFee(TransactionReceipt receipt) {
		// 1. 获取实际消耗的 Gas 数量
		BigInteger gasUsed = receipt.getGasUsed();

		// 2. 解析 EffectiveGasPrice（16进制 → BigInteger）
		String effectiveGasPriceHex = receipt.getEffectiveGasPrice();
		BigInteger effectiveGasPrice = new BigInteger(effectiveGasPriceHex.substring(2),  16); // 去掉 "0x"

		// 3. 计算总手续费（单位：wei）
		BigInteger totalFee = gasUsed.multiply(effectiveGasPrice);

		return totalFee;
	}

	public Transaction getTransaction(Chain chain, String txId) {
		try {
			return web3jProviderService.getNextWeb3j(chain).ethGetTransactionByHash(txId).send().getTransaction().orElseThrow(()-> new RuntimeException("["+chain+"]Transaction not found"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public TransactionReceipt getReceipt(Chain chain, String txId){
		try {
			return web3jProviderService.getNextWeb3j(chain)
				.ethGetTransactionReceipt(txId)
				.send()
				.getTransactionReceipt()
				.orElseThrow(() -> new RuntimeException("["+chain+"]Transaction receipt not found"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public BigInteger getBalance(Chain chain, String address){
		try {
			return web3jProviderService.getNextWeb3j(chain).ethGetBalance(address,  DefaultBlockParameterName.LATEST).send().getBalance();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 获取ERC20代币余额
	 * @param chain
	 * @param contractAddress ERC20合约地址
	 * @param address 钱包地址
	 * @return 代币余额(最小单位)
	 */
	public BigInteger getErc20Balance(Chain chain, String contractAddress, String address) {
		// 1. 构造balanceOf函数调用
		Function function = new Function(
			"balanceOf",
			Collections.singletonList(new  Address(address)),
			Collections.singletonList(new  TypeReference<Uint256>() {})
		);

		// 2. 编码函数调用
		String encodedFunction = FunctionEncoder.encode(function);

		// 3. 执行eth_call查询
		EthCall response = null;
		try {
			response = web3jProviderService.getNextWeb3j(chain).ethCall(
					org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
						address, contractAddress, encodedFunction),
					DefaultBlockParameterName.LATEST)
				.send();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// 4. 解码返回结果
		String value = response.getValue();
		if (StrUtil.isBlank(value) || value.equals("0x"))  {
			return BigInteger.ZERO;
		}

		List<Type> results = FunctionReturnDecoder.decode(value,  function.getOutputParameters());
		return (BigInteger) results.get(0).getValue();
	}

	public BigInteger getGasPrice(Chain chain) {
		return getGasPrice(web3jProviderService.getNextWeb3j(chain));
	}
	public BigInteger getGasPrice(Web3j web3j) {
		try {
			return web3j.ethGasPrice().send().getGasPrice();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public BigInteger getNonce(Chain chain, String address){
		return getNonce(web3jProviderService.getNextWeb3j(chain), chain, address);
	}
	public BigInteger getNonce(Web3j web3j, Chain chain, String address){
		BigInteger nonce = null;
		try {
			nonce = web3j.ethGetTransactionCount(
				address,
				DefaultBlockParameterName.PENDING
			).send().getTransactionCount();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return nonce;
	}

	public record Gas(BigInteger gasCost, BigInteger maxFeePerGas, BigInteger maxPriorityFeePerGas, BigInteger gasPrice) {}

	public boolean isEIP1559(Web3j web3j) {
		BigInteger baseGas = null;
		try {
			baseGas = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
				.send()
				.getBlock()
				.getBaseFeePerGas();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return baseGas != null && baseGas.compareTo(BigInteger.ZERO) > 0;
	}
	public Gas getGasCost(Web3j web3j, boolean isEIP1559,  BigInteger gasLimit) throws IOException {
		BigInteger gasCost;
		BigInteger maxPriorityFeePerGas = null;
		BigInteger maxFeePerGas = null;
		BigInteger gasPrice = null;
		if(isEIP1559){
			DefaultBlockParameter latest = DefaultBlockParameterName.LATEST;
			List<Double> rewards = Arrays.asList(
				Double.valueOf(10),
				Double.valueOf(50),
				Double.valueOf(90)
			);

			EthFeeHistory feeHistory = web3j.ethFeeHistory(5, latest, rewards)
				.send();

			BigInteger baseFee = feeHistory.getFeeHistory().getBaseFeePerGas().get(0);
			maxPriorityFeePerGas = Convert.toWei("2", Convert.Unit.GWEI).toBigInteger();
			maxFeePerGas = baseFee.multiply(BigInteger.valueOf(2)).add(maxPriorityFeePerGas);

			// 计算实际消耗的 gas 成本（通常为 baseFee + priorityFee）
			BigInteger effectiveGasPrice = baseFee.add(maxPriorityFeePerGas);
			gasCost = gasLimit.multiply(effectiveGasPrice);
		}else{
			gasPrice = web3j.ethGasPrice().send().getGasPrice();
			gasCost = gasLimit.multiply(gasPrice);
		}
		return new Gas(gasCost, maxFeePerGas, maxPriorityFeePerGas, gasPrice);
	}

	public RawTransaction buildTransaction(
		Web3j web3j,
		long chainId,
		BigInteger nonce,
		String to,
		BigInteger value,
		BigInteger gasLimit
	) throws IOException {
		boolean isEIP1559 = isEIP1559(web3j);
		Gas gas = getGasCost(web3j, isEIP1559, gasLimit);
		if (isEIP1559) {//EIP1559
			return RawTransaction.createEtherTransaction(
				chainId,
				nonce,
				gasLimit,
				to,
				value,
				gas.maxPriorityFeePerGas,
				gas.maxFeePerGas
			);
		} else {
			// Legacy
			return RawTransaction.createEtherTransaction(
				nonce,
				gas.gasPrice,
				gasLimit,
				to,
				value
			);
		}
	}

	public String sendEth(Chain chain, Credentials credentials, String toAddress, BigInteger amount, BigInteger nonce) {
		Web3j web3j = web3jProviderService.getNextWeb3j(chain);
		try {
			// 2. 获取当前gas价格
			BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
			// 3. 设置gas limit（标准转账为21000）
			BigInteger gasLimit = Transfer.GAS_LIMIT;

			// 4. 创建原始交易对象
			RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
				nonce,
				gasPrice,
				gasLimit,
				toAddress,
				amount
			);

			// 5. 签名交易
			byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Long.parseLong(chain.getChainId()),  credentials);
			String hexValue = Numeric.toHexString(signedMessage);

			// 6. 发送原始交易
			EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

			if (ethSendTransaction.hasError())  {
				throw new RuntimeException("["+chain+"] 交易发送失败: " + ethSendTransaction.getError().getMessage());
			}

			return ethSendTransaction.getTransactionHash();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String sendEth(Chain chain, Credentials credentials, String toAddress, BigInteger amount) {
		// 1. 获取nonce（交易序号）
		BigInteger nonce = getNonce(chain, credentials.getAddress());
		return sendEth(chain, credentials, toAddress, amount, nonce);
	}

	/**
	 * ERC20代币转账
	 * @param chain
	 * @param credentials
	 * @param contractAddress ERC20合约地址
	 * @param toAddress 接收地址
	 * @param amount 转账金额（代币最小单位）
	 * @param nonce
	 * @return 交易哈希
	 */
	public String sendErc20(Chain chain, Credentials credentials, String contractAddress,
		String toAddress, BigInteger amount, BigInteger nonce) {

		// 参数校验
		if(amount.compareTo(BigInteger.ZERO)  <= 0) {
			throw new IllegalArgumentException("转账金额必须大于0");
		}

		Web3j web3j = web3jProviderService.getNextWeb3j(chain);

		try {
			// 1. 构造transfer函数
			Function function = new Function(
				"transfer",
				Arrays.asList(new  Address(toAddress), new Uint256(amount)),
				Collections.emptyList()
			);
			String encodedFunction = FunctionEncoder.encode(function);

			// 2. 获取gas参数
			BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
			// 3. 估算gasLimit
			BigInteger gasLimit = estimateGasLimit(web3j, credentials.getAddress(), contractAddress, encodedFunction);

			// 4. 创建交易
			RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, encodedFunction);
			// 5. 签名交易
			byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Long.parseLong(chain.getChainId()),  credentials);
			String hexValue = Numeric.toHexString(signedMessage);

			// 6. 发送原始交易
			EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

			if (ethSendTransaction.hasError())  {
				throw new RuntimeException("["+chain+"] 交易发送失败: " + ethSendTransaction.getError().getMessage());
			}

			return ethSendTransaction.getTransactionHash();
		} catch (Exception e) {
			throw new RuntimeException("["+chain+"] ERC20转账失败: ",  e);
		}
	}

	/**
	 * ERC20代币转账
	 * @param chain
	 * @param credentials
	 * @param contractAddress ERC20合约地址
	 * @param toAddress 接收地址
	 * @param amount 转账金额（代币最小单位）
	 * @return 交易哈希
	 */
	public String sendErc20(Chain chain, Credentials credentials, String contractAddress,
		String toAddress, BigInteger amount) {
		return sendErc20(chain, credentials, contractAddress, toAddress, amount, getNonce(chain, credentials.getAddress()));
	}

	public BigInteger estimateGasLimit(Web3j web3j, String address,
		String contract, String data) {
		return BigInteger.valueOf(80000);
//		EthEstimateGas estimate = null;
//		try {
//			estimate = web3j.ethEstimateGas(
//				org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
//					address,
//					contract,
//					data
//				)
//			).send();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		return estimate.getAmountUsed().add(BigInteger.valueOf(10_000));  // 安全余量
	}

	public BigInteger estimateGasLimit(Chain chain,String address,
		String contract, BigInteger amount) {
		return BigInteger.valueOf(80000);
//		Function function = new Function(
//			"transfer",
//			Arrays.asList(new  Address(address), new Uint256(amount)),
//			Collections.emptyList()
//		);
//		String encodedFunction = FunctionEncoder.encode(function);
//		EthEstimateGas estimate = null;
//		try {
//			estimate = web3jProviderService.getNextWeb3j(chain).ethEstimateGas(
//				org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
//					address,
//					contract,
//					encodedFunction
//				)
//			).send();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		return estimate.getAmountUsed().add(BigInteger.valueOf(20_000));  // 安全余量
	}

	public BigDecimal calculateGas(Chain chain, String contractAddress) throws IOException {

		Web3j web3j = web3jProviderService.getNextWeb3j(chain);
		String fromAddress = "0x1440ec793aE50fA046B95bFeCa5aF475b6003f9e";
		String toAddress = "0x1440ec793aE50fA046B95bFeCa5aF475b6003f9e";

		Function function = new Function(
			"transfer",
			Arrays.asList(new Address(toAddress), new Uint256(BigInteger.valueOf(1000))), // 1 USDT (6 decimals)
			Collections.emptyList()
		);

		String encodedData = FunctionEncoder.encode(function);

		EthGasPrice gasPriceResponse = web3j.ethGasPrice().send();
//		log.info("EthService calculateGas gasPriceResponse {}", JSONUtil.toJsonStr(gasPriceResponse));
		if(gasPriceResponse.hasError()) throw new RuntimeException(chain +" "+ gasPriceResponse.getError().getMessage());
		BigInteger gasPrice = gasPriceResponse.getGasPrice();

		EthEstimateGas estimateGas = web3j.ethEstimateGas(
			org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(fromAddress, null, null, null, contractAddress, encodedData)
		).send();
//		log.info("EthService calculateGas estimateGas {}",JSONUtil.toJsonStr(estimateGas));
		if(estimateGas.hasError()) throw new RuntimeException(chain + " " + estimateGas.getError().getMessage());
		BigInteger gasUsed = estimateGas.getAmountUsed();
		BigInteger safeGas = gasUsed.multiply(BigInteger.valueOf(115)).divide(BigInteger.valueOf(100));
		BigDecimal costInEth = new BigDecimal(gasPrice.multiply(safeGas))
			.divide(BigDecimal.TEN.pow(18), 8, RoundingMode.HALF_UP);

//			log.info("gasPrice = {}, gasUsed = {}, costEth = {}", gasPrice, gasUsed, costInEth);
		return costInEth;

	}
}
