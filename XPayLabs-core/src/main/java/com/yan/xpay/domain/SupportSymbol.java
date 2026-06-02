package com.yan.xpay.domain;

import com.yan.xpay.enums.Chain;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Supported Symbol
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
public class SupportSymbol implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Symbol，EX. USDT、BTC、ETH
     */
    private String symbol;

    /**
     * Chain，EX. TRON、ETH、BTC、BSC
     */
    private Chain chain;

    /**
     * Token contract address (with the main currency being empty)
     */
    private String contractAddress;

    /**
     * Decimals
     */
    private Integer decimals;


}
