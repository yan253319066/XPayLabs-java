package com.yan.xpay.service;

import com.yan.xpay.domain.NotifyOrder;
import com.yan.xpay.domain.PaymentOrder;
import com.yan.xpay.domain.req.BaseCreateOrderReq;
import com.yan.xpay.domain.req.CreateCollectionOrderReq;
import com.yan.xpay.domain.req.CreatePayoutOrderReq;
import com.yan.xpay.domain.req.ReqPayload;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.domain.vo.PayinOrderVo;
import com.yan.xpay.domain.vo.PaymentAddress;
import com.yan.xpay.domain.vo.PaymentOrderVo;
import com.yan.xpay.domain.bo.PaymentOrderBo;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.OrderStatus;
import com.yan.xpay.enums.OrderType;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 通用支付订单Service接口
 *
 * @author Yan
 * @date 2025-07-12
 */
public interface IPaymentOrderService {

    PayinOrderVo getPayinByOrderId(String orderId, String sign);
    OrderStatus getPayinOrderStatus(String orderId, String sign);

    /**
     *  设置订单过期
     * @param order
     * @return
     */
    Boolean setOrderExpired(PaymentOrder order);
    /**
     * 查找初始收款订单
     * @param chain
     * @param symbol
     * @param address
     * @return
     */
    List<PaymentOrder> getInitPayinByAddress(Chain chain, String symbol, String address);
    List<PaymentOrder> getInitPayoutByAddress(Chain chain, String symbol, String address);

    /**
     * 按照txid查找收款订单
     * @param chain
     * @param symbol
     * @param txid
     * @return
     */
    PaymentOrder getPayoutByTxid(Chain chain, String symbol, String txid);

    /**
     * 查找收款未完成订单
     * @param chain
     * @return
     */
    Map<String, List<PaymentOrder>> getPendingPayinGroupedByAddress(Chain chain);

    /**
     * 查找付款未完成订单
     * @param chain
     * @return
     */
    Map<String, List<PaymentOrder>> getPendingPayoutGroupedByAddress(Chain chain);
    List<PaymentOrder> getPendingOrders(Chain chain, OrderType type);
    List<PaymentOrder> getPendingOrders(Collection<Chain> chains, OrderType type);
    List<PaymentOrder> getPayoutInit(Collection<Chain> chains);
    /**
     * 创建订单
     * @param type
     * @param req
     * @param merchant
     * @return
     */
    PaymentAddress createOrder(OrderType type, BaseCreateOrderReq req, MerchantVo merchant);

    /**
     * 创建收款
     * @param req
     * @param merchant
     * @return
     */
    PaymentAddress createCollection(ReqPayload<CreateCollectionOrderReq> req, MerchantVo merchant);

    /**
     * 创建付款
     * @param req
     * @param merchant
     * @return
     */
    PaymentAddress createPayout(ReqPayload<CreatePayoutOrderReq> req, MerchantVo merchant);

    /**
     * 按订单ID获取订单状态
     * @param orderId
     * @return
     */
    NotifyOrder getStatus(String orderId);

    /**
     * 查询通用支付订单
     *
     * @param id 主键
     * @return 通用支付订单
     */
    PaymentOrderVo queryById(Long id);

    /**
     * 分页查询通用支付订单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 通用支付订单分页列表
     */
    TableDataInfo<PaymentOrderVo> queryPageList(PaymentOrderBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的通用支付订单列表
     *
     * @param bo 查询条件
     * @return 通用支付订单列表
     */
    List<PaymentOrderVo> queryList(PaymentOrderBo bo);

    /**
     * 新增通用支付订单
     *
     * @param bo 通用支付订单
     * @return 是否新增成功
     */
    Boolean insertByBo(PaymentOrderBo bo);

    /**
     * 修改通用支付订单
     *
     * @param bo 通用支付订单
     * @return 是否修改成功
     */
    Boolean updateByBo(PaymentOrderBo bo);

    /**
     * 校验并批量删除通用支付订单信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
