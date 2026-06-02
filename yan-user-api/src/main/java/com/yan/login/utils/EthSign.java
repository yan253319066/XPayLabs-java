package com.yan.login.utils;

import org.web3j.crypto.*;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * 以太坊签名消息校验工具
 */
public class EthSign {
	/**
	 * 以太坊自定义的签名消息都以以下字符开头 参考 eth_sign in
	 * https://github.com/ethereum/wiki/wiki/JSON-RPC
	 */
	public static final String PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

	public static void main(String[] args) {
		// 签名后的数据
		String signature = "0xca58c81d224776f27a2ad62f17575d0cbc8efa621cab87be0dd8e7c00879e69a2fb673715a17bf68762035c2cdff2df330838456ebee0753a3bd62f091979a2f1b";
		// 签名原文
		String message = "Hello world";
		// 签名的钱包地址
		String address = "0xD0Aca4b00822bE7AD302dc0E2A72a528581f446F".toLowerCase();
		Boolean result = validate(signature, message, address);
		System.out.println(result);
	}

	/**
	 * 对签名消息，原始消息，账号地址三项信息进行认证，判断签名是否有效
	 var sha3Msg = web3.utils.sha3(msg);
        console.log(sha3Msg)
        signedData = await web3.eth.personal.sign(sha3Msg, this.walletAddress);
        console.log("signedData",signedData)
        
        // ethers签名文本消息
        let message = "My name is APP.";
        let signature = await signer.signMessage(message)
	 * 
	 * @param signature
	 * @param message
	 * @param address
	 * @return
	 */
	public static boolean validate(String signature, String message, String address) {
		// 参考 eth_sign in https://github.com/ethereum/wiki/wiki/JSON-RPC
		// eth_sign
		// The sign method calculates an Ethereum specific signature with:
		// sign(keccak256("\x19Ethereum Signed Message:\n" + len(message) + message))).
		//
		// By adding a prefix to the message makes the calculated signature recognisable
		// as an Ethereum specific signature.
		// This prevents misuse where a malicious DApp can sign arbitrary data (e.g.
		// transaction) and use the signature to
		// impersonate the victim.
		String prefix = PERSONAL_MESSAGE_PREFIX + message.length();
		byte[] msgHash = Hash.sha3((prefix + message).getBytes());

		byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
		byte v = signatureBytes[64];
		if (v < 27) {
			v += 27;
		}

		SignatureData sd = new SignatureData(v, Arrays.copyOfRange(signatureBytes, 0, 32),
				Arrays.copyOfRange(signatureBytes, 32, 64));

		String addressRecovered = null;
		boolean match = false;

		// Iterate for each possible key to recover
		for (int i = 0; i < 4; i++) {
			BigInteger publicKey = Sign.recoverFromSignature((byte) i,
					new ECDSASignature(new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())), msgHash);
			if (publicKey != null) {
				addressRecovered = "0x" + Keys.getAddress(publicKey);
				if (addressRecovered.equals(address)) {
					match = true;
					break;
				}
			}
		}
		return match;
	}
}