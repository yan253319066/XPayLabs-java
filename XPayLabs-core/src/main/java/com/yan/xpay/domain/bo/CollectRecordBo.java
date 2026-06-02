package com.yan.xpay.domain.bo;

import com.yan.xpay.domain.CollectRecord;
import com.yan.xpay.enums.BlockchainStatus;
import com.yan.xpay.enums.Chain;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 链上归集记录业务对象 t_collect_record
 *
 * @author Yan
 * @date 2025-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CollectRecord.class, reverseConvertGenerate = false)
public class CollectRecordBo extends BaseEntity {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long merchantId;

    /**
     * 
     */
    private Long blockNumber;

    /**
     * 
     */
    private String fromAddress;

    /**
     * 
     */
    private String toAddress;

    /**
     * 
     */
    private Chain chain;

    /**
     * 
     */
    private String symbol;

    /**
     * 
     */
    private BigDecimal amount;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String txId;

    /**
     * 
     */
    private String contractAddress;

    /**
     * 
     */
    private BigDecimal txFee;

    /**
     * 
     */
    private Integer confirmedNum;

    /**
     * 交易状态：PENDING, SUCCESS, FAILED
     */
    private BlockchainStatus status;

    /**
     * 交易区块时间
     */
    private Long blockTime;

    /**
     * 预计收集数量
     */
    private BigDecimal collectAmount;

    /**
     * 平台手续费
     */
    private BigDecimal fee;

    /**
     * 平台手续费率
     */
    private BigDecimal feeRatio;


}
