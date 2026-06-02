package com.yan.user.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.yan.user.enums.GoogleStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.mybatis.core.domain.BaseUserEntity;

import java.io.Serial;

/**
 * 用户信息对象 tb_user
 *
 * @author Yan
 * @date 2025-06-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseUserEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 用户编号
     */
    private String userCode;

    /**
     * 用户账号
     */
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
     * 有效推荐人数
     */
    private Long validRecommend;

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
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

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
