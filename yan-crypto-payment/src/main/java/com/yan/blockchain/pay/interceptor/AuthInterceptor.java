package com.yan.blockchain.pay.interceptor;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.blockchain.pay.annotation.VerifySign;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.exception.SignedException;
import com.yan.xpay.service.IMerchantService;
import com.yan.xpay.utils.IpWhitelistUtil;
import com.yan.xpay.utils.WebhookSignUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

	private final IMerchantService merchantService;

	public static final String REQUEST_MERCHANT_KEY = "AUTH_MERCHANT";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String token = request.getHeader("X-API-TOKEN");
		if (StrUtil.isEmpty(token)) {
			log.error("uri {} token {} Permission denied", request.getRequestURI(), token);
			throw new ServiceException("Permission denied");
		}

		MerchantVo merchant = merchantService.getByToken(token);
		if (merchant == null) {
			log.error("token {} Invalid permissions", token);
			throw new ServiceException("Invalid permissions");
		}

		IpWhitelistUtil.ipIsAllowed(merchant.getEnableWhitelistIp(), merchant.getWhiteListIp());

		request.setAttribute(REQUEST_MERCHANT_KEY, merchant);

		// 只处理带有@VerifySign注解的方法
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;

			// 检查类上的注解
			Class<?> controllerClass = handlerMethod.getBeanType();
			VerifySign classAnnotation = controllerClass.getAnnotation(VerifySign.class);

			// 检查方法上的注解
			VerifySign methodAnnotation = handlerMethod.getMethodAnnotation(VerifySign.class);

			// 类或方法有注解且required=true时触发验证
			boolean needVerify = (classAnnotation != null && classAnnotation.required())  ||
				(methodAnnotation != null && methodAnnotation.required());

			if(needVerify) {
				// 1. 获取请求参数
				Map<String, Object> params = getParams(request);
				if(params == null || params.size() == 0) return true;
				// 2. 配置获取密钥
				String secret = merchant.getWebhookSecret();

				// 3. 调用验证工具
				boolean b = WebhookSignUtil.verifySign(secret,  params);
				if(b) return true;
				else throw new ServiceException("Sign error");
			}

		}

		return true;
	}

	private Map<String, Object> getParams(HttpServletRequest request) {
		Map<String, Object> params = new HashMap<>();

		// 处理GET/POST参数 - 总是安全的
		request.getParameterMap().forEach((k,  v) ->
			params.put(k,  v.length  == 1 ? v[0] : v));

		// 处理JSON Body
		if (request.getContentType()  != null
			&& request.getContentType().contains("application/json"))  {
			try {
				if (request.getContentLength()  > 0) {  // 检查是否有请求体
					ObjectMapper mapper = new ObjectMapper();
					Map<? extends String, ?> body = mapper.readValue(
						request.getInputStream(),  Map.class);
					params.putAll(body);
				}
			} catch (IOException e) {
				throw new SignedException.InvalidKeyException("JSON parsing failed");
			}
		}
		return params;
	}

}

