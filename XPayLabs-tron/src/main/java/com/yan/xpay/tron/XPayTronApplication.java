package com.yan.xpay.tron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"org.dromara",  "com.yan"})
@EnableScheduling
public class XPayTronApplication {
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(XPayTronApplication.class);
		application.setApplicationStartup(new BufferingApplicationStartup(2048));
		application.run(args);
		System.out.println("(♥◠‿◠)ﾉﾞ  Tron扫描系统启动成功   ლ(´ڡ`ლ)ﾞ");
	}
}
