package com.yan.xpay.sui.model;

import lombok.Data;

import java.util.List;

@Data
public class SuiTransactionBlocks {
	private List<SuiTransaction> data;
	private String nextCursor;
	private boolean hasNextPage;
}
