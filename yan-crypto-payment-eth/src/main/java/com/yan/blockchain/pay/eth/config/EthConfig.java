package com.yan.blockchain.pay.eth.config;

import com.yan.blockchain.pay.eth.service.EthService;
import com.yan.xpay.enums.Chain;
import com.yan.blockchain.pay.eth.service.BlockProcessorService;
import com.yan.blockchain.pay.eth.EvmBlockScanner;
import com.yan.blockchain.pay.eth.service.Web3jProviderService;
import com.yan.xpay.mapper.BlockHeightTrackerMapper;
import com.yan.xpay.mapper.ErrorBlockMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "eth")
public class EthConfig {

    private List<Chain> networks;

    // 链基础配置类
    @Data
    @AllArgsConstructor
    public static class ChainConfig {
        private List<String> rpcUrls;       // RPC端点列表
        private long scanIntervalSeconds;   // 每条链一个扫描间隔
        private int rpcProviderCount;       // 每条链需要的Web3j实例数量
    }

    @Bean
    public Map<Chain, ChainConfig> chainConfigs(Environment env) {
        Map<Chain, ChainConfig> configs = new HashMap<>();

        networks.forEach((chain) -> {
            switch (chain) {
                case BSC -> {
                    // BSC配置（3个RPC端点，扫描间隔5秒）
                    configs.put(Chain.BSC,  new ChainConfig(
                            Arrays.asList(
                                    "https://bsc-dataseed.bnbchain.org",
                                    "https://bsc-dataseed1.nariox.org",
                                    "https://bsc-dataseed.defibit.io",
                                    "https://bsc-dataseed.ninicoin.io"
                            ),
                            2,   // 扫描间隔
                            4   // Web3j实例数量
                    ));
                    log.info("加载 {} RPC节点", chain);
                }
                case BSC_TEST ->  {
                    configs.put(Chain.BSC_TEST,  new ChainConfig(
                            Arrays.asList(
                                    "https://data-seed-prebsc-1-s1.bnbchain.org:8545",
                                    "https://data-seed-prebsc-2-s1.bnbchain.org:8545",
                                    "https://data-seed-prebsc-1-s2.bnbchain.org:8545",
                                    "https://data-seed-prebsc-2-s2.bnbchain.org:8545",
                                    "https://data-seed-prebsc-1-s3.bnbchain.org:8545",
                                    "https://data-seed-prebsc-2-s3.bnbchain.org:8545"
                            ),
                            2,   // 扫描间隔
                            6   // Web3j实例数量
                    ));
                    log.info("加载 {} RPC节点", chain);
                }
                case ETH ->  {
                    // ETH配置（2个RPC端点，扫描间隔15秒）
                    configs.put(Chain.ETH,  new ChainConfig(
                            Arrays.asList(
                                    "https://0xrpc.io/eth",
                                    "https://rpc.flashbots.net"
                            ),
                            15,  // 扫描间隔
                            1    // Web3j实例数量
                    ));
                    log.info("加载 {} RPC节点", chain);
                }
                case ETH_SEPOLIA ->  {
                    configs.put(Chain.ETH_SEPOLIA,  new ChainConfig(
                            Arrays.asList(
                                    "https://eth-sepolia.g.alchemy.com/v2/uSj84PzV5eyb-g-vnZyq1",
                                    "https://rpc.ankr.com/eth_sepolia/2222a49d1f0845ea6f0ebb650e5de491bc54edff012536b1a7e7e19ec6c5cb80"
                            ),
                            5,  // 扫描间隔
                            2    // Web3j实例数量
                    ));
                    log.info("加载 {} RPC节点", chain);
                }
                case POLYGON -> {
                    configs.put(Chain.POLYGON,  new ChainConfig(
                            Arrays.asList(
                                    "https://polygon-mainnet.rpcfast.com?api_key=xbhWBI1Wkguk8SNMu1bvvLurPGLXmgwYeC4S6g2H7WdwFigZSmPWVZRxrskEQwIf",
                                    "https://rpc.sentio.xyz/matic",
                                    "https://api.zan.top/polygon-mainnet",
                                    "https://poly.api.pocket.network",
                                    "https://go.getblock.io/02667b699f05444ab2c64f9bff28f027"
                            ), 2, 5
                    ));
                    log.info("加载 {} RPC节点", chain);
                }
                case POLYGON_AMOY -> {
                    configs.put(Chain.POLYGON_AMOY,  new ChainConfig(
                            Arrays.asList("https://rpc-amoy.polygon.technology"
                            ), 2, 1
                    ));
                    log.info("加载 {} RPC节点", chain);
                }
                case AVAX_C_CHAIN -> {
                    configs.put(Chain.AVAX_C_CHAIN,  new ChainConfig(
                            Arrays.asList("https://api.avax.network/ext/bc/C/rpc"
                            ), 2, 1
                    ));
                    log.info("加载 {} RPC节点", chain);
                }
                case AVAX_FUJI_TEST -> {
                    configs.put(Chain.AVAX_FUJI_TEST,  new ChainConfig(
                            Arrays.asList("https://api.avax-test.network/ext/bc/C/rpc"
                            ), 2, 1
                    ));
                    log.info("加载 {} RPC节点", chain);
                }
            }
        });

        return configs;
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
