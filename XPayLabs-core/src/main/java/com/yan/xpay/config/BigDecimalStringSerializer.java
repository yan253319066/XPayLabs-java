package com.yan.xpay.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalStringSerializer extends JsonSerializer<BigDecimal> {
	@Override
	public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider provider)
		throws IOException {
		// 使用toPlainString()避免科学计数法
		gen.writeString(value.stripTrailingZeros().toPlainString());
	}
}
