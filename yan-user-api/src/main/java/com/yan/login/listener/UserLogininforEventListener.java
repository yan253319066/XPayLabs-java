package com.yan.login.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.yan.login.event.UserLogininforEvent;
import com.yan.user.domain.bo.LogininforBo;
import com.yan.user.service.ILogininforService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ip.AddressUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.service.ISysClientService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserLogininforEventListener {

	private final ILogininforService logininforService;
	private final ISysClientService clientService;

	/**
	 * 用户端 记录登录信息
	 *
	 * @param logininforEvent 登录事件
	 */
	@Async
	@EventListener
	public void recordLogininfor(UserLogininforEvent logininforEvent) {
		HttpServletRequest request = logininforEvent.getRequest();
		final UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
		final String ip = ServletUtils.getClientIP(request);
		// 客户端信息
		String clientId = request.getHeader(LoginHelper.CLIENT_KEY);
		SysClientVo client = null;
		if (StringUtils.isNotBlank(clientId)) {
			client = clientService.queryByClientId(clientId);
		}

		String address = AddressUtils.getRealAddressByIP(ip);
		StringBuilder s = new StringBuilder();
		s.append(getBlock(ip));
		s.append(address);
		s.append(getBlock(logininforEvent.getUsername()));
		s.append(getBlock(logininforEvent.getStatus()));
		s.append(getBlock(logininforEvent.getMessage()));
		// 打印信息到日志
		log.info(s.toString(), logininforEvent.getArgs());
		// 获取客户端操作系统
		String os = userAgent.getOs().getName();
		// 获取客户端浏览器
		String browser = userAgent.getBrowser().getName();
		// 封装对象
		LogininforBo logininfor = new LogininforBo();
		logininfor.setUserName(logininforEvent.getUsername());
		if (ObjectUtil.isNotNull(client)) {
			logininfor.setClientKey(client.getClientKey());
			logininfor.setDeviceType(client.getDeviceType());
		}
		logininfor.setIpaddr(ip);
		logininfor.setLoginLocation(address);
		logininfor.setBrowser(browser);
		logininfor.setOs(os);
		logininfor.setMsg(logininforEvent.getMessage());
		// 日志状态
		if (StringUtils.equalsAny(logininforEvent.getStatus(), Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
			logininfor.setStatus(Constants.SUCCESS);
		} else if (Constants.LOGIN_FAIL.equals(logininforEvent.getStatus())) {
			logininfor.setStatus(Constants.FAIL);
		}
		// 插入数据
		logininforService.insertLogininfor(logininfor);
	}

	private String getBlock(Object msg) {
		if (msg == null) {
			msg = "";
		}
		return "[" + msg.toString() + "]";
	}
}
