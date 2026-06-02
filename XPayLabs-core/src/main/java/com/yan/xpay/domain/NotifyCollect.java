package com.yan.xpay.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Order Response
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NotifyCollect {

    /**
     * Merchant's estimated withdrawal amount
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal collectAmount;
    /**
     * Service charge
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal fee;
    /**
     * Fee Ratio
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal feeRatio;

    /**
     * FAILED reason
     */
    private String reason;

    private Transaction transaction;

}
