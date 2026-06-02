package com.yan.xpay.domain;

import com.yan.xpay.enums.NotifyType;
import lombok.Data;

/**
 * Webhook callback notification
 */
@Data
public class NotifyPayload {
	/**
	 * Sign
	 */
	private String sign;
	/**
	 * timestamp
	 */
	private Long timestamp;
	/**
	 * nonce
	 */
	private String nonce;

	/**
	 * notify type
	 */
	private NotifyType notifyType;

	/**
	 * data
	 */
	private Object data;

	public NotifyPayload(){

	}

	public NotifyPayload(NotifyType type, Object data) {
		this.notifyType = type;
		this.data = data;
	}
}
