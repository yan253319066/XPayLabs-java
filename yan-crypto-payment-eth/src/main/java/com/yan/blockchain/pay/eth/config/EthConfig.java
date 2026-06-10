package com.yan.blockchain.pay.eth.config;

import com.yan.blockchain.pay.eth.service.EthService;
import com.yan.xpay.enums.Chain;
import com.yan.blockchain.pay.eth.service.BlockProcessorService;
import com.yan.blockchain.pay.eth.EvmBlockScanner;
import com.yan.blockchain.pay.eth.service.Web3jProviderService;
import com.yan.xpay.mapper.BlockHeightTrackerMapper;
import com.yan.xpay.mapper.ErrorBlockMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "eth")
public class EthConfig {

    private List<Chain> networks;
    private Map<Chain, ChainConfig> chains;

    // 链基础配置类
    @Data
    public static class ChainConfig {
        private List<String> rpcUrls;       // RPC端点列表
        private long scanIntervalSeconds;   // 每条链一个扫描间隔
    }

    @Bean
    public Map<Chain, ChainConfig> chainConfigs() {
        Map<Chain, ChainConfig> active = new HashMap<>();
        networks.forEach(chain -> {
            ChainConfig cfg = chains.get(chain);
            if (cfg != null) {
                active.put(chain, cfg);
            }
        });
        log.info("Active chain configs (filtered by networks): {}", active.keySet());
        return active;
    }

    @Bean
    public EvmBlockScanner evmBlockScanner(
            BlockHeightTrackerMapper blockHeightTrackerMapper,
            BlockProcessorService blockProcessorService,
            EthService ethService,
            ErrorBlockMapper errorBlockMapper) {
        log.info("EvmBlockScanner loading completed");
        return new EvmBlockScanner(
                blockHeightTrackerMapper,
                blockProcessorService,
                ethService,
                errorBlockMapper
        );
    }

    @Bean
    public Web3jProviderService web3jProviderService(Map<Chain, ChainConfig> chainConfigs) {
        log.info("Web3jProviderService loading completed");
        return new Web3jProviderService(chainConfigs);
    }
}
