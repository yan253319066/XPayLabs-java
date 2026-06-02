package com.yan.login.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ObjectUtil;
import com.yan.login.domain.model.*;
import com.yan.login.domain.vo.LoginUserVo;
import com.yan.login.service.UserLoginService;
import com.yan.login.service.UserRegisterService;
import com.yan.login.service.IUserAuthStrategy;
import org.dromara.common.core.enums.GrantType;
import com.yan.user.service.ISocialService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.service.ConfigService;
import org.dromara.common.core.utils.*;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.social.config.properties.SocialLoginConfigProperties;
import org.dromara.common.social.config.properties.SocialProperties;
import org.dromara.common.social.utils.SocialUtils;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.service.ISysClientService;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 登录注册
 */
@Slf4j
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserAuthController {
	private final SocialProperties socialProperties;
	private final UserLoginService userLoginService;
	private final UserRegisterService userRegisterService;
	private final ISocialService socialUserService;
	private final ISysClientService clientService;
	private final ConfigService configService;
	private final ScheduledExecutorService scheduledExecutorService;

	// 定义认证类型与配置键的映射关系
	private static final Map<GrantType, String> GRANT_TYPE_CONFIG_MAP = Map.of(
		GrantType.PASSWORD, "user.password.login",
		GrantType.SMS, "user.sms.login",
		GrantType.WEB3, "user.web3.login",
		GrantType.SOCIAL, "user.social.login"
	);

	/**
	 * 登录方法
	 *
	 * @param body 登录信息
	 * @return 结果
	 */
	@PostMapping("/login")
	public R<LoginUserVo> login(@RequestBody String body, HttpServletRequest request) {
		UserLoginBody userLoginBody = JsonUtils.parseObject(body, UserLoginBody.class);
		ValidatorUtils.validate(userLoginBody);
		// 授权类型和客户端id
		String clientId = request.getHeader(LoginHelper.CLIENT_KEY);
		GrantType grantType = userLoginBody.getGrantType();
		if(StringUtils.isBlank(clientId)) return R.fail(LoginHelper.CLIENT_KEY+" cannot be empty.");
		SysClientVo client = clientService.queryByClientId(clientId);
		// 查询不到 client 或 client 内不包含 grantType
		if (ObjectUtil.isNull(client) || !StringUtils.contains(client.getGrantType(), grantType.name().toLowerCase())) {
			log.info("客户端id: {} 认证类型：{} 异常!.", clientId, grantType);
			return R.fail(MessageUtils.message("auth.grant.type.error"));
		} else if (!SystemConstants.NORMAL.equals(client.getStatus())) {
			return R.fail(MessageUtils.message("auth.grant.type.blocked"));
		}

		switch (grantType) {
			case PASSWORD, SMS, WEB3, SOCIAL -> {
				String configKey = GRANT_TYPE_CONFIG_MAP.get(grantType);
				String value = configService.getConfigValue(configKey);
				if (StringUtils.isBlank(value)  || !Boolean.parseBoolean(value))  {
					log.error(" 认证类型：{} 异常!", grantType);
					return R.fail(MessageUtils.message("auth.grant.type.error"));
				}
			}
			default -> {
				log.error(" 未知认证类型：{}", grantType);
				return R.fail(MessageUtils.message("auth.grant.type.unknown"));
			}
		}

		// 登录
		LoginUserVo userLoginVo = IUserAuthStrategy.login(body, client, grantType);

		return R.ok(userLoginVo);
	}
	/**
	 * 获取跳转URL
	 *
	 * @param source 登录来源
	 * @return 结果
	 */
	@GetMapping("/binding/{source}")
	public R<String> authBinding(@PathVariable("source") String source, @RequestParam String domain) {
		SocialLoginConfigProperties obj = socialProperties.getType().get(source);
		if (ObjectUtil.isNull(obj)) {
			return R.fail(source + "平台账号暂不支持");
		}
		AuthRequest authRequest = SocialUtils.getAuthRequest(source, socialProperties);
		Map<String, String> map = new HashMap<>();
		map.put("domain", domain);
		map.put("state", AuthStateUtils.createState());
		String authorizeUrl = authRequest.authorize(Base64.encode(JsonUtils.toJsonString(map), StandardCharsets.UTF_8));
		return R.ok("操作成功", authorizeUrl);
	}

	/**
	 * 前端回调绑定授权(需要token)
	 *
	 * @param loginBody 请求体
	 * @return 结果
	 */
	@PostMapping("/social/callback")
	public R<Void> socialCallback(@RequestBody UserSocialLoginBody loginBody) {
		// 校验token
		StpUtil.checkLogin();
		// 获取第三方登录信息
		AuthResponse<AuthUser> response = SocialUtils.loginAuth(
			loginBody.getSource(), loginBody.getSocialCode(),
			loginBody.getSocialState(), socialProperties);
		AuthUser authUserData = response.getData();
		// 判断授权响应是否成功
		if (!response.ok()) {
			return R.fail(response.getMsg());
		}
		userLoginService.socialRegister(authUserData);
		return R.ok();
	}


	/**
	 * 取消授权(需要token)
	 *
	 * @param socialId socialId
	 */
	@DeleteMapping(value = "/unlock/{socialId}")
	public R<Void> unlockSocial(@PathVariable Long socialId) {
		// 校验token
		StpUtil.checkLogin();
		Boolean rows = socialUserService.deleteWithValidById(socialId);
		return rows ? R.ok() : R.fail("取消授权失败");
	}


	/**
	 * 退出登录
	 */
	@PostMapping("/logout")
	public R<Void> logout() {
		userLoginService.logout();
		return R.ok("退出成功");
	}

	/**
	 * 用户注册
	 */
	@PostMapping("/register")
	public R<LoginUserVo> register(@RequestBody String body, HttpServletRequest request) {
		// 授权类型和客户端id
		String clientId = request.getHeader(LoginHelper.CLIENT_KEY);
		if(StringUtils.isBlank(clientId)) return R.fail(LoginHelper.CLIENT_KEY+" cannot be empty.");
		userRegisterService.register(body);
		return login(body, request);
	}
}
