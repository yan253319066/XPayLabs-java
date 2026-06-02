package com.yan.login.domain.model;

import org.dromara.common.core.enums.GrantType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登录对象
 *
 * @author Lion Li
 */

@Data
public class UserLoginBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 授权类型
     *
     */
    @NotNull(message = "{auth.grant.type.not.blank}")
    private GrantType grantType;

    /**
     * 验证码
     */
    private String code;

    /**
     * 唯一标识
     */
    private String uuid;

}
