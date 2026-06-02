package com.yan.login.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.yan.login.domain.vo.UserCaptchaVo;
import com.yan.user.enums.ValidateCodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.reflect.ReflectUtils;
import org.dromara.common.mail.config.properties.MailProperties;
import org.dromara.common.mail.utils.MailUtils;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.dromara.common.web.enums.CaptchaType;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;

/**
 * 验证码操作处理
 *
 * @author Lion Li
 */
@SaIgnore
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
public class UserCaptchaController {

    private final CaptchaProperties captchaProperties;
    private final MailProperties mailProperties;

    @Value("${spring.application.name}")
    private String appName;

    /**
     * 短信验证码
     *
     * @param phonenumber 用户手机号
     * @param type
     */
    @RateLimiter(key = "#phonenumber + ':' + #type", time = 60, count = 1)
    @GetMapping("/resource/sms/code")
    public R<Void> smsCode(@NotBlank(message = "{user.phonenumber.not.blank}") String phonenumber, @NotNull(message = "Phone type not blank") ValidateCodeType type) {
        String key = GlobalConstants.CAPTCHA_CODE_KEY + phonenumber + ":" + type;
        String code = RandomUtil.randomNumbers(4);
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        // 验证码模板id 自行处理 (查数据库或写死均可)
        String templateId = "";
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("code", code);
        SmsBlend smsBlend = SmsFactory.getSmsBlend("config1");
        SmsResponse smsResponse = smsBlend.sendMessage(phonenumber, templateId, map);
        if (!smsResponse.isSuccess()) {
            log.error("验证码短信发送异常 => {}", smsResponse);
            return R.fail(smsResponse.getData().toString());
        }
        return R.ok();
    }

    /**
     * 邮箱验证码
     * @param email
     * @param type
     * @return
     */
    @GetMapping("/resource/email/code")
    public R<Void> emailCode(@NotBlank(message = "{user.email.not.blank}") String email, @NotNull(message = "Email type not blank") ValidateCodeType type) {
        if (!mailProperties.getEnabled()) {
            return R.fail("Current system does not have the email function enabled.");
        }
        SpringUtils.getAopProxy(this).emailCodeImpl(email, type);
        return R.ok();
    }

    /**
     * 邮箱验证码
     * 独立方法避免验证码关闭之后仍然走限流
     */
    @RateLimiter(key = "#email + ':' + #type", time = 60, count = 1)
    public void emailCodeImpl(String email, ValidateCodeType type) {
        String key = GlobalConstants.CAPTCHA_CODE_KEY + email + ":" + type;
        String code = RandomUtil.randomNumbers(4);
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        try {
            String subject = "";
            String content = "";

            switch (type) {
                case REGISTER -> {
                    subject = MessageUtils.message("verification.register.title", appName, code);
                    content = MessageUtils.message("verification.register.content", appName, code);
                }
                case LOGIN -> {
                    subject = MessageUtils.message("verification.login.title", appName, code);
                    content = MessageUtils.message("verification.login.content", appName, code);
                }
                case FORGET_PASSWORD -> {
                    subject = MessageUtils.message("verification.forgetPassword.title", appName, code);
                    content = MessageUtils.message("verification.forgetPassword.content", appName, code);
                }
                case RESET_PASSWORD -> {
                    subject = MessageUtils.message("verification.resetPassword.title", appName, code);
                    content = MessageUtils.message("verification.resetPassword.content", appName, code);
                }
            }
            MailUtils.sendText(email, subject, content);
        } catch (Exception e) {
            log.error("验证码邮件发送异常 => ", e);
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 生成图形验证码
     */
    @GetMapping("/auth/code")
    public R<UserCaptchaVo> getCode() {
        boolean captchaEnabled = captchaProperties.getEnable();
        if (!captchaEnabled) {
            UserCaptchaVo userCaptchaVo = new UserCaptchaVo();
            userCaptchaVo.setCaptchaEnabled(false);
            return R.ok(userCaptchaVo);
        }
        return R.ok(SpringUtils.getAopProxy(this).getCodeImpl());
    }

    /**
     * 生成验证码
     * 独立方法避免验证码关闭之后仍然走限流
     */
    @RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
    public UserCaptchaVo getCodeImpl() {
        // 保存验证码信息
        String uuid = IdUtil.simpleUUID();
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + uuid;
        // 生成验证码
        CaptchaType captchaType = captchaProperties.getType();
        boolean isMath = CaptchaType.MATH == captchaType;
        Integer length = isMath ? captchaProperties.getNumberLength() : captchaProperties.getCharLength();
        CodeGenerator codeGenerator = ReflectUtils.newInstance(captchaType.getClazz(), length);
        AbstractCaptcha captcha = SpringUtils.getBean(captchaProperties.getCategory().getClazz());
        captcha.setGenerator(codeGenerator);
        captcha.createCode();
        // 如果是数学验证码，使用SpEL表达式处理验证码结果
        String code = captcha.getCode();
        if (isMath) {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(StringUtils.remove(code, "="));
            code = exp.getValue(String.class);
        }
        RedisUtils.setCacheObject(verifyKey, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        UserCaptchaVo userCaptchaVo = new UserCaptchaVo();
        userCaptchaVo.setUuid(uuid);
        userCaptchaVo.setImg(captcha.getImageBase64());
        return userCaptchaVo;
    }

}
