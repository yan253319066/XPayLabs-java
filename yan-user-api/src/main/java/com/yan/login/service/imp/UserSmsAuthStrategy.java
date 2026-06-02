package com.yan.login.service.imp;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.login.domain.model.UserPhoneSmsLoginBody;
import com.yan.login.domain.vo.LoginUserVo;
import com.yan.login.service.IUserAuthStrategy;
import com.yan.login.service.UserLoginService;
import com.yan.login.utils.ValidateCode;
import com.yan.user.domain.User;
import com.yan.user.domain.vo.UserVo;
import com.yan.user.enums.ValidateCodeType;
import com.yan.user.mapper.UserMapper;
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
 * 短信认证策略
 *
 * @author Michelle.Chung
 */
@Slf4j
@Service(IUserAuthStrategy.PREFIX + "Sms" + IUserAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class UserSmsAuthStrategy implements IUserAuthStrategy {

	private final UserLoginService userLoginService;
	private final UserMapper userMapper;

	@Override
	public LoginUserVo login(String body, SysClientVo client) {
		UserPhoneSmsLoginBody loginBody = JsonUtils.parseObject(body, UserPhoneSmsLoginBody.class);
		ValidatorUtils.validate(loginBody);
		String phonenumber = loginBody.getPhonenumber();
		String smsCode = loginBody.getSmsCode();

		UserVo user = loadUserByPhonenumber(phonenumber);
		userLoginService.checkLogin(LoginType.SMS, user.getUserName(), () -> !ValidateCode.validateSmsCode(phonenumber, smsCode, ValidateCodeType.LOGIN));
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

	private UserVo loadUserByPhonenumber(String phonenumber) {
		UserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<User>().eq(User::getPhonenumber, phonenumber));
		if (ObjectUtil.isNull(user)) {
			log.info("登录用户：{} 不存在.", phonenumber);
			throw new UserException("user.not.exists", phonenumber);
		} else if (SystemConstants.DISABLE.equals(user.getStatus())) {
			log.info("登录用户：{} 已被停用.", phonenumber);
			throw new UserException("user.blocked", phonenumber);
		}
		return user;
	}

}