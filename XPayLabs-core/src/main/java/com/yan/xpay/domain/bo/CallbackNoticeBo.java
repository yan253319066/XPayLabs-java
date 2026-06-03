package com.yan.xpay.domain.bo;

import com.yan.xpay.domain.CallbackNotice;
import com.yan.xpay.enums.NotifyStatus;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 回调通知业务对象 t_callback_notice
 *
 * @author Yan
 * @date 2026-06-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CallbackNotice.class, reverseConvertGenerate = false)
public class CallbackNoticeBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long orderId;

    /**
     * 回调URL
     */
    @NotBlank(message = "回调URL不能为空", groups = { AddGroup.class, EditGroup.class })
    private String callbackUrl;

    /**
     * 回调通知状态
     */
    @NotBlank(message = "回调通知状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private NotifyStatus notifyStatus;

    /**
     * 通知时间
     */
    @NotNull(message = "通知时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date notifyTime;


}
