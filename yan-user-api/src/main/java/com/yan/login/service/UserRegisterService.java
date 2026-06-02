package com.yan.login.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.login.domain.model.UserEmailRegisterBody;
import com.yan.login.domain.model.UserLoginBody;
import com.yan.login.domain.model.UserPasswordRegisterBody;
import com.yan.login.domain.model.UserPhoneRegisterBody;
import com.yan.login.utils.ValidateCode;
import com.yan.user.domain.User;
import com.yan.user.domain.bo.UserBo;
import com.yan.user.enums.ValidateCodeType;
import com.yan.user.mapper.UserMapper;
import com.yan.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.CaptchaException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.service.ConfigService;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.springframework.stereotype.Service;

/**
 * 注册校验方法
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserRegisterService {

    private final IUserService userService;
    private final UserMapper userMapper;
    private final CaptchaProperties captchaProperties;
    private final ConfigService configService;

    /**
     * 注册
     */
    public void register(String registerBody) {
        UserLoginBody userLoginBody = JsonUtils.parseObject(registerBody, UserLoginBody.class);
        ValidatorUtils.validate(userLoginBody);
        boolean captchaEnabled = captchaProperties.getEnable();
        switch (userLoginBody.getGrantType()) {
            case PASSWORD -> {
                JSONObject obj = JSONUtil.parseObj(registerBody);
                String value = configService.getConfigValue("user.password.registerUser");
                if(StrUtil.isBlank(value) || !Boolean.valueOf(value)){
                    log.warn("系统没有开启 {} 注册", userLoginBody.getGrantType());
                    throw new ServiceException("Current system does not have the registration function enabled.");
                }
                if(StrUtil.isNotBlank(obj.getStr("username"))) {
                    UserPasswordRegisterBody body = JsonUtils.parseObject(registerBody, UserPasswordRegisterBody.class);
                    ValidatorUtils.validate(body);
                    // 验证码开关
                    if (captchaEnabled) {
                        ValidateCode.validateCaptcha(body.getUsername(), body.getCode(), body.getUuid(), ValidateCodeType.REGISTER);
                    }

                    boolean exist = userMapper.exists(new LambdaQueryWrapper<User>()
                        .eq(User::getUserName, body.getUsername()));

                    if (exist) {
                        throw new UserException("user.register.save.error", body.getUsername());
                    }

                    usernameRegister(body);
                }else if(StrUtil.isNotBlank(obj.getStr("phone"))) {
                    UserPhoneRegisterBody body = JsonUtils.parseObject(registerBody, UserPhoneRegisterBody.class);
                    ValidatorUtils.validate(body);
                    // 验证码开关
                    if (captchaEnabled) {
                        ValidateCode.validateCaptcha(body.getPhone(), body.getCode(), body.getUuid(), ValidateCodeType.REGISTER);
                    }

                    boolean exist = userMapper.exists(new LambdaQueryWrapper<User>()
                        .eq(User::getPhonenumber, body.getPhone()));

                    if (exist) {
                        throw new UserException("user.register.save.error", body.getPhone());
                    }

                    phoneRegister(body);
                }else if(StrUtil.isNotBlank(obj.getStr("email"))) {
                    UserEmailRegisterBody body = JsonUtils.parseObject(registerBody, UserEmailRegisterBody.class);
                    ValidatorUtils.validate(body);
                    // 验证码开关
                    if (captchaEnabled) {
                        ValidateCode.validateCaptcha(body.getEmail(), body.getCode(), body.getUuid(), ValidateCodeType.REGISTER);
                    }

                    boolean exist = userMapper.exists(new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, body.getEmail()));

                    if (exist) {
                        throw new UserException("user.register.save.error", body.getEmail());
                    }

                    emailRegister(body);
                }else {
                    log.warn("GrantType 认证类型：{} 异常!.", userLoginBody.getGrantType());
                    throw new ServiceException(MessageUtils.message("auth.grant.type.error"));
                }
            }
            case SMS -> {
                String value = configService.getConfigValue("user.sms.registerUser");
                if(StrUtil.isBlank(value) || !Boolean.valueOf(value)){
                    log.warn("系统没有开启 {} 注册", userLoginBody.getGrantType());
                }
                throw new ServiceException("Current system does not have the registration function enabled.");

            }

            default -> throw new ServiceException("找不到注册类型为" + userLoginBody.getGrantType());
        }
    }

    private void usernameRegister(UserPasswordRegisterBody body){
        UserBo user = new UserBo();
        user.setUserName(body.getUsername());
        user.setPassword(BCrypt.hashpw(body.getPassword()));
        String parentInviteCode = body.getInviteCode();
        userService.registerUser(user, parentInviteCode);
    }
    private void phoneRegister(UserPhoneRegisterBody body){
        UserBo user = new UserBo();
        user.setUserName(body.getPhone());
        user.setPhonenumber(body.getPhone());
        user.setPassword(BCrypt.hashpw(body.getPassword()));
        if(StrUtil.isNotBlank(body.getAreacode()))
            user.setAreacode(StrUtil.removePrefix(body.getAreacode(), "+"));
        else user.setAreacode("86");
        String parentInviteCode = body.getInviteCode();
        //验证phone
        if(ValidateCode.validateSmsCode(user.getPhonenumber(), body.getSmsCode(), ValidateCodeType.REGISTER)){
            userService.registerUser(user, parentInviteCode);
            ValidateCode.recordLogininfor(user.getUserName(), MessageUtils.message("user.register.success"), Constants.REGISTER);
        }
        else {
            ValidateCode.recordLogininfor(user.getUserName(), MessageUtils.message("user.register.error"), Constants.REGISTER);
            throw new CaptchaException();
        }
    }
    private void emailRegister(UserEmailRegisterBody body){
        UserBo user = new UserBo();
        user.setUserName(body.getEmail());
        user.setEmail(body.getEmail());
        user.setPassword(BCrypt.hashpw(body.getPassword()));
        String parentInviteCode = body.getInviteCode();
        //验证email
        if(ValidateCode.validateEmailCode(user.getEmail(), body.getEmailCode(), ValidateCodeType.REGISTER)){
            userService.registerUser(user, parentInviteCode);
            ValidateCode.recordLogininfor(user.getUserName(), MessageUtils.message("user.register.success"), Constants.REGISTER);
        }
        else {
            ValidateCode.recordLogininfor(user.getUserName(), MessageUtils.message("user.register.error"), Constants.REGISTER);
            throw new CaptchaException();
        }
    }

}
