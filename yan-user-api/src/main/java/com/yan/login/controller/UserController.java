package com.yan.login.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.StrUtil;
import com.yan.login.domain.bo.Verify2faBo;
import com.yan.login.domain.model.ForgetPayPwdForm;
import com.yan.login.domain.model.ForgetPwdForm;
import com.yan.login.domain.model.UpdatePwdForm;
import com.yan.login.domain.vo.ChildCountVo;
import com.yan.login.service.GoogleAuthService;
import com.yan.login.service.UserLoginService;
import com.yan.user.domain.bo.UserBo;
import com.yan.user.domain.vo.UserVo;
import com.yan.user.enums.GoogleStatus;
import com.yan.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户信息
 *
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
	private final IUserService userService;
	private final UserLoginService userLoginService;
	private final GoogleAuthService googleAuthService;
	/**
	 * 获取用户信息
	 *
	 * @return 用户信息
	 */
	@GetMapping("/getInfo")
	public R<UserVo> getInfo() {
		LoginUser loginUser = LoginHelper.getLoginUser();
		UserVo user = userService.queryById(loginUser.getUserId());
		return R.ok(user);
	}
	/**
	 * 获取直推（一级）用户
	 * @return
	 */
	@GetMapping("getFirstChildList")
	public TableDataInfo<UserVo> getChildList(PageQuery pageQuery){
		UserBo bo = new UserBo();
		bo.setParentId(bo.getUserId());
		return userService.queryPageList(bo, pageQuery);
	}
	/**
	 * 获取直推（一级）用户人数
	 * @return
	 */
	@GetMapping("getFirstChildCount")
	public R<ChildCountVo> getChildCount() {
		ChildCountVo childCountVo = new ChildCountVo();
		childCountVo.setCount(userService.getFirstChildCount(LoginHelper.getUserId()));
		return R.ok(childCountVo);
	}
	/**
	 * 获取所有下级用户人数
	 * @return
	 */
	@GetMapping("getAllChildCount")
	public R<ChildCountVo> getAllChildCount() {
		ChildCountVo childCountVo = new ChildCountVo();
		childCountVo.setCount(userService.countByReferrerIds(LoginHelper.getUserId().toString()));
		return R.ok(childCountVo);
	}

	/**
	 * 修改密码
	 * @param form
	 * @return
	 */
	@RateLimiter(count = 3, time = 60 * 30, limitType = LimitType.IP)
	@PostMapping("updatePwd")
	public R<Void> updatePwd(@RequestBody @Validated UpdatePwdForm form) {
		return toAjax(userLoginService.updatePwd(LoginHelper.getUserId(), form));
	}

	/**
	 * 忘記密碼(短信找回)
	 * @param form
	 * @return
	 */
	@SaIgnore
	@RateLimiter(count = 3, time = 60 * 30, limitType = LimitType.IP)
	@PostMapping("forgetPwd")
	public R<Void> forgetPwd(@RequestBody @Validated ForgetPwdForm form) {
		return toAjax(userLoginService.forgetPwd(form));
	}

	/**
	 * 忘记支付密码
	 * @param form
	 * @return
	 */
	@RateLimiter(count = 3, time = 60 * 30, limitType = LimitType.IP)
	@PostMapping("forgetPayPwd")
	public R<Void> forgetPayPwd(@RequestBody @Validated ForgetPayPwdForm form) {
		return toAjax(userLoginService.forgetPayPwd(form));
	}

	/**
	 * 绑定2fa
	 * @return
	 */
	@GetMapping("/bind2fa")
	public R<Map<String, Object>> bind2fa() {
		LoginUser loginUser = LoginHelper.getLoginUser();
		UserVo user = userService.queryById(loginUser.getUserId());
		if(user.getGoogleStatus() == GoogleStatus.BOUND) return R.fail("Bound already");
		String secretKey;
		if(StrUtil.isNotBlank(user.getGoogleSecretkey())) secretKey = user.getGoogleSecretkey();
		else secretKey = googleAuthService.generateSecretKey(user.getUserName());
		String qrCodeUrl = googleAuthService.getQRCodeUrl(user.getUserName(), secretKey);

		return R.ok(Map.of(
			"secretKey", secretKey,
			"qrCodeUrl", qrCodeUrl
		));
	}

	/**
	 * 验证 2FA 验证码
	 * @param bo
	 * @return
	 */
	@RateLimiter(count = 100, time = 60 * 60 * 24, limitType = LimitType.IP)
	@PostMapping("/verify2fa")
	public R<Map<String, Object>> verify2FA(@Validated @RequestBody Verify2faBo bo) {
		String username = LoginHelper.getUsername();
		boolean b = googleAuthService.verifyCode(username, bo.getCode());
		return R.ok(Map.of(
			"verify", b
		));
	}

}
