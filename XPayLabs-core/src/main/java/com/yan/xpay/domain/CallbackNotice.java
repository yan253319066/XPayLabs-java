package com.yan.xpay.domain;


import com.yan.xpay.enums.NotifyStatus;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.util.Date;

@Data
@TableName("t_callback_notice")
public class CallbackNotice {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 回调URL
     */
    private String callbackUrl;

    /**
     * 回调通知状态
     */
    private NotifyStatus notifyStatus;

    /**
     * 通知时间
     */
    private Date notifyTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
