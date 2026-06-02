package com.yan.login.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * 用户注册对象
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPhoneRegisterBody extends UserLoginBody {

    /**
     * 手机号区号
     */
    private String areacode;

    /**
     * 电话
     */
    @NotBlank(message = "{user.phone.not.blank}")
    @Length(min = 2, max = 30, message = "{user.phone.length.valid}")
    private String phone;

    /**
     * 用户密码
     */
    @NotBlank(message = "{user.password.not.blank}")
    @Length(min = 5, max = 30, message = "{user.password.length.valid}")
    private String password;

    /**
     * 短信验证码
     */
    @NotBlank(message = "{user.smsCode.not.blank}")
    private String smsCode;

    /**
     * 父邀请码
     */
    private String inviteCode;

}
