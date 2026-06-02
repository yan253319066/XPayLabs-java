package com.yan.blockchain.pay.config;

import cn.hutool.core.codec.Base64;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "fiat-currency")
public class FiatCurrencyConfig {
	private Jdpay jdpay = new Jdpay();

	@Data
	public static class Jdpay {
		private String merchantNo;
		private String publicKey;
		private String privateKey;
		private String notifyInUrl;
		private String notifyOutUrl;

		public String getDecodePrivateKey(){
			String decode = Base64.decodeStr(this.privateKey);
			return decode;
		}
	}

	public static void main(String[] args) {
		String privateKey = """
-----BEGIN PRIVATE KEY-----

-----END PRIVATE KEY-----
		""".replace("-----BEGIN PRIVATE KEY-----", "")
			.replace("-----END PRIVATE KEY-----", "")
			.replaceAll("\\s", "");
		String s = Base64.encode(privateKey);
		System.out.println(s);
		String s2 = Base64.decodeStr(s);
		System.out.println(s2);


	String publicKey = """
-----BEGIN PUBLIC KEY-----

-----END PUBLIC KEY-----
		""".replace("-----BEGIN PUBLIC KEY-----", "")
		.replace("-----END PUBLIC KEY-----", "")
		.replaceAll("\\s", "");
	String encodePublicKey = Base64.encode(publicKey);
		System.out.println(encodePublicKey);
	String encodePublicKey2 = Base64.decodeStr(encodePublicKey);
		System.out.println(encodePublicKey2);
	}

}
