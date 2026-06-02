package com.yan.user.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yan.user.domain.User;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.user.enums.GoogleStatus;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.service.OssService;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.sensitive.annotation.Sensitive;
import org.dromara.common.sensitive.core.SensitiveStrategy;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户信息视图对象 tb_user
 *
 * @author Yan
 * @date 2025-06-09
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = User.class)
public class UserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 用户编号
     */
    @ExcelProperty(value = "用户编号")
    private String userCode;

    /**
     * 用户账号
     */
    @ExcelProperty(value = "用户账号")
    private String userName;

    /**
     * 用户昵称
     */
    @ExcelProperty(value = "用户昵称")
    private String nickName;

    /**
     * 用户类型
     */
    @ExcelProperty(value = "用户类型")
    private UserType userType;

    /**
     * 用户邮箱
     */
    @Sensitive(strategy = SensitiveStrategy.EMAIL)
    @ExcelProperty(value = "用户邮箱")
    private String email;

    /**
     * 手机区号
     */
    @ExcelProperty(value = "手机区号")
    private String areacode;

    /**
     * 手机号码
     */
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    @ExcelProperty(value = "手机号码")
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @ExcelProperty(value = "用户性别", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /**
     * 头像地址
     */
    @ExcelProperty(value = "头像地址")
    private Long avatar;

    private String avatarUrl;
    private String getAvatarUrl() {
        if(avatar != null){
            OssService ossService = SpringUtils.getBean(OssService.class);
            return ossService.selectUrlByIds(avatar.toString());
        }else return null;
    }

    /**
     * 密码
     */
    @JsonIgnore
    @JsonProperty
    private String password;

    /**
     * VIP等级
     */
    @ExcelProperty(value = "VIP等级")
    private Long vipLevel;

    /**
     * 支付密码
     */
    @JsonIgnore
    @JsonProperty
    private String payPwd;

    /**
     * 邀请码
     */
    @ExcelProperty(value = "邀请码")
    private String inviteCode;

    /**
     * 推荐人ID
     */
    @ExcelProperty(value = "推荐人ID")
    private Long parentId;

    /**
     * 有效推荐人数
     */
    @ExcelProperty(value = "有效推荐人数")
    private Long validRecommend;

    /**
     * 谷歌2fa
     */
    @JsonIgnore
    @JsonProperty
    @ExcelProperty(value = "谷歌2fa")
    private String googleSecretkey;
    /**
     * 谷歌2fa是否绑定 未绑定UNBOUND，绑定BOUND
     */
    @ExcelProperty(value = "谷歌2fa是否绑定")
    private GoogleStatus googleStatus;

    /**
     * 积分
     */
    @ExcelProperty(value = "积分")
    private Long point;

    /**
     * 帐号状态（0正常 1停用）
     */
    @ExcelProperty(value = "帐号状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 最后登录IP
     */
    @ExcelProperty(value = "最后登录IP")
    private String loginIp;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;


}
