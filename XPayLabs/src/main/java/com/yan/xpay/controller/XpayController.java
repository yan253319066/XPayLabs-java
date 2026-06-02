package com.yan.xpay.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * welcome
 */
@Validated
@RequiredArgsConstructor
@RestController
public class XpayController {
	@GetMapping("/")
	public String hello(){
		return "Hello XPayLabs";
	}
	@GetMapping("/favicon.ico")
	public String favicon(){
		return "favicon.ico";
	}
}
