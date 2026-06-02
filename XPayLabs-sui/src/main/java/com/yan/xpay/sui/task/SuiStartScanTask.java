package com.yan.xpay.sui.task;

import com.yan.xpay.enums.Chain;
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

	private final SuiScanTask suiScanTaskMain;
	private final SuiScanTask suiScanTaskTest;
	private final ScheduledExecutorService scheduledExecutorService;

	@EventListener(ApplicationReadyEvent.class)
	public void startOnReady() {
		// SUI 主网扫描
		scheduledExecutorService.scheduleAtFixedRate(
			() -> suiScanTaskMain.scanBlocks(Chain.SUI),
			1, 1, TimeUnit.SECONDS
		);
		log.info("开启[{}]扫描", Chain.SUI);

		// SUI 测试网扫描
		scheduledExecutorService.scheduleAtFixedRate(
			() -> suiScanTaskTest.scanBlocks(Chain.SUI_TEST),
			1, 1, TimeUnit.SECONDS
		);
		log.info("开启[{}]扫描", Chain.SUI_TEST);
	}
}
