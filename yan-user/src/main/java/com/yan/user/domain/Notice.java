package com.yan.user.domain;


import com.yan.user.enums.ReadStatus;
import org.dromara.common.core.enums.Status;
import org.dromara.common.mybatis.core.domain.BaseUserEntity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_notice")
public class Notice extends BaseUserEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    private String noticeType;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 公告状态
     */
    private Status status;

    /**
     * 是否已读
     */
    private ReadStatus readStatus;

    /**
     * 备注
     */
    private String remark;


}
