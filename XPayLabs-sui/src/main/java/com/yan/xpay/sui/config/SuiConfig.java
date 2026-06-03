package com.yan.xpay.sui.config;

import com.yan.xpay.enums.Chain;
import com.yan.xpay.mapper.BlockHeightTrackerMapper;
import com.yan.xpay.mapper.ErrorBlockMapper;
import com.yan.xpay.sui.service.BlockProcessorService;
import com.yan.xpay.sui.task.SuiScanTask;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Data
@Configuration
@ConfigurationProperties(prefix = "sui")
public class SuiConfig {
	private List<Chain> networks;
	private String serviceUrl;
	private String encryptionKey;

	@Bean
	public SuiScanTask suiScanTask(
		SuiConfig config,
		BlockHeightTrackerMapper trackerMapper,
		ErrorBlockMapper errorMapper,
		BlockProcessorService processor,
		Executor threadPoolTaskExecutor) {
		return new SuiScanTask(
			config, trackerMapper, errorMapper, processor,
			threadPoolTaskExecutor
		);
	}

//	@Bean
//	public SuiScanTask suiScanTaskTest(
//		SuiConfig config,
//		BlockHeightTrackerMapper trackerMapper,
//		ErrorBlockMapper errorMapper,
//		BlockProcessorService processor,
//		Executor threadPoolTaskExecutor) {
//		return new SuiScanTask(
//			config, trackerMapper, errorMapper, processor,
//			threadPoolTaskExecutor
//		);
//	}


	// -----------------------------
	// 配置限流参数
	// -----------------------------

	private final int rps = 200;
	private final int maxConcurrent = 40; // 最大并发
	private final Semaphore semaphore = new Semaphore(rps);
	private final Semaphore concurrencySemaphore = new Semaphore(maxConcurrent);
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

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
			throw new RuntimeException("SUI RPC 调用被中断", e);
		} catch (Exception e) {
			throw new RuntimeException("SUI RPC 调用失败", e);
		} finally {
			concurrencySemaphore.release();
		}
	}

	@PreDestroy
	public void shutdown() {
		scheduler.shutdown();
	}
}
