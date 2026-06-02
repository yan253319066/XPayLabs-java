package com.yan.config;

import lombok.extern.slf4j.Slf4j;
import org.dromara.system.service.ISysOssConfigService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OssConfig {
	public OssConfig (ISysOssConfigService ossConfigService) {
		ossConfigService.init();
		log.info("初始化OSS配置成功");
	}
}
