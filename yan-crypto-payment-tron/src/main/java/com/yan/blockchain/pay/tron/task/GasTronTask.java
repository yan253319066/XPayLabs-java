package com.yan.blockchain.pay.tron.task;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.Constant;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.utils.ByteArray;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GasTronTask {
	public static void main(String[] args) {
		String privateKey = "";
		ApiWrapper client = ApiWrapper.ofMainnet(privateKey);
		// transfer(address,uint256) returns (bool)
		String usdtAddr = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
		String fromAddr = client.keyPair.toBase58CheckAddress();
		String toAddress = "TA1gifHJsxC2NyLapuvMgBPPvGaFcR1DSR";
		Function trc20Transfer = new Function("transfer",
			Arrays.asList(new Address(toAddress),
				new Uint256(BigInteger.valueOf(10).multiply(BigInteger.valueOf(10).pow(6)))),
			Arrays.asList(new TypeReference<Bool>() {
			}));

		String encodedHex = FunctionEncoder.encode(trc20Transfer);
		log.info("{}", encodedHex);
		estimateUSDTEnergy(fromAddr,usdtAddr, encodedHex);

		Contract.TriggerSmartContract trigger =
			Contract.TriggerSmartContract.newBuilder()
				.setOwnerAddress(ApiWrapper.parseAddress(fromAddr))
				.setContractAddress(ApiWrapper.parseAddress(usdtAddr))
				.setData(ByteString.copyFrom(ByteArray.fromHexString(encodedHex)))
				.build();

		Response.TransactionExtention txnExt = client.blockingStub.triggerConstantContract(trigger);

		Response.EstimateEnergyMessage energyMessage = client.estimateEnergyV2(fromAddr, usdtAddr, encodedHex);
		System.out.println(energyMessage.getEnergyRequired());

//		Chain.Transaction signedTxn = client.signTransaction(txnExt);
//		System.out.println("signedTxn => "+signedTxn.toString());



		//		// ✅ 初始化 Wrapper（主网 / 测试网均可）
//		String privateKey = "";
//		ApiWrapper wrapper = ApiWrapper.ofMainnet(privateKey);
//		// 也可以：ApiWrapper.ofShasta(privateKey);
//
//		// ✅ 设置合约地址（TRC20 USDT）和参数
//		String ownerAddr = "TAU6r7DuC7piesvmhegHPiV6riQBNpE7o2";
//		String contractAddr = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"; // 主网USDT
//		String toAddr = "TWpQquev2Qhs3v7VFJyZj9GVbR4YhhETYS";
//		BigInteger amount = BigInteger.valueOf(1_000_000); // 1 USDT = 10^6
//
//		// ✅ 构造函数
//		Function function = new Function(
//			"transfer",
//			Arrays.asList(new Address(toAddr), new Uint256(amount)),
//			Arrays.asList(new TypeReference<Bool>() {})
//		);
//
		// ✅ 调用 estimateEnergy()
//		Response.EstimateEnergyMessage result = client.estimateEnergy(fromAddr, usdtAddr, trc20Transfer);
//		// ✅ 打印能量估算结果
//		System.out.println("Energy used: " + result.getEnergyRequired());
//		System.out.println("Result: " + JSONUtil.toJsonStr(result));
//		System.out.println("Result: " + JSONUtil.toJsonStr(result.getResult()));
	}

	/**
	 * 使用 estimateenergy API 估算能量消耗
	 */
	public static int estimateUSDTEnergy(String fromAddress, String contractAddress, String parameter) {

		String body = "{\n" +
			"  \"owner_address\":\"" +fromAddress+  "\",\n" +
			"  \"contract_address\": \"" + contractAddress +  "\",\n" +
			"  \"function_selector\": \"transfer(address,uint256)\",\n" +
			"  \"parameter\": \"" + parameter + "\",\n" +
			"  \"visible\": true\n" +
			"}";
		log.info("body {}", body);
		cn.hutool.http.HttpResponse response = cn.hutool.http.HttpRequest.post("https://api.trongrid.io/wallet/triggerconstantcontract")
			.contentType("application/json")
			.body(body)
			.execute();
		log.info("{}", response);


		return 64285; // 默认值
	}

}
