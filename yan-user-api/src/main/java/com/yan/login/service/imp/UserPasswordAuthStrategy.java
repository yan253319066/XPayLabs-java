package com.yan.login.service.imp;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.login.domain.model.UserEmailLoginBody;
import com.yan.login.domain.model.UserLoginBody;
import com.yan.login.domain.model.UserPasswordLoginBody;
import com.yan.login.domain.model.UserPhoneLoginBody;
import com.yan.login.domain.vo.LoginUserVo;
import com.yan.login.service.UserLoginService;
import com.yan.login.utils.ValidateCode;
import com.yan.login.service.IUserAuthStrategy;
import com.yan.user.domain.User;
import com.yan.user.domain.vo.UserVo;
import com.yan.user.enums.ValidateCodeType;
import com.yan.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.enums.LoginType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.dromara.system.domain.vo.SysClientVo;
import org.springframework.stereotype.Service;

/**
 * 密码认证策略
 *
 * @author Michelle.Chung
 */
@Slf4j
@Service(IUserAuthStrategy.PREFIX + "Password" + IUserAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class UserPasswordAuthStrategy implements IUserAuthStrategy {

    private final CaptchaProperties captchaProperties;
    private final UserLoginService userLoginService;
    private final UserMapper userMapper;

    @Override
    public LoginUserVo login(String body, SysClientVo client) {
        JSONObject obj = JSONUtil.parseObj(body);
        UserLoginBody userLoginBody = JsonUtils.parseObject(body, UserLoginBody.class);
        ValidatorUtils.validate(userLoginBody);
        LoginUser loginUser;
        UserVo user;
        if(StrUtil.isNotBlank(obj.getStr("username"))) {
            UserPasswordLoginBody loginBody = JsonUtils.parseObject(body, UserPasswordLoginBody.class);
            ValidatorUtils.validate(loginBody);
            String username = loginBody.getUsername();
            String password = loginBody.getPassword();
            String code = loginBody.getCode();
            String uuid = loginBody.getUuid();

            boolean captchaEnabled = captchaProperties.getEnable();
            // 验证码开关
            if (captchaEnabled) {
                ValidateCode.validateCaptcha(username, code, uuid, ValidateCodeType.LOGIN);
            }

            user = loadUserByUsername(username);
            userLoginService.checkLogin(LoginType.PASSWORD, username, () -> !BCrypt.checkpw(password, user.getPassword()));

        }else if(StrUtil.isNotBlank(obj.getStr("phone"))) {
            UserPhoneLoginBody loginBody = JsonUtils.parseObject(body, UserPhoneLoginBody.class);
            ValidatorUtils.validate(loginBody);
            String phonenumber = loginBody.getPhonenumber();
            String password = loginBody.getPassword();

            user = loadUserByPhonenumber(phonenumber);
            userLoginService.checkLogin(LoginType.PASSWORD, phonenumber, () -> !BCrypt.checkpw(password, user.getPassword()));

        }else if(StrUtil.isNotBlank(obj.getStr("email"))) {
            UserEmailLoginBody loginBody = JsonUtils.parseObject(body, UserEmailLoginBody.class);
            ValidatorUtils.validate(loginBody);
            String email = loginBody.getEmail();
            String password = loginBody.getPassword();

            user = loadUserByEmail(email);
            userLoginService.checkLogin(LoginType.PASSWORD, email, () -> !BCrypt.checkpw(password, user.getPassword()));

        }else {
            log.warn("GrantType 认证类型：{} 异常!.", userLoginBody.getGrantType());
            throw new ServiceException(MessageUtils.message("auth.grant.type.error"));
        }

        // 此处可根据登录用户的数据不同 自行创建 loginUser
        loginUser = userLoginService.buildLoginUser(user);

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

    private UserVo loadUserByUsername(String username) {
        UserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<User>().eq(User::getUserName, username));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：username {} 不存在.", username);
            throw new UserException("user.not.exists", username);
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("登录用户：username {} 已被停用.", username);
            throw new UserException("user.blocked", username);
        }
        return user;
    }

    private UserVo loadUserByEmail(String email) {
        UserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：email {} 不存在.", email);
            throw new UserException("user.not.exists", email);
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("登录用户：email {} 已被停用.", email);
            throw new UserException("user.blocked", email);
        }
        return user;
    }

    private UserVo loadUserByPhonenumber(String phonenumber) {
        UserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<User>().eq(User::getPhonenumber, phonenumber));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：phonenumber {} 不存在.", phonenumber);
            throw new UserException("user.not.exists", phonenumber);
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("登录用户：phonenumber {} 已被停用.", phonenumber);
            throw new UserException("user.blocked", phonenumber);
        }
        return user;
    }

}
