package com.yan.xpay.service;

import com.yan.xpay.domain.*;

/**
 * 区块链数据同步修改数据库
 */
public interface BlockchainDataSyncService {
	/**
	 * 商家充值pending
	 * @param tx
	 * @param merchantId
	 * @return
	 */
	Boolean merchantRecharge(Transaction tx, Long merchantId);

	/**
	 * 商家提现pengding
	 * @param tx
	 * @param withdraw
	 * @return
	 */
	Boolean merchantWithdrawal(Transaction tx, MerchantRechargeWithdraw withdraw);

	/**
	 * 代收链上数据已打包pending状态
	 * @param tx
	 * @param order
	 * @return
	 */
	Boolean paymentPending(Transaction tx, PaymentOrder order);

	/**
	 * 确认代收代付链上数据已完成
	 * @param order
	 * @param tx
	 * @return
	 */
	Boolean completeTransaction(PaymentOrder order, Transaction tx);

	/**
	 * 代收用户少付记录
	 */
	Boolean alreadyGivenTransaction(PaymentOrder order, Transaction tx, AssetType assetType);

	/**
	 * 归集
	 * @param userAddress
	 * @param tx
     * @param assetType
	 * @return
	 */
	Boolean collect(UserAddress userAddress, Transaction tx, AssetType assetType);

}
