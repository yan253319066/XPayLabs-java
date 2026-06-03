package com.yan.xpay.sui.task;

import com.yan.xpay.sui.config.SuiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuiStartScanTask {

	private final SuiScanTask suiScanTask;
	private final ScheduledExecutorService scheduledExecutorService;
    private final SuiConfig suiConfig;

	@EventListener(ApplicationReadyEvent.class)
	public void startOnReady() {
        suiConfig.getNetworks().forEach(network -> {
            scheduledExecutorService.scheduleAtFixedRate(
                    () -> suiScanTask.scanBlocks(network),
                    1, 1, TimeUnit.SECONDS
            );
            log.info("开启[{}]扫描", network);
        });
	}
}
