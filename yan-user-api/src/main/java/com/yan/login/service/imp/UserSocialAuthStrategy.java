package com.yan.login.service.imp;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.yan.login.domain.model.UserSocialLoginBody;
import com.yan.login.domain.vo.LoginUserVo;
import com.yan.login.service.IUserAuthStrategy;
import com.yan.login.service.UserLoginService;
import com.yan.user.domain.vo.SocialVo;
import com.yan.user.domain.vo.UserVo;
import com.yan.user.mapper.UserMapper;
import com.yan.user.service.ISocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.social.config.properties.SocialProperties;
import org.dromara.common.social.utils.SocialUtils;
import org.dromara.system.domain.vo.SysClientVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 第三方授权策略
 *
 * @author thiszhc is 三三
 */
@Slf4j
@Service(IUserAuthStrategy.PREFIX + "Social" + IUserAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class UserSocialAuthStrategy implements IUserAuthStrategy {

    private final SocialProperties socialProperties;
    private final ISocialService sysSocialService;
    private final UserMapper userMapper;
    private final UserLoginService userLoginService;

    /**
     * 登录-第三方授权登录
     *
     * @param body     登录信息
     * @param client   客户端信息
     */
    @Override
    public LoginUserVo login(String body, SysClientVo client) {
        UserSocialLoginBody loginBody = JsonUtils.parseObject(body, UserSocialLoginBody.class);
        ValidatorUtils.validate(loginBody);
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(
                loginBody.getSource(), loginBody.getSocialCode(),
                loginBody.getSocialState(), socialProperties);
        if (!response.ok()) {
            throw new ServiceException(response.getMsg());
        }
        AuthUser authUserData = response.getData();
//        if ("GITEE".equals(authUserData.getSource())) {
//            // 如用户使用 gitee 登录顺手 star 给作者一点支持 拒绝白嫖
//            HttpUtil.createRequest(Method.PUT, "https://gitee.com/api/v5/user/starred/dromara/RuoYi-Vue-Plus")
//                    .formStr(MapUtil.of("access_token", authUserData.getToken().getAccessToken()))
//                    .executeAsync();
//            HttpUtil.createRequest(Method.PUT, "https://gitee.com/api/v5/user/starred/dromara/RuoYi-Cloud-Plus")
//                    .formStr(MapUtil.of("access_token", authUserData.getToken().getAccessToken()))
//                    .executeAsync();
//        }

        List<SocialVo> list = sysSocialService.selectByAuthId(authUserData.getSource() + authUserData.getUuid());
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("你还没有绑定第三方账号，绑定后才可以登录！");
        }
        SocialVo social = list.get(0);

        UserVo user = loadUser(social.getUserId());
        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        LoginUser loginUser = userLoginService.buildLoginUser(user);

        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType(client.getDeviceType().name());
        // 自定义分配 不同用户体系 不同 token 授权时间 不设置默认走全局 yml 配置
        // 例如: 后台用户30分钟过期 app用户1天过期
        model.setTimeout(client.getTimeout());
        model.setActiveTimeout(client.getActiveTimeout());
        model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());
        // 生成token
        LoginHelper.login(loginUser, model);

        LoginUserVo loginUserVo = new LoginUserVo();
        loginUserVo.setAccessToken(StpUtil.getTokenValue());
        loginUserVo.setExpireIn(StpUtil.getTokenTimeout());
        loginUserVo.setClientId(client.getClientId());
        return loginUserVo;
    }

    private UserVo loadUser(Long userId) {
        UserVo user = userMapper.selectVoById(userId);
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", "");
            throw new UserException("user.not.exists", "");
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", "");
            throw new UserException("user.blocked", "");
        }
        return user;
    }

}
