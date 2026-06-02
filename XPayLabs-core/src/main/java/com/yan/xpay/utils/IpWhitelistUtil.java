package com.yan.xpay.utils;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.yan.xpay.enums.EnableWhitelistIp;
import jakarta.servlet.http.HttpServletRequest;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.NetUtils;
import org.dromara.common.core.utils.ServletUtils;

import java.util.Arrays;

public class IpWhitelistUtil {

	/**
	 * ip是否在白名单（不检查内网IP）
	 * @param enableWhitelistIp
	 * @param whiteListIps
	 */
	public static void ipIsAllowed(EnableWhitelistIp enableWhitelistIp, String whiteListIps) {
		Assert.notNull(enableWhitelistIp);
		if(enableWhitelistIp == EnableWhitelistIp.DISABLED) return;
		if(StrUtil.isBlank(whiteListIps)) throw new ServiceException("Please set up the whitelist IP");
		String clientIP = IpWhitelistUtil.getClientIpForWhitelist(ServletUtils.getRequest());
		boolean isInner = false;
		if(NetUtils.isIPv6(clientIP)) {
			if(NetUtils.isInnerIPv6(clientIP)){
				isInner = true;
			}
		}
		if(NetUtils.isIPv4(clientIP)) {
			if(NetUtils.isInnerIP(clientIP)){
				isInner = true;
			}
		}
		if(isInner) return;
		else {
			boolean isAllowed = Arrays.stream(whiteListIps.split(","))
				.map(String::trim)
				.anyMatch(ip -> ip.equals(clientIP));
			if(!isAllowed) throw new ServiceException("IP ["+clientIP+"] is not on the whitelist.");
		}
	}

	/**
	 * 获取用于白名单判断的客户端IP
	 */
	public static String getClientIpForWhitelist(HttpServletRequest request) {
		String ip = JakartaServletUtil.getClientIP(request);

		if (ip == null) {
			return null;
		}

		// 如果是多个IP（例如 X-Forwarded-For），只取第一个
		if (ip.contains(",")) {
			ip = ip.split(",")[0].trim();
		}

		return normalizeIp(ip);
	}

	/**
	 * IPv6 / 映射IPv4 转换成标准形式
	 */
	private static String normalizeIp(String ip) {
		if (ip == null) return null;

		// IPv6 本地回环
		if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
			return "127.0.0.1";
		}

		// IPv6 映射 IPv4 (::ffff:192.168.0.1)
		if (ip.startsWith("::ffff:")) {
			return ip.substring(7);
		}

		return ip;
	}
}
