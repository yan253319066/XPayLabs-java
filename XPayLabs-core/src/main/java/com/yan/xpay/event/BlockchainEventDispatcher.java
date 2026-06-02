package com.yan.xpay.event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.config.PendingTransactionCache;
import com.yan.xpay.config.SystemAddressCache;
import com.yan.xpay.config.XPayConfig;
import com.yan.xpay.domain.*;
import com.yan.xpay.domain.vo.MerchantAddressVo;
import com.yan.xpay.domain.vo.NotifyMerchant;
import com.yan.xpay.enums.*;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.mapper.UserAddressMapper;
import com.yan.xpay.service.*;
import com.yan.xpay.utils.AmountUtils;
import com.yan.xpay.utils.FeeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockchainEventDispatcher {
	private final BlockchainDataSyncService blockchainDataSyncService;
	private final ApplicationEventPublisher eventPublisher;

	private final IPaymentOrderService paymentOrderService;
	private final IMerchantAddressService merchantAddressService;
	private final AssetTypeCache assetTypeCache;
	private final PendingTransactionCache pendingTransactionCache;
	private final WalletService walletService;
	private final IMerchantRechargeWithdrawService merchantRechargeWithdrawService;
	private final SystemAddressCache systemAddressCache;
	private final UserAddressMapper userAddressMapper;
	private final MerchantMapper merchantMapper;
	private final XPayConfig xPayConfig;

	@Async
	@EventListener
	public void handleTransactionEvent(TransactionEvent event) {
		Transaction tx = event.getTx();
		String toAddress = tx.getTo().toLowerCase();
		String fromAddress = tx.getFrom().toLowerCase();
		tx.setTimestamp(unifyTimestamp(tx.getTimestamp()));

		// 按需加载数据（减少不必要的查询）
		boolean processed = false;

		AssetType assetType = assetTypeCache.getBySymbol(tx.getChain(),  tx.getSymbol());
		if(assetType == null) throw new ServiceException("Unsupported asset type");

		if(!processed) {
			// 平台热钱包转出并且是转给系统地址（大概率是转手续费）
			if(assetType.getHotAddress().equalsIgnoreCase(tx.getFrom()) && systemAddressCache.existAddress(tx.getChain(), tx.getTo())) {
				if(tx.getStatus() == BlockchainStatus.SUCCESS)
					log.info("平台热钱包转出给系统地址成功（大概率是转手续费） {}", JSONUtil.toJsonStr(tx));
				processed = true;
			}
		}

		// 检查支付订单（代收/代付场景）
		if (!processed) {
			processed = processPaymentOrders(tx, assetType);
		}

		// 检查商家地址（充值/提现场景）
		if (!processed) {
			List<MerchantAddressVo> hotAddresses = merchantAddressService.getHotAddressByAddress(tx.getChain(), tx.getSymbol(), toAddress);
			if (CollUtil.isNotEmpty(hotAddresses)) {
				if(hotAddresses.size() > 1){
					log.warn("存在多个商家热钱包地址，不会出现这种情况的，只有手动改了数据库才会出现，V2版本是允许的。这里不会处理，需要手动处理。 {}", JSONUtil.toJsonStr(hotAddresses));
					return;
				}
				processed = recharge(tx, hotAddresses.get(0).getMerchantId());
				log.info("商家充值，结果 {} status {} chain {} txId {}", processed, tx.getStatus(), tx.getChain(), tx.getTxid());
			} else {
				MerchantRechargeWithdraw withdraw = merchantRechargeWithdrawService.getWithdrawByTxid(tx.getChain(), tx.getSymbol(), tx.getTxid());
				if (withdraw != null) {
					processed = withdrawal(tx, withdraw);
					log.info("商家提现，结果 {} status {} chain {} txId {}", processed, tx.getStatus(), tx.getChain(), tx.getTxid());
				}
			}
		}

		// 检查用户地址（1、归集场景, 2、根据用户生成的地址检测充值）
		if (!processed) {
			processed = processCollectionOrRecharge(tx, assetType);
		}

		updateTransactionCache(tx, processed);
	}

	private boolean processCollectionOrRecharge(Transaction tx, AssetType assetType) {
		UserAddress userAddress = userAddressMapper.selectOne(
			new LambdaQueryWrapper<UserAddress>()
				.eq(UserAddress::getChain, tx.getChain())
				.eq(UserAddress::getSymbol, tx.getSymbol())
				.in(UserAddress::getAddress, tx.getFrom(), tx.getTo()));
		if(userAddress != null) {
			if(userAddress.getAddress().equalsIgnoreCase(tx.getFrom())) {//收集
				boolean b = blockchainDataSyncService.collect(userAddress, tx);
				log.info("归集，结果 {} status {} chain {} txId {}", b, tx.getStatus(), tx.getChain(), tx.getTxid());
				return b;
			}else if(userAddress.getAddress().equalsIgnoreCase(tx.getTo())) {//商家是固定每个用户一个地址 -> 充值
				Merchant merchant = merchantMapper.selectById(userAddress.getMerchantId());
				if(merchant.getGeneratedAddressType() == GeneratedAddressType.USER) {//商家生成地址类型必须是按用户ID生成的才能进入
					if(merchant.getAccountType() == MerchantAccountType.MAIN) {
						if(assetType.getNetwork() == BlockchainNetwork.TEST) {
							log.warn("正式账户不支持测试网络 merchant name {}", merchant.getName());
							return true;
						};
					}
					if(tx.getStatus() == BlockchainStatus.PENDING) {//发现用户充值,这里只需要把pending数据添加，后面的流程会自动走代收。
						//创建代收订单
						PaymentOrder payin = createPayin(merchant, userAddress.getUserId(), userAddress.getChain(), userAddress.getSymbol(), AmountUtils.fromAmount(tx.getAmount(), tx.getDecimals()), userAddress.getAddress());
						boolean b = blockchainDataSyncService.paymentPending(tx, payin);
						log.info("固定每个用户一个地址充值，结果 {} status {} chain {} txId {}", b, tx.getStatus(), tx.getChain(), tx.getTxid());
						paymentNotifyMerchant(payin, tx);
						return b;
					}
				}
			}
		}
		return false;
	}

	private PaymentOrder createPayin(Merchant merchant, String uid, Chain chain, String symbol, BigDecimal amount, String receiveAddress) {
		AssetType assetType = assetTypeCache.getBySymbol(chain, symbol);
		// 创建订单
		Snowflake snowflake = IdUtil.getSnowflake(1, 1);
		String orderId = snowflake.nextIdStr();

		PaymentOrder payin = new PaymentOrder();
		payin.setOrderType(OrderType.COLLECTION);
		payin.setMerchantOrderId(orderId);
		payin.setUid(uid);
		payin.setMerchantId(merchant.getId());
		payin.setAssetTypeId(assetType.getId());
		payin.setChain(assetType.getChain());
		payin.setSymbol(assetType.getSymbol());
		payin.setAmount(amount);
		payin.setReceiveAddress(receiveAddress);

		BigDecimal fee = FeeUtils.getPlatformFee(amount, merchant.getFeeRatio());

		if(fee.compareTo(BigDecimal.ZERO) > 0) {
			payin.setHandingRate(merchant.getFeeRatio());
			payin.setHandingFee(fee);
		}else {
			payin.setHandingRate(BigDecimal.ZERO);
			payin.setHandingFee(BigDecimal.ZERO);
		}

		payin.setNotifyStatus(NotifyStatus.INIT);
		payin.setCallbackUrl(merchant.getCallbackUrl());
		Long timestamp = DateUtil.currentSeconds() + xPayConfig.getOrderExpiredTime();
		payin.setExpiredTime(timestamp);
		payin.setCreateTime(DateUtil.date());
		payin.setUpdateTime(DateUtil.date());
		payin.setStatus(OrderStatus.INIT);
		return payin;
	}

	private boolean processPaymentOrders(Transaction tx, AssetType assetType) {
		// 尝试代付
		PaymentOrder payoutOrder = paymentOrderService.getPayoutByTxid(tx.getChain(), tx.getSymbol(), tx.getTxid());
		if (payoutOrder != null)  {
			boolean b;
			if(tx.getStatus() == BlockchainStatus.PENDING){
				b = blockchainDataSyncService.paymentPending(tx, payoutOrder);
			}else {
				b = blockchainDataSyncService.completeTransaction(payoutOrder, tx);
			}
			log.info("代付，processed {} status {} chain {} txId {}", b, tx.getStatus(), tx.getChain(), tx.getTxid());
			paymentNotifyMerchant(payoutOrder, tx);
			return b;

		}else {
			// 尝试代收
			List<PaymentOrder> payinOrders = paymentOrderService.getInitPayinByAddress(tx.getChain(), tx.getSymbol(), tx.getTo());
			if (CollUtil.isNotEmpty(payinOrders))  {
				if(payinOrders.size() > 1) {
					log.error("V3版本 不允许一个地址同时在一个链上有多个订单。 {}", JSONUtil.toJsonStr(payinOrders));
					return false;
				}
				boolean b = processPayment(tx, payinOrders.get(0),assetType);
				log.info("代收，processed {} status {} chain {} txId {}", b, tx.getStatus(), tx.getChain(), tx.getTxid());
				return b;
			}
		}

		return false;
	}

	private void updateTransactionCache(Transaction tx, boolean processed) {
		if (processed) {
			if (tx.getStatus()  == BlockchainStatus.PENDING) {
				pendingTransactionCache.setPendingTransaction(tx.getChain(),  tx);
//				log.info("添加{}交易到缓存 chain {} txId {}", tx.getStatus(), tx.getChain(), tx.getTxid());
			} else {
				pendingTransactionCache.deletePendingTransaction(tx.getChain(),  tx.getTxid());
//				log.info("删除{}交易到缓存 chain {} txId {}", tx.getStatus(), tx.getChain(), tx.getTxid());
			}
		}else if(tx.getStatus() != BlockchainStatus.PENDING) {
			pendingTransactionCache.deletePendingTransaction(tx.getChain(),  tx.getTxid());
//			log.info("删除缓存 {}", JSONUtil.toJsonStr(tx));
		}
	}

	private boolean processPayment(Transaction tx, PaymentOrder order, AssetType assetType) {
		BigInteger txAmount = AmountUtils.toAmount(order.getAmount(),  assetType.getDecimals());
		if(!assetType.getSymbol().equals(order.getSymbol())) {
			log.info("block status {} 订单币种和交易币种不一致 tx symbol {} order symbol {}", tx.getStatus(), tx.getSymbol(), order.getSymbol());
			return false;
		}
		if(tx.getAmount().toBigInteger().compareTo(
				txAmount
			) >= 0) {
			if(tx.getStatus() == BlockchainStatus.PENDING){
				blockchainDataSyncService.paymentPending(tx, order);
			}else {
				blockchainDataSyncService.completeTransaction(order, tx);
			}
			paymentNotifyMerchant(order, tx);
			return true;
		}else {
			if(tx.getStatus() == BlockchainStatus.PENDING) return true;
			else {
				return blockchainDataSyncService.alreadyGivenTransaction(order, tx, assetType);
			}
		}
	}

//	private boolean processPayment(Transaction tx, List<PaymentOrder> orders, AssetType assetType) {
//		PaymentOrder order;
//		if(tx.getStatus() == BlockchainStatus.PENDING){
//			Optional<PaymentOrder> latestMatchedOrder = orders.stream()
//				.filter(o ->
//					assetType.getSymbol().equals(o.getSymbol())  &&
//						tx.getAmount().toBigInteger().compareTo(
//							AmountUtils.toAmount(o.getAmount(),  assetType.getDecimals())
//						) == 0
//				)
//				.max(Comparator.comparing(PaymentOrder::getCreateTime));  // 直接取时间最大的
//
//			if (latestMatchedOrder.isPresent())  {
//				order = latestMatchedOrder.get();
//				blockchainDataSyncService.paymentPending(tx, order);
//				latestMatchedOrder.ifPresent(orders::remove);
//			} else {
//				// 没有匹配项的处理
//				log.info("没有匹配到pending订单 {}", JSONUtil.toJsonStr(tx));
//				return false;
//			}
//		}else {
//			Optional<PaymentOrder> confirmOrder = orders.stream()
//				.filter(o -> tx.getTxid().equals(o.getTxId()))
//				.findFirst();
//
//			if (confirmOrder.isPresent()) {//确认订单
//				order = confirmOrder.get();
//				blockchainDataSyncService.completeTransaction(order, tx);
//				confirmOrder.ifPresent(orders::remove);
//			}else {
//				// 没有匹配项的处理
//				log.info("没有匹配到确认订单 {}", JSONUtil.toJsonStr(tx));
//				return false;
//			}
//		}
//
//		paymentNotifyMerchant(order, tx);
//
//		return true;
//
//	}

	/**
	 * 支付通知
	 * @param order
	 * @param tx
	 */
	private void paymentNotifyMerchant(PaymentOrder order, Transaction tx) {
		//通知商家
		NotifyOrder notifyOrder = new NotifyOrder();
		notifyOrder.setOrderId(order.getMerchantOrderId());
		notifyOrder.setUid(order.getUid());
		notifyOrder.setOrderType(order.getOrderType());
		notifyOrder.setStatus(order.getStatus());
		if(StrUtil.isNotBlank(order.getReason()))
			notifyOrder.setReason(order.getReason());
		notifyOrder.setAmount(order.getAmount());
		notifyOrder.setActualAmount(order.getActualAmount());
		notifyOrder.setFee(order.getHandingFee());
		notifyOrder.setTransaction(tx);

		NotifyType notifyType = null;
		if(tx.getStatus() == BlockchainStatus.PENDING) notifyType = NotifyType.ORDER_PENDING_CONFIRMATION;
		if(tx.getStatus() == BlockchainStatus.SUCCESS) notifyType = NotifyType.ORDER_SUCCESS;
		if(tx.getStatus() == BlockchainStatus.FAILED) notifyType = NotifyType.ORDER_FAILED;
		NotifyPayload notifyPayload = new NotifyPayload(notifyType, notifyOrder);

		NotifyMerchant notifyMerchant = new NotifyMerchant();
		notifyMerchant.setMerchantId(order.getMerchantId());
		notifyMerchant.setCallbackUrl(order.getCallbackUrl());
		notifyMerchant.setNotifyPayload(notifyPayload);

		MerchantNotifyEvent merchantNotifyEvent = new MerchantNotifyEvent();
		merchantNotifyEvent.setNotifyMerchant(notifyMerchant);
		log.info("回调商家 {} {}", order.getOrderType() == OrderType.PAYOUT ? "代付" : "代收", JSONUtil.toJsonStr(notifyMerchant));
		eventPublisher.publishEvent(merchantNotifyEvent);
	}

	private boolean recharge(Transaction tx, Long merchantId) {
		return blockchainDataSyncService.merchantRecharge(tx, merchantId);
	}

	private boolean withdrawal(Transaction tx, MerchantRechargeWithdraw withdraw) {
		return blockchainDataSyncService.merchantWithdrawal(tx, withdraw);
	}

	/**
	 * 是否需要归集监听
	 * @param event
	 */
	@Async
	@EventListener
	public void handleCollectEvent(CollectEvent event) {
		AssetType assetType = assetTypeCache.getBySymbol(event.getChain(), event.getSymbol());
		walletService.triggerCollection(assetType, event.getAddress(), event.getBalance().toBigInteger());
	}

	/**
	 * 统一处理时间戳，10位秒级自动补齐为13位毫秒级，13位原样返回，异常返回-1
	 * @param timestamp 传入的时间戳（Long类型，推荐用Long，避免Integer溢出）
	 * @return 标准13位毫秒级时间戳
	 */
	public static Long unifyTimestamp(Long timestamp) {
		if (timestamp == null) {
			return -1L;
		}
		String tsStr = timestamp.toString();
		// 10位时间戳 → ×1000 补为13位
		if (tsStr.length() == 10) {
			return timestamp * 1000;
		}
		// 13位时间戳 → 原样返回
		else if (tsStr.length() == 13) {
			return timestamp;
		}
		// 非10/13位，返回-1标记异常
		else {
			return -1L;
		}
	}
}
