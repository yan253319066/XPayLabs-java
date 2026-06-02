package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yan.xpay.enums.Chain;
import lombok.Data;

import java.io.Serial;
import java.math.BigInteger;
import java.util.Date;

@Data
@TableName("t_error_block")
public class ErrorBlock {
	@Serial
	private static final long serialVersionUID = 1L;

	@TableId(value = "id")
	private  Long id;
	private Chain chain;
	private BigInteger blockNumber;
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;
}
