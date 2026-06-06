package com.yan.blockchain.pay.tron.config;

import com.yan.xpay.enums.Chain;
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
	private Map<Chain, TronChainConfig> chains;

	@Data
	public static class TronChainConfig {
		private String fullNode = "";
		private String solidityNode = "";
		private String hexPrivateKey = "";
		private String apiKey = "";
	}

	@Bean
	@Lazy
	public Map<Chain, ApiWrapper> apiWrappers() {
		Map<Chain, ApiWrapper> wrappers = new EnumMap<>(Chain.class);

		networks.forEach((chain) -> {
			TronChainConfig cfg = chains.get(chain);
			if (cfg != null) {
				wrappers.put(chain, createApiWrapper(chain, cfg));
			}
		});

		log.info("Active TRON chain configs: {}", wrappers.keySet());
		return wrappers;
	}

	private ApiWrapper createApiWrapper(Chain chain, TronChainConfig cfg) {
		return switch (chain) {
			case TRON -> {
				String full = cfg.getFullNode().isEmpty() ? "grpc.trongrid.io:50051" : cfg.getFullNode();
				String solid = cfg.getSolidityNode().isEmpty() ? "grpc.trongrid.io:50052" : cfg.getSolidityNode();
				yield new ApiWrapper(full, solid, cfg.getHexPrivateKey(), cfg.getApiKey());
			}
			case TRON_TEST -> {
				String full = cfg.getFullNode().isEmpty() ? "grpc.shasta.trongrid.io:50051" : cfg.getFullNode();
				String solid = cfg.getSolidityNode().isEmpty() ? "grpc.shasta.trongrid.io:50052" : cfg.getSolidityNode();
				yield new ApiWrapper(full, solid, cfg.getHexPrivateKey());
			}
			default -> throw new IllegalArgumentException("Unsupported chain: " + chain);
		};
	}
}
