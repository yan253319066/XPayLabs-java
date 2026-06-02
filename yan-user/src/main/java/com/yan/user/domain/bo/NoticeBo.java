package com.yan.user.domain.bo;

import com.yan.user.domain.Notice;
import com.yan.user.enums.ReadStatus;
import org.dromara.common.core.enums.Status;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 用户通知公告业务对象 tb_notice
 *
 * @author Yan
 * @date 2025-10-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Notice.class, reverseConvertGenerate = false)
public class NoticeBo extends BaseEntity {

    /**
     * 公告ID
     */
    @NotNull(message = "公告ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空", groups = { AddGroup.class, EditGroup.class })
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    @NotBlank(message = "公告类型（1通知 2公告）不能为空", groups = { AddGroup.class, EditGroup.class })
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
