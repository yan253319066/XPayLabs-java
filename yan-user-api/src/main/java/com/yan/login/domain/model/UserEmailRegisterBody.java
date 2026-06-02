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
public class UserEmailRegisterBody extends UserLoginBody {

    /**
     * 邮箱
     */
    @NotBlank(message = "{user.email.not.blank}")
    @Length(min = 2, max = 30, message = "{user.email.length.valid}")
    private String email;

    /**
     * 用户密码
     */
    @NotBlank(message = "{user.password.not.blank}")
    @Length(min = 5, max = 30, message = "{user.password.length.valid}")
    private String password;

    /**
     * 邮箱验证码
     */
    @NotBlank(message = "{user.emailCode.not.blank}")
    private String emailCode;

    /**
     * 父邀请码
     */
    private String inviteCode;

}
