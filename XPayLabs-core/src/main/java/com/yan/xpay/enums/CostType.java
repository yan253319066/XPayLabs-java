package com.yan.xpay.enums;

import org.dromara.common.core.utils.MessageUtils;

public enum CostType {
	/**
	 * 收款
	 */
	PAYIN("cost.type.payin"),
	/**
	 * 付款
	 */
	PAYOUT("cost.type.payout"),
	/**
	 * PAYOUT GAS费
	 */
	PAYOUT_GAS("cost.type.payout.gas"),
	/**
	 * COLLECTED GAS费
	 */
	COLLECTED_GAS("cost.type.collected.gas"),
	/**
	 * 归集时转gas费
	 */
	SEND_GAS("cost.type.send.gas"),

	/**
	 * 归集的
	 */
	COLLECTED("cost.type.collected"),

	WITHDRAWAL_GAS("cost.type. withdrawal.gas")
	;

	private final String i18nKey;

	CostType(String i18nKey) {
		this.i18nKey = i18nKey;
	}

	public String getDisplayName() {
		return MessageUtils.message(this.i18nKey);
	}
}
