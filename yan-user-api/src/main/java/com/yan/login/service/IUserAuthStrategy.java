package com.yan.login.service;

import cn.hutool.core.util.StrUtil;
import com.yan.login.domain.vo.LoginUserVo;
import org.dromara.common.core.enums.GrantType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.system.domain.vo.SysClientVo;

/**
 * 用户端授权策略
 *
 * @author Michelle.Chung
 */
public interface IUserAuthStrategy {

    String PREFIX = "user";
    String BASE_NAME = "AuthStrategy";

    /**
     * 登录
     *
     * @param body      登录对象
     * @param client    授权管理视图对象
     * @param grantType 授权类型
     * @return 登录验证信息
     */
    static LoginUserVo login(String body, SysClientVo client, GrantType grantType) {
        // 授权类型和客户端id
        String beanName = PREFIX + StrUtil.upperFirst(grantType.name().toLowerCase()) + BASE_NAME;
        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("Authorization type is incorrect");
        }
        IUserAuthStrategy instance = SpringUtils.getBean(beanName);
        return instance.login(body, client);
    }

    /**
     * 登录
     *
     * @param body   登录对象
     * @param client 授权管理视图对象
     * @return 登录验证信息
     */
    LoginUserVo login(String body, SysClientVo client);

}
