package com.yan.xpay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.*;
import com.yan.xpay.domain.bo.MerchantCostDetailBo;
import com.yan.xpay.enums.*;
import com.yan.xpay.mapper.*;
import com.yan.xpay.service.BlockchainDataSyncService;
import com.yan.xpay.service.IMerchantAssetsService;
import com.yan.xpay.service.IMerchantCostDetailService;
import com.yan.xpay.utils.AmountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class BlockchainDataSyncServiceImpl implements BlockchainDataSyncService {

	private final MerchantRechargeWithdrawMapper merchantRechargeWithdrawMapper;
	private final TxRecordMapper txRecordMapper;
	private final PaymentOrderMapper paymentOrderMapper;
	private final IMerchantCostDetailService merchantCostDetailService;
	private final AssetTypeCache assetTypeCache;
	private final CollectRecordMapper collectRecordMapper;
	private final UserAddressMapper userAddressMapper;
	private final MerchantMapper merchantMapper;
	private final IMerchantAssetsService merchantAssetsService;
	private final LockTemplate lockTemplate;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public Boolean merchantRecharge(Transaction tx, Long merchantId) {

		Merchant  merchant = merchantMapper.selectById(merchantId);
		if(merchant == null) throw new ServiceException(merchantId+" 商户不存在");
		AssetType assetType = assetTypeCache.getBySymbol(tx.getChain(),  tx.getSymbol());
		if(merchant.getAccountType() == MerchantAccountType.MAIN) {
			if(assetType.getNetwork() == BlockchainNetwork.TEST) {
				log.warn("正式账户不支持测试网络充值 merchant name {}", merchant.getName());
				throw new ServiceException("Production accounts do not support test networks.");
			}
		}

		String lockKey = "xpay:lock:merchant:" + merchantId + ":" + tx.getSymbol();
		// 获取分布式锁
		final LockInfo lockInfo = lockTemplate.lock(lockKey, 30000L, 5000L, RedissonLockExecutor.class);
		if (lockInfo == null) {
			throw new ServiceException("System is busy, please try again later.");
		}

		try {
			MerchantRechargeWithdraw merchantRechargeWithdraw = merchantRechargeWithdrawMapper.selectOne(new LambdaQueryWrapper<MerchantRechargeWithdraw>().eq(MerchantRechargeWithdraw::getTxId, tx.getTxid()));
			BigDecimal amount = AmountUtils.fromAmount(tx.getAmount().toPlainString(), tx.getDecimals());
			AssetType nativeToken = assetTypeCache.getNativeToken(tx.getChain());
			BigDecimal gas = AmountUtils.fromAmount(tx.getTxGas(), nativeToken.getDecimals());
			if(merchantRechargeWithdraw == null) {
				Snowflake snowflake = IdUtil.getSnowflake(1, 1);
				String transactionNo = snowflake.nextIdStr();
				merchantRechargeWithdraw = new MerchantRechargeWithdraw();
				merchantRechargeWithdraw.setTransactionNo(transactionNo);
				merchantRechargeWithdraw.setMerchantId(merchantId);
				merchantRechargeWithdraw.setFee(BigDecimal.ZERO);
				merchantRechargeWithdraw.setPayAddress(tx.getFrom());
				merchantRechargeWithdraw.setStatus(RechargeWithdrawStatus.PENDING);
				merchantRechargeWithdraw.setChain(tx.getChain());
				merchantRechargeWithdraw.setAmount(amount);
				merchantRechargeWithdraw.setReceiveAddress(tx.getTo());
				merchantRechargeWithdraw.setSymbol(tx.getSymbol());
				merchantRechargeWithdraw.setContractAddress(tx.getContractAddress());
				merchantRechargeWithdraw.setTxGas(gas);
				merchantRechargeWithdraw.setType(RechargeWithdraw.RECHARGE);
				merchantRechargeWithdraw.setTxId(tx.getTxid());
				return merchantRechargeWithdrawMapper.insert(merchantRechargeWithdraw) > 0;
			}else {
				if(merchantRechargeWithdraw.getStatus() != RechargeWithdrawStatus.PENDING) {
					log.warn("充值重复数据 {}", JSONUtil.toJsonStr(tx));
					return true;
				}
				merchantRechargeWithdraw.setStatus(tx.getStatus() == BlockchainStatus.SUCCESS ? RechargeWithdrawStatus.SUCCESS : RechargeWithdrawStatus.FAILED);
				merchantRechargeWithdraw.setTxGas(gas);
				if(tx.getStatus() == BlockchainStatus.SUCCESS){
					if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3){
						SimpleTransfer simpleTransfer = new SimpleTransfer();
						simpleTransfer.setSymbol(tx.getSymbol());
						simpleTransfer.setAmount(amount);
						simpleTransfer.setMerchantId(merchantId);
						simpleTransfer.setRate(BigDecimal.ZERO);
						simpleTransfer.setType(AssetOperType.RECHARGE);
						simpleTransfer.setRemark("商家充值");
						simpleTransfer.setFeeRate(merchant.getFeeRatio());
						simpleTransfer.setFee(BigDecimal.ZERO);
						simpleTransfer.setFeeSymbol(tx.getSymbol());
						simpleTransfer.setTransactionNo(merchantRechargeWithdraw.getTransactionNo());
						simpleTransfer.setChain(tx.getChain());
						simpleTransfer.setNetwork(assetType.getNetwork().name());
						merchantAssetsService.transfer(simpleTransfer);
					}
					log.info("商家充值成功 amount {} merchant {} transactionNo {}  {}", AmountUtils.fromAmount(tx.getAmount(), tx.getDecimals()), merchant.getName(), merchantRechargeWithdraw.getTransactionNo(), JSONUtil.toJsonStr(tx));

				}
				return merchantRechargeWithdrawMapper.updateById(merchantRechargeWithdraw) > 0;
			}
		}finally {
			lockTemplate.releaseLock(lockInfo);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public Boolean merchantWithdrawal(Transaction tx, MerchantRechargeWithdraw merchantRechargeWithdraw) {

		Merchant  merchant = merchantMapper.selectById(merchantRechargeWithdraw.getMerchantId());
		if(merchant == null) throw new ServiceException(merchantRechargeWithdraw.getMerchantId()+" 商户不存在");
		AssetType assetType = assetTypeCache.getBySymbol(tx.getChain(),  tx.getSymbol());
		if(merchant.getAccountType() == MerchantAccountType.TEST) {
			if(assetType.getNetwork() == BlockchainNetwork.MAIN) {
				log.warn("测试账户不支持正式网络提现 merchant name {}", merchant.getName());
				throw new ServiceException("Testnet accounts do not support main networks.");
			}
		}

		String lockKey = "xpay:lock:merchant:" + merchantRechargeWithdraw.getMerchantId() + ":" + tx.getSymbol();
		// 获取分布式锁
		final LockInfo lockInfo = lockTemplate.lock(lockKey, 30000L, 5000L, RedissonLockExecutor.class);
		if (lockInfo == null) {
			throw new ServiceException("System is busy, please try again later.");
		}

		try {
			if(merchantRechargeWithdraw.getStatus() == RechargeWithdrawStatus.SUCCESS ||  merchantRechargeWithdraw.getStatus() == RechargeWithdrawStatus.FAILED) {
				log.warn("提现重复数据 merchantRechargeWithdrawId {} {}", merchantRechargeWithdraw.getMerchantId(), JSONUtil.toJsonStr(tx));
				return true;
			}
			BigDecimal amount = AmountUtils.fromAmount(tx.getAmount().toPlainString(), tx.getDecimals());
			AssetType nativeToken = assetTypeCache.getNativeToken(tx.getChain());
			BigDecimal gas = AmountUtils.fromAmount(tx.getTxGas(), nativeToken.getDecimals());
			RechargeWithdrawStatus status = null;
			if(BlockchainStatus.PENDING == tx.getStatus()) status = RechargeWithdrawStatus.PENDING;
			else if(BlockchainStatus.SUCCESS == tx.getStatus()) status = RechargeWithdrawStatus.SUCCESS;
			else if(BlockchainStatus.FAILED == tx.getStatus()) status = RechargeWithdrawStatus.FAILED;
			merchantRechargeWithdraw.setStatus(status);
			merchantRechargeWithdraw.setChain(tx.getChain());
			merchantRechargeWithdraw.setAmount(amount);
			merchantRechargeWithdraw.setReceiveAddress(tx.getTo());
			merchantRechargeWithdraw.setSymbol(tx.getSymbol());
			merchantRechargeWithdraw.setContractAddress(tx.getContractAddress());
			merchantRechargeWithdraw.setTxGas(gas);
			if(tx.getStatus() == BlockchainStatus.SUCCESS){
				if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3){
					SimpleTransfer simpleTransfer = new SimpleTransfer();
					simpleTransfer.setSymbol(tx.getSymbol());
					simpleTransfer.setAmount(amount);
					simpleTransfer.setMerchantId(merchant.getId());
					simpleTransfer.setRate(BigDecimal.ZERO);
					simpleTransfer.setType(AssetOperType.WITHDRAW);
					simpleTransfer.setRemark("商家提现");
					simpleTransfer.setFeeRate(merchant.getFeeRatio());
					simpleTransfer.setFee(merchantRechargeWithdraw.getFee());
					simpleTransfer.setFeeSymbol(tx.getSymbol());
					simpleTransfer.setTransactionNo(merchantRechargeWithdraw.getTransactionNo());
					simpleTransfer.setChain(tx.getChain());
					simpleTransfer.setNetwork(assetType.getNetwork().name());
					merchantAssetsService.transfer(simpleTransfer);
				}
				log.info("商家提现成功 amount {} merchant {} transactionNo {} {}", AmountUtils.fromAmount(tx.getAmount(), tx.getDecimals()), merchant.getName(), merchantRechargeWithdraw.getTransactionNo(), JSONUtil.toJsonStr(tx));
			}else if(tx.getStatus() == BlockchainStatus.FAILED) {
				if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3){
					SimpleTransfer simpleTransfer = new SimpleTransfer();
					simpleTransfer.setSymbol(tx.getSymbol());
					simpleTransfer.setAmount(amount);
					simpleTransfer.setMerchantId(merchant.getId());
					simpleTransfer.setRate(BigDecimal.ZERO);
					simpleTransfer.setType(AssetOperType.WITHDRAW_REFUND);
					simpleTransfer.setRemark("商家提现失败，资金解冻");
					simpleTransfer.setFeeRate(merchant.getFeeRatio());
					simpleTransfer.setFee(merchantRechargeWithdraw.getFee());
					simpleTransfer.setFeeSymbol(tx.getSymbol());
					simpleTransfer.setTransactionNo(merchantRechargeWithdraw.getTransactionNo());
					simpleTransfer.setChain(tx.getChain());
					simpleTransfer.setNetwork(assetType.getNetwork().name());
					merchantAssetsService.transfer(simpleTransfer);
				}
				log.info("商家提现失败 amount {} merchant {} {}", AmountUtils.fromAmount(tx.getAmount(), tx.getDecimals()), merchant.getName(), JSONUtil.toJsonStr(tx));
			}
			return merchantRechargeWithdrawMapper.updateById(merchantRechargeWithdraw) > 0;
		}finally {
			lockTemplate.releaseLock(lockInfo);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public Boolean paymentPending(Transaction tx, PaymentOrder order) {

		Merchant  merchant = merchantMapper.selectById(order.getMerchantId());
		if(merchant == null) throw new ServiceException(order.getMerchantId()+" 商户不存在");
		AssetType assetType = assetTypeCache.getBySymbol(tx.getChain(),  tx.getSymbol());
		if(merchant.getAccountType() == MerchantAccountType.MAIN) {
			if(assetType.getNetwork() == BlockchainNetwork.TEST) {
				log.warn("正式账户不支持测试网络支付pending， merchant name {}", merchant.getName());
				throw new ServiceException("Production accounts do not support test networks.");
			}
		}else {
			if(assetType.getNetwork() == BlockchainNetwork.MAIN && order.getOrderType() == OrderType.PAYOUT) {
				log.warn("测试账户不支持正式网络创建代付pending merchant name {}", merchant.getName());
				throw new ServiceException("Test accounts do not support main networks.");
			}
		}

		if (txRecordMapper.exists(new LambdaQueryWrapper<TxRecord>()
			.eq(TxRecord::getTxId, tx.getTxid())))  {
			log.error("交易记录已存在: {} ", tx.getTxid());
			return false;
		}

		TxRecord record = new TxRecord();
		record.setChain(tx.getChain());
		record.setSymbol(tx.getSymbol());
		record.setTxId(tx.getTxid());
		record.setFromAddress(tx.getFrom());
		record.setToAddress(tx.getTo());
		record.setAmount(tx.getAmount());
		record.setBlockNumber(tx.getBlockNum());
		record.setBlockTime(tx.getTimestamp());
		record.setOrderId(order.getMerchantOrderId());
		record.setStatus(tx.getStatus());
		record.setTxType(order.getOrderType() == OrderType.COLLECTION ? TxType.COLLECTION : TxType.PAYOUT);
		if(StrUtil.isNotBlank(tx.getContractAddress()))
			record.setContractAddress(tx.getContractAddress());
		txRecordMapper.insert(record);

		order.setStatus(OrderStatus.PENDING_CONFIRMATION);
		order.setPayAddress(tx.getFrom());
		order.setTxId(tx.getTxid());

		order.setNotifyStatus(NotifyStatus.SUCCESS);
		order.setNotifyTime(DateUtil.date());

//		log.info(" 发现交易[{}], chain {} symbol {} from {} to {} 金额: {}", tx.getTxid(), tx.getChain(), tx.getSymbol(), tx.getFrom(), tx.getTo(), tx.getAmount());
		return paymentOrderMapper.insertOrUpdate(order);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public Boolean completeTransaction(PaymentOrder order, Transaction tx) {

		Merchant  merchant = merchantMapper.selectById(order.getMerchantId());
		if(merchant == null) throw new ServiceException(order.getMerchantId()+" 商户不存在");
		AssetType assetType = assetTypeCache.getBySymbol(tx.getChain(),  tx.getSymbol());
		if(merchant.getAccountType() == MerchantAccountType.MAIN) {
			if(assetType.getNetwork() == BlockchainNetwork.TEST) {
				log.warn("正式账户不支持测试网络支付 merchant name {}", merchant.getName());
				throw new ServiceException("Production accounts do not support test networks.");
			}
		}else {
			if(assetType.getNetwork() == BlockchainNetwork.MAIN && order.getOrderType() == OrderType.PAYOUT) {
				log.warn("测试账户不支持正式网络创建代付 merchant name {}", merchant.getName());
				throw new ServiceException("Test accounts do not support main networks.");
			}
		}

		String lockKey = "xpay:lock:merchant:" + order.getMerchantId() + ":" + tx.getSymbol();
		// 获取分布式锁
		final LockInfo lockInfo = lockTemplate.lock(lockKey, 30000L, 5000L, RedissonLockExecutor.class);
		if (lockInfo == null) {
			throw new ServiceException("System is busy, please try again later.");
		}

		try {
			if(order.getStatus() == OrderStatus.SUCCESS  || order.getStatus() == OrderStatus.FAILED || order.getStatus() == OrderStatus.EXPIRED) {
				log.warn("订单重复数据 {}", JSONUtil.toJsonStr(tx));
				return true;
			}
			OrderStatus status = null;
			BigDecimal amount = AmountUtils.fromAmount(tx.getAmount().toPlainString(), tx.getDecimals());
			if(amount.compareTo(order.getAmount()) < 0) {
				throw new ServiceException("订单金额不符 order amount " + order.getAmount() + "tx amount "+ amount);
			}
			if(BlockchainStatus.SUCCESS == tx.getStatus()) status = OrderStatus.SUCCESS;
			else if(BlockchainStatus.FAILED == tx.getStatus()) status = OrderStatus.FAILED;
			order.setStatus(status);
			order.setPayAddress(tx.getFrom());
			order.setTxId(tx.getTxid());
			AssetType nativeToken = assetTypeCache.getNativeToken(tx.getChain());
			order.setTxGas(AmountUtils.fromAmount(tx.getTxGas(), nativeToken.getDecimals()));
			order.setNotifyStatus(NotifyStatus.SUCCESS);
			order.setNotifyTime(DateUtil.date());
			if(order.getOrderType() == OrderType.PAYOUT)
				order.setActualAmount(order.getAmount());
			else
				order.setActualAmount(order.getAmount().subtract(order.getHandingFee()));
			order.setExtraGiven(amount.subtract(order.getAmount()));
			paymentOrderMapper.updateById(order);

			TxRecord txRecord = txRecordMapper.selectOne(new LambdaQueryWrapper<TxRecord>().eq(TxRecord::getTxId, order.getTxId()));
			if(txRecord != null) {
				txRecord.setTxFee(tx.getTxGas());
				txRecord.setStatus(tx.getStatus());
				txRecord.setConfirmedNum(tx.getConfirmedNum());
				txRecordMapper.updateById(txRecord);
			}

			if(OrderType.PAYOUT == order.getOrderType()) {
				//代付花费的gas费
				MerchantCostDetailBo merchantCostDetailBo2 = new MerchantCostDetailBo();
				merchantCostDetailBo2.setCostType(CostType.PAYOUT_GAS);
				merchantCostDetailBo2.setChain(order.getChain());
				merchantCostDetailBo2.setSymbol(order.getSymbol());
				merchantCostDetailBo2.setMerchantId(order.getMerchantId());
				merchantCostDetailBo2.setBusinessId(order.getMerchantOrderId());
				merchantCostDetailBo2.setAmount(AmountUtils.fromAmount(tx.getTxGas(), nativeToken.getDecimals()).negate());
				merchantCostDetailService.insertByBo(merchantCostDetailBo2);

				if(tx.getStatus() == BlockchainStatus.FAILED) {
					if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3){
						SimpleTransfer simpleTransfer = new SimpleTransfer();
						simpleTransfer.setSymbol(tx.getSymbol());
						simpleTransfer.setAmount(order.getAmount());
						simpleTransfer.setMerchantId(order.getMerchantId());
						simpleTransfer.setRate(BigDecimal.ZERO);
						simpleTransfer.setType(AssetOperType.PAYOUT_REFUND);
						simpleTransfer.setRemark("商家付款失败，资金解冻");
						simpleTransfer.setFeeRate(order.getHandingRate());
						simpleTransfer.setFee(order.getHandingFee());
						simpleTransfer.setFeeSymbol(tx.getSymbol());
						simpleTransfer.setTransactionNo(order.getMerchantOrderId());
						simpleTransfer.setChain(tx.getChain());
						simpleTransfer.setNetwork(assetType.getNetwork().name());
						merchantAssetsService.transfer(simpleTransfer);
					}
					log.info("付款失败 amount {} orderNo {} {}", AmountUtils.fromAmount(tx.getAmount(), tx.getDecimals()), order.getMerchantOrderId(), JSONUtil.toJsonStr(tx));
					return true;
				}

			}else if(OrderType.COLLECTION == order.getOrderType()) {
				//V3版本将地址状态改为未使用
				if(tx.getStatus() == BlockchainStatus.SUCCESS || tx.getStatus() == BlockchainStatus.FAILED) {
					UserAddress userAddress = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, "0").eq(UserAddress::getAddress, tx.getTo()).eq(UserAddress::getChain, tx.getChain()).eq(UserAddress::getSymbol, tx.getSymbol()).eq(UserAddress::getStatus, AddressStatus.USED));
					if(userAddress != null){
						userAddress.setStatus(AddressStatus.UNUSED);
						userAddressMapper.updateById(userAddress);
					}
				}
			}

			if(tx.getStatus() == BlockchainStatus.SUCCESS){
				if(order.getOrderType() == OrderType.COLLECTION){
					if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3){
						SimpleTransfer simpleTransfer = new SimpleTransfer();
						simpleTransfer.setSymbol(tx.getSymbol());
						simpleTransfer.setAmount(order.getAmount());
						simpleTransfer.setMerchantId(order.getMerchantId());
						simpleTransfer.setRate(BigDecimal.ZERO);
						simpleTransfer.setType(AssetOperType.PAYIN);
						simpleTransfer.setRemark("商家收款");
						simpleTransfer.setFeeRate(order.getHandingRate());
						simpleTransfer.setFee(order.getHandingFee());
						simpleTransfer.setFeeSymbol(tx.getSymbol());
						simpleTransfer.setTransactionNo(order.getMerchantOrderId());
						simpleTransfer.setChain(tx.getChain());
						simpleTransfer.setNetwork(assetType.getNetwork().name());
						merchantAssetsService.transfer(simpleTransfer);
					}

					log.info("收款成功 amount {} orderNo {} {}", AmountUtils.fromAmount(tx.getAmount(), tx.getDecimals()), order.getMerchantOrderId(), JSONUtil.toJsonStr(tx));
				}
				else if(order.getOrderType() == OrderType.PAYOUT){
					if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3){
						SimpleTransfer simpleTransfer = new SimpleTransfer();
						simpleTransfer.setSymbol(tx.getSymbol());
						simpleTransfer.setAmount(order.getAmount());
						simpleTransfer.setMerchantId(order.getMerchantId());
						simpleTransfer.setRate(BigDecimal.ZERO);
						simpleTransfer.setType(AssetOperType.PAYOUT);
						simpleTransfer.setRemark("商家付款");
						simpleTransfer.setFeeRate(order.getHandingRate());
						simpleTransfer.setFee(order.getHandingFee());
						simpleTransfer.setFeeSymbol(tx.getSymbol());
						simpleTransfer.setTransactionNo(order.getMerchantOrderId());
						simpleTransfer.setChain(tx.getChain());
						simpleTransfer.setNetwork(assetType.getNetwork().name());
						merchantAssetsService.transfer(simpleTransfer);
					}
					log.info("付款成功 amount {} orderNo {} {}", AmountUtils.fromAmount(tx.getAmount(), tx.getDecimals()), order.getMerchantOrderId(), JSONUtil.toJsonStr(tx));
				}
			}
			return true;
		}
		catch (RuntimeException e) {
			log.error("block status {}", tx.getStatus(), e);
			throw e;
		}
		finally {
			lockTemplate.releaseLock(lockInfo);
		}

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public Boolean alreadyGivenTransaction(PaymentOrder order, Transaction tx, AssetType assetType) {
		BigDecimal txAmount = AmountUtils.fromAmount(tx.getAmount(),  assetType.getDecimals());
		log.info("block status {} 交易金额小于订单金额 tx amount {} order amount {}", tx.getStatus(), txAmount, order.getAmount());
		PaymentOrder update = new PaymentOrder();
		update.setId(order.getId());
		update.setPayAddress(tx.getFrom());
		update.setTxId(tx.getTxid());
		update.setAlreadyGiven(order.getAlreadyGiven().add(txAmount));
		return paymentOrderMapper.updateById(update) > 0;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public Boolean collect(UserAddress userAddress, Transaction tx) {
		if (userAddress != null) {
			if (tx.getStatus()  == BlockchainStatus.SUCCESS) {
				userAddress.setCollectible(UserAddressCollectible.NO);
				userAddress.setAmount(BigDecimal.ZERO);
				userAddressMapper.updateById(userAddress);
				log.info("归集成功 {}", JSONUtil.toJsonStr(tx));
			}
			CollectRecord collectRecord = collectRecordMapper.selectOne(new LambdaQueryWrapper<CollectRecord>().eq(CollectRecord::getTxId, tx.getTxid()));
			if(collectRecord != null){
				collectRecord.setBlockNumber(tx.getBlockNum());
				collectRecord.setContractAddress(tx.getContractAddress());
				collectRecord.setBlockTime(tx.getTimestamp());
				collectRecord.setConfirmedNum(tx.getConfirmedNum());
				collectRecord.setStatus(tx.getStatus());
				collectRecord.setTxFee(tx.getTxGas());
				collectRecordMapper.updateById(collectRecord);
			}
			return true;
		}
		return false;
	}

}
