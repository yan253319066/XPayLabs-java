package com.yan.login.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.lock.annotation.Lock4j;
import com.yan.login.domain.model.ForgetPayPwdForm;
import com.yan.login.domain.model.ForgetPwdForm;
import com.yan.login.domain.model.UpdatePwdForm;
import com.yan.login.utils.ValidateCode;
import com.yan.user.domain.User;
import com.yan.user.domain.bo.SocialBo;
import com.yan.user.domain.vo.SocialVo;
import com.yan.user.domain.vo.UserVo;
import com.yan.user.mapper.UserMapper;
import com.yan.user.service.ISocialService;
import com.yan.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthUser;
import org.dromara.common.core.constant.CacheConstants;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.enums.LoginType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.CaptchaExpireException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.*;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * 登录校验方法
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class UserLoginService {

	@Value("${user.password.maxRetryCount}")
	private Integer maxRetryCount;

	@Value("${user.password.lockTime}")
	private Integer lockTime;

	private final ISocialService socialService;
	private final UserMapper userMapper;
	private final IUserService userService;


	/**
	 * 绑定第三方用户
	 *
	 * @param authUserData 授权响应实体
	 */
	@Lock4j
	public void socialRegister(AuthUser authUserData) {
		String authId = authUserData.getSource() + authUserData.getUuid();
		// 第三方用户信息
		SocialBo bo = BeanUtil.toBean(authUserData, SocialBo.class);
		BeanUtil.copyProperties(authUserData.getToken(), bo);
		Long userId = LoginHelper.getUserId();
		bo.setUserId(userId);
		bo.setAuthId(authId);
		bo.setOpenId(authUserData.getUuid());
		bo.setUserName(authUserData.getUsername());
		bo.setNickName(authUserData.getNickname());
		List<SocialVo> checkList = socialService.selectByAuthId(authId);
		if (CollUtil.isNotEmpty(checkList)) {
			throw new ServiceException("此三方账号已经被绑定!");
		}
		// 查询是否已经绑定用户
		SocialBo params = new SocialBo();
		params.setUserId(userId);
		params.setSource(bo.getSource());
		List<SocialVo> list = socialService.queryList(params);
		if (CollUtil.isEmpty(list)) {
			// 没有绑定用户, 新增用户信息
			socialService.insertByBo(bo);
		} else {
			// 更新用户信息
			bo.setId(list.get(0).getId());
			socialService.updateByBo(bo);
			// 如果要绑定的平台账号已经被绑定过了 是否抛异常自行决断
			// throw new ServiceException("此平台账号已经被绑定!");
		}
	}


	/**
	 * 退出登录
	 */
	public void logout() {
		try {
			LoginUser userLogin = LoginHelper.getLoginUser();
			if (ObjectUtil.isNull(userLogin)) {
				return;
			}
			ValidateCode.recordLogininfor(userLogin.getUsername(), MessageUtils.message("user.logout.success"), Constants.LOGOUT);
		} catch (NotLoginException ignored) {
		} finally {
			try {
				StpUtil.logout();
			} catch (NotLoginException ignored) {
			}
		}
	}

	/**
	 * 构建登录用户
	 */
	public LoginUser buildLoginUser(UserVo user) {
		LoginUser loginUser = new LoginUser();
		Long userId = user.getUserId();
		loginUser.setUserId(userId);
		loginUser.setUsername(user.getUserName());
		loginUser.setNickname(user.getNickName());
		loginUser.setUserType(user.getUserType());
//		loginUser.setMenuPermission(permissionService.getMenuPermission(userId));
//		loginUser.setRolePermission(permissionService.getRolePermission(userId));
//		List<SysRoleVo> roles = roleService.selectRolesByUserId(userId);
//		List<SysPostVo> posts = postService.selectPostsByUserId(userId);
//		loginUser.setRoles(BeanUtil.copyToList(roles, RoleDTO.class));
//		loginUser.setPosts(BeanUtil.copyToList(posts, PostDTO.class));
		return loginUser;
	}

	/**
	 * 记录登录信息
	 *
	 * @param userId 用户ID
	 */
	public void recordLoginInfo(Long userId, String username, String ip) {
		User user = new User();
		user.setUserId(userId);
		user.setLoginIp(ip);
		user.setLoginDate(DateUtils.getNowDate());
		user.setUpdateBy(username);
		DataPermissionHelper.ignore(() -> userMapper.updateById(user));
	}

	/**
	 * 修改密码
	 * @param userId
	 * @param form
	 * @return
	 */
	public boolean updatePwd(Long userId, UpdatePwdForm form) {
		UserVo userVo = userMapper.selectVoById(userId);
		if (userVo == null)
			throw new ServiceException(MessageUtils.message("user.not.match"));
		if (StrUtil.isNotBlank(userVo.getPassword())) {
			String username = userVo.getUserName();
			checkLogin(LoginType.PASSWORD, username, () -> !BCrypt.checkpw(form.getOldPwd(), userVo.getPassword()));
		}
		userVo.setPassword(BCrypt.hashpw(form.getNewPwd()));
		User user = MapstructUtils.convert(userVo, User.class);
		return userMapper.updateById(user) > 0;
	}

	/**
	 * 忘记密码
	 * @param form
	 * @return
	 */
	public boolean forgetPwd(ForgetPwdForm form) {
		if(validateSmsCode(form.getMobile(), form.getCode())){
			UserVo userVo = userService.queryByUsername(form.getMobile());
			if (userVo == null)
				throw new ServiceException(MessageUtils.message("user.not.match"));
			userVo.setPassword(BCrypt.hashpw(form.getPassword()));
			User user = MapstructUtils.convert(userVo, User.class);
			return userMapper.updateById(user) > 0;
		}else throw new ServiceException(MessageUtils.message("user.jcaptcha.error"));
	}

	/**
	 * 忘记支付密码
	 * @param form
	 * @return
	 */
	public boolean forgetPayPwd(ForgetPayPwdForm form) {
		if(validateSmsCode(form.getMobile(), form.getCode())){
			UserVo userVo = userService.queryByUsername(form.getMobile());
			if (userVo == null)
				throw new ServiceException(MessageUtils.message("user.not.match"));
			userVo.setPayPwd(BCrypt.hashpw(form.getPayPwd()));
			User user = MapstructUtils.convert(userVo, User.class);
			return userMapper.updateById(user) > 0;
		}else throw new ServiceException(MessageUtils.message("user.jcaptcha.error"));
	}

	/**
	 * 校验短信验证码
	 */
	private boolean validateSmsCode(String phonenumber, String smsCode) {
		String code = RedisUtils.getCacheObject(GlobalConstants.CAPTCHA_CODE_KEY + phonenumber);
		if (StringUtils.isBlank(code)) {
			throw new CaptchaExpireException();
		}
		return code.equals(smsCode);
	}

	/**
	 * 登录校验
	 */
	public void checkLogin(LoginType loginType, String username, Supplier<Boolean> supplier) {
		String errorKey = CacheConstants.PWD_ERR_CNT_KEY + username;
		String loginFail = Constants.LOGIN_FAIL;

		// 获取用户登录错误次数，默认为0 (可自定义限制策略 例如: key + username + ip)
		int errorNumber = ObjectUtil.defaultIfNull(RedisUtils.getCacheObject(errorKey), 0);
		// 锁定时间内登录 则踢出
		if (errorNumber >= maxRetryCount) {
			ValidateCode.recordLogininfor(username, MessageUtils.message(loginType.getRetryLimitExceed(), maxRetryCount, lockTime), loginFail);
			throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
		}

		if (supplier.get()) {
			// 错误次数递增
			errorNumber++;
			RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockTime));
			// 达到规定错误次数 则锁定登录
			if (errorNumber >= maxRetryCount) {
				ValidateCode.recordLogininfor(username, MessageUtils.message(loginType.getRetryLimitExceed(), maxRetryCount, lockTime), loginFail);
				throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
			} else {
				// 未达到规定错误次数
				ValidateCode.recordLogininfor(username, MessageUtils.message(loginType.getRetryLimitCount(), errorNumber), loginFail);
				throw new UserException(loginType.getRetryLimitCount(), errorNumber);
			}
		}

		// 登录成功 清空错误次数
		RedisUtils.deleteObject(errorKey);
	}

}
