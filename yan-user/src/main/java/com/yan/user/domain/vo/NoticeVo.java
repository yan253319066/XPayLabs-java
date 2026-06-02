package com.yan.user.domain.vo;

import com.yan.user.domain.Notice;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.user.enums.ReadStatus;
import org.dromara.common.core.enums.Status;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 用户通知公告视图对象 tb_notice
 *
 * @author Yan
 * @date 2025-10-26
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Notice.class)
public class NoticeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    @ExcelProperty(value = "公告ID")
    private Long id;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 公告标题
     */
    @ExcelProperty(value = "公告标题")
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    @ExcelProperty(value = "公告类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=通知,2=公告")
    private String noticeType;

    /**
     * 公告内容
     */
    @ExcelProperty(value = "公告内容")
    private String noticeContent;

    /**
     * 公告状态
     */
    @ExcelProperty(value = "公告状态")
    private Status status;

    /**
     * 是否已读
     */
    @ExcelProperty(value = "是否已读")
    private ReadStatus readStatus;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
