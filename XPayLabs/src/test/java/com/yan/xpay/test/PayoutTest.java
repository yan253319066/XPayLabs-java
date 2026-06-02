package com.yan.xpay.test;

import cn.hutool.core.util.IdUtil;
import com.yan.xpay.domain.req.CreatePayoutOrderReq;
import com.yan.xpay.domain.req.ReqPayload;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.service.IMerchantService;
import com.yan.xpay.service.IPaymentOrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
public class PayoutTest {

	@Autowired
	private IPaymentOrderService orderService;
	@Autowired
	private IMerchantService merchantService;

	@Test
	public void payout(){
		for (int i = 0; i < 1; i++) {
			ReqPayload<CreatePayoutOrderReq> req = new ReqPayload<>();
			CreatePayoutOrderReq createPayoutOrderReq = new CreatePayoutOrderReq();
			createPayoutOrderReq.setOrderId(IdUtil.fastSimpleUUID());
			if(i == 0){
				createPayoutOrderReq.setAmount(new BigDecimal("0.0001"));
				createPayoutOrderReq.setSymbol("SUI");
			}
			else {
				createPayoutOrderReq.setAmount(BigDecimal.ONE);
				createPayoutOrderReq.setSymbol("USDT");
			}
			createPayoutOrderReq.setChain(Chain.SUI_TEST);
			createPayoutOrderReq.setReceiveAddress("0xa740ec72dd8b5bb22d9ee739df47e8befa98670d4f4ffe949711bfef94aef06b");
			req.setData(createPayoutOrderReq);
			MerchantVo merchantVo = merchantService.getMerchantByName("XpayTest");
			orderService.createPayout(req, merchantVo);
		}
//		for (int i = 0; i < 2; i++) {
//			ReqPayload<CreatePayoutOrderReq> req = new ReqPayload<>();
//			CreatePayoutOrderReq createPayoutOrderReq = new CreatePayoutOrderReq();
//			createPayoutOrderReq.setAmount(BigDecimal.ONE);
//			createPayoutOrderReq.setOrderId(IdUtil.fastSimpleUUID());
//			createPayoutOrderReq.setSymbol("USDT");
//			createPayoutOrderReq.setChain(Chain.TRON_TEST);
//			createPayoutOrderReq.setReceiveAddress("TQTdR9EMACFcZCTsCzTzsEKLmYAvZ3WF4H");
//			req.setData(createPayoutOrderReq);
//			MerchantVo merchantVo = merchantService.getMerchantByName("XpayTestV3");
//			orderService.createPayout(req, merchantVo);
//		}
	}

//	@Test
	public void withdraw(){
		for (int i = 0; i < 2; i++) {
			merchantService.withdrawal(merchantService.getMerchantByName("XpayTestV3"), Chain.BSC_TEST, "USDT", BigDecimal.ONE);
		}
	}
}
