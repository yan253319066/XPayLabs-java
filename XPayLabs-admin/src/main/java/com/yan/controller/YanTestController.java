package com.yan.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端测试
 */
@SaIgnore
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
public class YanTestController {
	@GetMapping("/test")
	public R<Void> test() {
		log.info("master test");
		return R.ok("Hello.");
	}
}
