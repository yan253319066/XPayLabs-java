package com.yan.xpay.domain.bo;

import com.yan.xpay.domain.TxRecord;
import com.yan.xpay.enums.BlockchainStatus;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.TxType;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 链上交易记录业务对象 t_tx_record
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@AutoMapper(target = TxRecord.class, reverseConvertGenerate = false)
public class TxRecordBo {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 对应订单ID
     */
    @NotNull(message = "对应订单ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderId;

    private Long blockNumber;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fromAddress;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String toAddress;

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Chain chain;

    private String symbol;

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal amount;

    /**
     * 
     */
    private String txId;
    private String feeTxId;

    private String contractAddress;

    /**
     * 交易类型：COLLECTION,PAYOUT
     */
    private TxType txType;

    private BigDecimal txFee;

    private Integer confirmedNum;

    /**
     * 交易状态
     */
    private BlockchainStatus status;

    /**
     * 交易区块时间
     */
    private Long blockTime;

}
