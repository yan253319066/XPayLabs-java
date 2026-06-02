package com.yan.xpay.domain;

import com.yan.xpay.enums.Chain;
import lombok.Data;

import java.io.Serial;

@Data
public class GeneralHotAddress {
	@Serial
	private static final long serialVersionUID = 1L;
	private Chain chain;
	private String hotAddress;
	private String keystore;
	private String encrypt;

}
