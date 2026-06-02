package com.yan.login.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.yan.login.event.UserLogininforEvent;
import com.yan.user.enums.ValidateCodeType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.exception.user.CaptchaException;
import org.dromara.common.core.exception.user.CaptchaExpireException;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.utils.RedisUtils;

import java.time.Duration;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidateCode {

	/**
	 * 校验短信验证码
	 */
	public static boolean validateSmsCode(String phonenumber, String smsCode, ValidateCodeType type) {
		String key = GlobalConstants.CAPTCHA_CODE_KEY + phonenumber;
		if(type != null)
			key = StrUtil.concat(true, GlobalConstants.CAPTCHA_CODE_KEY, phonenumber, ":", type.name());
		String code = RedisUtils.getCacheObject(key);

		switch (type) {
			case LOGIN -> {
				if (StringUtils.isBlank(code)) {
					recordLogininfor(phonenumber, MessageUtils.message("user.jcaptcha.expire"), Constants.LOGIN_FAIL);
					throw new CaptchaExpireException();
				}
			}
			case REGISTER -> {
				if (StringUtils.isBlank(code)) {
					recordLogininfor(phonenumber, MessageUtils.message("user.jcaptcha.expire"), Constants.REGISTER_ERROR);
					throw new CaptchaExpireException();
				}
			}
			case FORGET_PASSWORD -> {
				if (StringUtils.isBlank(code)) {
					recordLogininfor(phonenumber, MessageUtils.message("user.jcaptcha.expire"), "Forget Password Error");
					throw new CaptchaExpireException();
				}
			}
		}

		boolean b = code != null && code.equals(smsCode);
		if(b) RedisUtils.deleteObject(key);
		return b;
	}

	/**
	 * 校验邮箱验证码
	 */
	public static boolean validateEmailCode(String email, String emailCode, ValidateCodeType type) {
		String key = GlobalConstants.CAPTCHA_CODE_KEY + email;
		if(type != null)
			key = StrUtil.concat(true, GlobalConstants.CAPTCHA_CODE_KEY, email, ":", type.name());
		String code = RedisUtils.getCacheObject(key);

		switch (type) {
			case LOGIN -> {
				if (StringUtils.isBlank(code)) {
					recordLogininfor(email, MessageUtils.message("user.jcaptcha.expire"), Constants.LOGIN_FAIL);
					throw new CaptchaExpireException();
				}
			}
			case REGISTER -> {
				if (StringUtils.isBlank(code)) {
					recordLogininfor(email, MessageUtils.message("user.jcaptcha.expire"), Constants.REGISTER_ERROR);
					throw new CaptchaExpireException();
				}
			}
			case FORGET_PASSWORD -> {
				if (StringUtils.isBlank(code)) {
					recordLogininfor(email, MessageUtils.message("user.jcaptcha.expire"), "Forget Password Error");
					throw new CaptchaExpireException();
				}
			}
		}

		boolean b = code != null && code.equals(emailCode);
		if(b) RedisUtils.deleteObject(key);
		return b;
	}

	/**
	 * 图形校验验证码
	 *
	 * @param username 用户名
	 * @param code     验证码
	 * @param uuid     唯一标识
	 * @param type
	 */
	public static void validateCaptcha(String username, String code, String uuid, ValidateCodeType type) {
		String key = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
		if(type != null)
			key = StrUtil.concat(true, GlobalConstants.CAPTCHA_CODE_KEY, uuid, type.name());
		String captcha = RedisUtils.getCacheObject(key);
		RedisUtils.deleteObject(key);
		String message = "";
		switch (type) {
			case LOGIN -> message = Constants.LOGIN_FAIL;
			case REGISTER -> message = Constants.REGISTER_ERROR;
		}
		if (captcha == null) {
			recordLogininfor(username, MessageUtils.message("user.jcaptcha.expire"), message);
			throw new CaptchaExpireException();
		}
		if (!code.equalsIgnoreCase(captcha)) {
			recordLogininfor(username, MessageUtils.message("user.jcaptcha.error"), message);
			throw new CaptchaException();
		}
	}

	/**
	 * 记录登录信息
	 *
	 * @param username 用户名
	 * @param message  消息内容
	 * @param status   状态
	 *
	 */
	public static void recordLogininfor(String username, String message, String status){
		UserLogininforEvent logininforEvent = new UserLogininforEvent();
		logininforEvent.setUsername(username);
		logininforEvent.setStatus(status);
		logininforEvent.setMessage(message);
		logininforEvent.setRequest(ServletUtils.getRequest());
		SpringUtils.context().publishEvent(logininforEvent);
	}
}
