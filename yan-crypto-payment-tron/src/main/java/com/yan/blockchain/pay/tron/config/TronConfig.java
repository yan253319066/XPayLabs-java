package com.yan.blockchain.pay.tron.config;

import com.yan.xpay.enums.Chain;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.tron.trident.core.ApiWrapper;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "tron")
public class TronConfig {
	private List<Chain> networks;
	private BigDecimal txFee = BigDecimal.valueOf(15);
	private String energyApikey;

//	@PostConstruct
//	public void init() {
//		log.info("Energy  API Key loaded: {}", energyApikey);
//	}

	@Bean
	@Lazy
	public Map<Chain, ApiWrapper> apiWrappers() {
		Map<Chain, ApiWrapper> wrappers = new EnumMap<>(Chain.class);

		networks.forEach((chain) -> {
			wrappers.put(chain,  createApiWrapper(chain));
		});

		return wrappers;
	}

	private ApiWrapper createApiWrapper(Chain chain) {
		switch (chain) {
			case TRON:
				return ApiWrapper.ofMainnet("", "6ae37058-7809-45c8-b86c-2b5799d19acd");
			case TRON_TEST:
				return ApiWrapper.ofShasta("");
			default:
				throw new IllegalArgumentException("Unsupported chain: " + chain);
		}
	}
}
