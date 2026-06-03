package com.yan.xpay.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yan.xpay.domain.CallbackNotice;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.NotifyStatus;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 回调通知视图对象 t_callback_notice
 *
 * @author Yan
 * @date 2026-06-02
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CallbackNotice.class)
public class CallbackNoticeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 订单ID
     */
    @ExcelProperty(value = "订单ID")
    private Long orderId;

    /**
     * 回调URL
     */
    @ExcelProperty(value = "回调URL")
    private String callbackUrl;

    /**
     * 回调通知状态
     */
    @ExcelProperty(value = "回调通知状态")
    private NotifyStatus notifyStatus;

    /**
     * 通知时间
     */
    @ExcelProperty(value = "通知时间")
    private Date notifyTime;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;


}
