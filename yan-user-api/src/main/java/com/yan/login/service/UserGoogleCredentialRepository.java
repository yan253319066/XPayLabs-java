package com.yan.login.service;

import com.warrenstrange.googleauth.ICredentialRepository;
import com.yan.user.domain.bo.UserBo;
import com.yan.user.domain.vo.UserVo;
import com.yan.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserGoogleCredentialRepository implements ICredentialRepository {

	private final IUserService userService;

	@Override
	public String getSecretKey(String username) {
		UserVo user = userService.getUser(username);
		return user.getGoogleSecretkey();
	}

	@Override
	public void saveUserCredentials(String username, String secretKey, int validationCode, List<Integer> scratchCodes) {
		UserVo user = userService.getUser(username);
		UserBo bo = new UserBo();
		bo.setUserId(user.getUserId());
		bo.setGoogleSecretkey(secretKey);
		userService.updateByBo(bo);
	}
}
