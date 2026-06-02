package com.yan.login.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Verify2faBo {
	@NotNull(message = "The code cannot be empty")
	private Integer code;
}
