package com.yan.user.domain.bo;

import com.yan.user.domain.User;
import com.yan.user.enums.GoogleStatus;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;
import org.dromara.common.mybatis.core.domain.BaseUserEntity;

/**
 * 用户信息业务对象 tb_user
 *
 * @author Yan
 * @date 2025-06-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = User.class, reverseConvertGenerate = false)
public class UserBo extends BaseUserEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户编号
     */
    private String userCode;

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户类型
     */
    private UserType userType;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机区号
     */
    private String areacode;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 头像地址
     */
    private Long avatar;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空", groups = { AddGroup.class})
    private String password;

    /**
     * VIP等级
     */
    private Long vipLevel;

    /**
     * 支付密码
     */
    private String payPwd;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 推荐人ID
     */
    private Long parentId;

    /**
     * 推荐人IDs
     */
    private String referrerIds;

    /**
     * 谷歌2fa
     */
    private String googleSecretkey;
    /**
     * 谷歌2fa是否绑定 未绑定UNBOUND，绑定BOUND
     */
    private GoogleStatus googleStatus;

    /**
     * 积分
     */
    private Long point;

    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private Date loginDate;

    /**
     * 备注
     */
    private String remark;


}
