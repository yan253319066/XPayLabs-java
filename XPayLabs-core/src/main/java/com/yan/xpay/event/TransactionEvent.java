package com.yan.xpay.event;

import com.yan.xpay.domain.Transaction;
import lombok.Data;

@Data
public class TransactionEvent {
	private Transaction tx;
}
