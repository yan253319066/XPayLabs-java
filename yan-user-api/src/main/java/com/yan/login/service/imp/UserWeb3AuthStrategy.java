package com.yan.login.service.imp;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.login.domain.model.UserWeb3LoginBody;
import com.yan.login.domain.vo.LoginUserVo;
import com.yan.login.service.IUserAuthStrategy;
import com.yan.login.service.UserLoginService;
import com.yan.login.utils.EthSign;
import com.yan.user.domain.User;
import com.yan.user.domain.bo.UserBo;
import com.yan.user.domain.vo.UserVo;
import com.yan.user.mapper.UserMapper;
import com.yan.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.enums.LoginType;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.vo.SysClientVo;
import org.springframework.stereotype.Service;

/**
 * web3认证
 */
@Slf4j
@Service(IUserAuthStrategy.PREFIX + "Web3" + IUserAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class UserWeb3AuthStrategy implements IUserAuthStrategy {
	private final UserMapper userMapper;
	private final IUserService userService;
	private final UserLoginService userLoginService;

	@Override
	public LoginUserVo login(String body, SysClientVo client) {
		UserWeb3LoginBody loginBody = JsonUtils.parseObject(body, UserWeb3LoginBody.class);
		ValidatorUtils.validate(loginBody);

		UserVo user = loadUserByUsername(loginBody);

		userLoginService.checkLogin(LoginType.WEB3, loginBody.getAddress(), () -> !EthSign.validate(loginBody.getSignature(), loginBody.getMessage(), loginBody.getAddress().toLowerCase()));
		// 此处可根据登录用户的数据不同 自行创建 loginUser
		LoginUser loginUser = userLoginService.buildLoginUser(user);

		loginUser.setClientKey(client.getClientKey());
		loginUser.setUserType(user.getUserType());
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

	private UserVo loadUserByUsername(UserWeb3LoginBody loginBody) {
		UserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<User>().eq(User::getUserName, loginBody.getAddress()));
		if (ObjectUtil.isNull(user)) {
			UserBo bo = new UserBo();
			bo.setUserName(loginBody.getAddress());
			user = userService.registerUser(bo, loginBody.getInviteCode());
		} else if (SystemConstants.DISABLE.equals(user.getStatus())) {
			log.info("登录用户：{} 已被停用.", loginBody.getAddress());
			throw new UserException("user.blocked", loginBody.getAddress());
		}
		return user;
	}
}
