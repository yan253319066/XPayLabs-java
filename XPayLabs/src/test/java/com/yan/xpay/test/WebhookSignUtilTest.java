package com.yan.xpay.test;

import com.yan.xpay.config.XPayConfig;
import com.yan.xpay.enums.BlockchainStatus;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.NotifyType;
import com.yan.xpay.mapper.CollectRecordMapper;
import com.yan.xpay.mapper.PaymentOrderMapper;
import com.yan.xpay.mapper.TxRecordMapper;
import com.yan.xpay.service.WebhookService;
import com.yan.xpay.utils.AmountUtils;
import com.yan.xpay.domain.CollectRecord;
import com.yan.xpay.domain.NotifyCollect;
import com.yan.xpay.domain.NotifyPayload;
import com.yan.xpay.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
public class WebhookSignUtilTest {
	@Autowired
	private WebhookService webhookService;
	@Autowired
	private PaymentOrderMapper orderMapper;
	@Autowired
	private TxRecordMapper txRecordMapper;
	@Autowired
	private CollectRecordMapper collectRecordMapper;

	@Autowired
	private XPayConfig xPayConfig;

	@Test
	public void order(){
		log.info("订单超时时间：{}", xPayConfig.getOrderExpiredTime());
	}

//	@Test
	public void test(){

		String json = "[{\"sign\":\"4e9c5d824e35c6f4a9d4474fb3e902213955c38690b7eb0dd6fff96b4b5f790e\",\"timestamp\":1753506650,\"nonce\":\"f32bd50500374d5e98253d0c7c1b943f\",\"notifyType\":\"COLLECT_PENDING\",\"data\":{\"collectAmount\":0.0075,\"fee\":0.000037,\"feeRatio\":0.5,\"reason\":null,\"transaction\":{\"chain\":null,\"symbol\":null,\"blockNum\":null,\"txid\":null,\"contractAddress\":\"TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t\",\"from\":\"TW8ArYLg5PuwYugmYM8QSux5oXxfUbXA8c\",\"to\":\"TCyFHUZg2Y373uCYyx6QVbQJc4LZeaDre1\",\"amount\":0.0075,\"timestamp\":null,\"txGas\":null,\"confirmedNum\":0,\"status\":\"PENDING\"}}}]";

		CollectRecord
			collectRecord = createCollectRecord(1944052832788480002L, null, null, null, "TW8ArYLg5PuwYugmYM8QSux5oXxfUbXA8c", "TCyFHUZg2Y373uCYyx6QVbQJc4LZeaDre1", 7500L, 37L, new BigDecimal("0.5"), null);
		NotifyCollect notifyCollect = new NotifyCollect();
		notifyCollect.setTransaction(Transaction.getTransaction(collectRecord, 6));
		notifyCollect.setCollectAmount(AmountUtils.fromAmount("7500",  6));
		notifyCollect.setFee(AmountUtils.fromAmount("37",  6));
		notifyCollect.setFeeRatio(new BigDecimal("0.5"));
		NotifyPayload notifyPayload = new NotifyPayload(NotifyType.COLLECT_PENDING, notifyCollect);
		webhookService.notifyMerchant(1944052832788480002L, "http://localhost:8077/webhook", notifyPayload);


//		PaymentOrder order = orderMapper.selectById(1947534996477763586L);
//		TxRecord txRecord = txRecordMapper.selectById(1947535135594450945L);
//		NotifyOrder notifyOrder = new NotifyOrder();
//		notifyOrder.setOrderId(order.getMerchantOrderId());
//		notifyOrder.setOrderType(order.getOrderType());
//		notifyOrder.setStatus(order.getStatus());
//		notifyOrder.setTransaction(com.yan.blockchain.pay.domain.Transaction.getTransaction(order, txRecord, 6));
//		NotifyPayload notifyPayload = new NotifyPayload(NotifyType.ORDER_SUCCESS, notifyOrder);
//		webhookService.notifyMerchant(1944052832788480002L, "111", notifyPayload);

//		CollectRecord collectRecord = collectRecordMapper.selectById(1947525792337674241L);
//		NotifyCollect notifyCollect = new NotifyCollect();
//		notifyCollect.setCollectAmount(AmountUtils.fromAmount(collectRecord.getCollectAmount().longValue(),  6));
//		notifyCollect.setFee(AmountUtils.fromAmount(collectRecord.getFee().longValue(),  6));
//		notifyCollect.setFeeRatio(collectRecord.getFeeRatio());
//		notifyCollect.setTransaction(com.yan.blockchain.pay.domain.Transaction.getTransaction(collectRecord, 6));
//		NotifyPayload notifyPayload2 = new NotifyPayload(NotifyType.COLLECT_SUCCESS, notifyCollect);
//		webhookService.notifyMerchant(
//			1944052832788480002L,
//			"http://localhost:8077/webhook",
//			notifyPayload2
//		);
	}

	private CollectRecord createCollectRecord(Long merchantId, String txId, Chain chain, String symbol, String userAddress, String to, Long amount, Long fee, BigDecimal feeRatio, Long txFee){

		CollectRecord record = new CollectRecord();
		record.setMerchantId(merchantId);
		record.setTxId(txId);
		record.setChain(chain);
		record.setSymbol(symbol);
		record.setStatus(BlockchainStatus.PENDING);
		record.setFromAddress(userAddress);
		record.setToAddress(to);
		record.setAmount(new BigDecimal(amount));
		record.setCollectAmount(new BigDecimal(amount));
		record.setFee(new BigDecimal(fee));
		record.setFeeRatio(feeRatio);
		if(txFee != null)
			record.setTxFee(new BigDecimal(txFee));

		return record;
	}
}
