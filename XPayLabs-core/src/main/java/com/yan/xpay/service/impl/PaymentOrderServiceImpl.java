package com.yan.xpay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.config.XPayConfig;
import com.yan.xpay.domain.*;
import com.yan.xpay.domain.req.BaseCreateOrderReq;
import com.yan.xpay.domain.req.CreateCollectionOrderReq;
import com.yan.xpay.domain.req.CreatePayoutOrderReq;
import com.yan.xpay.domain.req.ReqPayload;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.domain.vo.PayinOrderVo;
import com.yan.xpay.domain.vo.PaymentAddress;
import com.yan.xpay.enums.*;
import com.yan.xpay.mapper.TxRecordMapper;
import com.yan.xpay.service.IMerchantAssetsService;
import com.yan.xpay.service.IUserAddressService;
import com.yan.xpay.utils.FeeUtils;
import com.yan.xpay.utils.SkipSign;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.yan.xpay.domain.bo.PaymentOrderBo;
import com.yan.xpay.domain.vo.PaymentOrderVo;
import com.yan.xpay.mapper.PaymentOrderMapper;
import com.yan.xpay.service.IPaymentOrderService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 通用支付订单Service业务层处理
 *
 * @author Yan
 * @date 2025-07-12
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentOrderServiceImpl implements IPaymentOrderService {

    private final PaymentOrderMapper baseMapper;
    private final AssetTypeCache assetTypeCache;
    private final TxRecordMapper txRecordMapper;
    private final IUserAddressService userAddressService;
    private final XPayConfig xPayConfig;
    private final IMerchantAssetsService merchantAssetsService;

    @Override
    public PayinOrderVo getPayinByOrderId(String orderId, String sign) {
        Assert.notBlank(orderId, "Order ID is blank");
        Assert.notBlank(sign, "Sign is blank");
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        if(!SkipSign.verify(params, sign)) throw new ServiceException("Sign error");
        PaymentOrder order = baseMapper.selectOne(new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getMerchantOrderId, orderId).eq(PaymentOrder::getOrderType, OrderType.COLLECTION));
        if(order == null) throw new ServiceException("Order not found");
        PayinOrderVo payinOrderVo = new PayinOrderVo();
        if(StrUtil.isNotBlank(sign))
            payinOrderVo.setReason(order.getReason());
        payinOrderVo.setExpiredTime(order.getExpiredTime());
        payinOrderVo.setAddress(order.getReceiveAddress());
        payinOrderVo.setOrderId(order.getMerchantOrderId());
        payinOrderVo.setChain(order.getChain());
        payinOrderVo.setSymbol(order.getSymbol());
        payinOrderVo.setStatus(order.getStatus());
        payinOrderVo.setAmount(order.getAmount());
        payinOrderVo.setActualAmount(order.getActualAmount());
        return payinOrderVo;
    }

    @Override
    public OrderStatus getPayinOrderStatus(String orderId, String sign) {
        Assert.notBlank(orderId, "Order ID is blank");
        Assert.notBlank(sign, "Sign is blank");
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        if(!SkipSign.verify(params, sign)) throw new ServiceException("Sign error");
        PaymentOrder order = baseMapper.selectOne(new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getMerchantOrderId, orderId).eq(PaymentOrder::getOrderType, OrderType.COLLECTION));
        if(order == null) throw new ServiceException("Order not found");
        return order.getStatus();
    }

    @Transactional
    @Override
    public Boolean setOrderExpired(PaymentOrder order) {
        order.setStatus(OrderStatus.EXPIRED);
        baseMapper.updateById(order);
        if(order.getOrderType() == OrderType.COLLECTION){
            UserAddress userAddress = userAddressService.getUserAddress(order.getChain(), order.getSymbol(), order.getReceiveAddress());
            if(userAddress != null &&  userAddress.getStatus() == AddressStatus.USED) {
                userAddress.setStatus(AddressStatus.UNUSED);
                userAddressService.updateById(userAddress);
            }
        }
        return true;
    }

    @Override
    public List<PaymentOrder> getInitPayinByAddress(Chain chain, String symbol, String address) {
        return baseMapper.selectList(
            new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getReceiveAddress, address)
                .in(PaymentOrder::getStatus, OrderStatus.INIT, OrderStatus.PENDING_CONFIRMATION, OrderStatus.PENDING)
                .eq(PaymentOrder::getChain, chain)
                .eq(PaymentOrder::getSymbol, symbol)
                .eq(PaymentOrder::getOrderType, OrderType.COLLECTION));
    }

    @Override
    public List<PaymentOrder> getInitPayoutByAddress(Chain chain, String symbol, String address) {
        return baseMapper.selectList(
            new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getPayAddress, address)
                .in(PaymentOrder::getStatus, OrderStatus.INIT, OrderStatus.PENDING_CONFIRMATION, OrderStatus.PENDING)
                .eq(PaymentOrder::getChain, chain)
                .eq(PaymentOrder::getSymbol, symbol)
                .eq(PaymentOrder::getOrderType, OrderType.PAYOUT));
    }

    @Override
    public PaymentOrder getPayoutByTxid(Chain chain, String symbol, String txid) {
        return baseMapper.selectOne(new LambdaQueryWrapper<PaymentOrder>()
            .eq(PaymentOrder::getTxId, txid)
            .eq(PaymentOrder::getChain, chain)
            .eq(PaymentOrder::getSymbol, symbol)
            .eq(PaymentOrder::getOrderType, OrderType.PAYOUT));
    }

    @Override
    public Map<String, List<PaymentOrder>> getPendingPayinGroupedByAddress(Chain chain) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<PaymentOrder>()
                    .in(PaymentOrder::getStatus, OrderStatus.PENDING, OrderStatus.PENDING_CONFIRMATION)
                    .eq(PaymentOrder::getChain, chain)
                    .eq(PaymentOrder::getOrderType, OrderType.COLLECTION))
            .stream()
            .collect(Collectors.groupingBy(
                order -> order.getReceiveAddress().toLowerCase()
            ));
    }

    @Override
    public Map<String, List<PaymentOrder>> getPendingPayoutGroupedByAddress(Chain chain) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<PaymentOrder>()
                    .in(PaymentOrder::getStatus, OrderStatus.PENDING, OrderStatus.PENDING_CONFIRMATION)
                    .eq(PaymentOrder::getChain, chain)
                    .eq(PaymentOrder::getOrderType, OrderType.PAYOUT))
            .stream()
            .collect(Collectors.groupingBy(
                order -> order.getReceiveAddress().toLowerCase()
            ));
    }

    @Override
    public List<PaymentOrder> getPendingOrders(Chain chain, OrderType type) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<PaymentOrder>()
                    .eq(PaymentOrder::getStatus, OrderStatus.PENDING).eq(PaymentOrder::getChain, chain).eq(PaymentOrder::getOrderType, type));
    }

    @Override
    public List<PaymentOrder> getPendingOrders(Collection<Chain> chains, OrderType type) {
        return baseMapper.selectList(
            new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getStatus, OrderStatus.PENDING).in(PaymentOrder::getChain, chains).eq(PaymentOrder::getOrderType, type));
    }

    @Override
    public List<PaymentOrder> getPayoutInit(Collection<Chain> chains) {
        return baseMapper.selectList(
            new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getStatus, OrderStatus.INIT).in(PaymentOrder::getChain, chains).eq(PaymentOrder::getOrderType, OrderType.PAYOUT));
    }

    @Transactional
    @Override
    public PaymentAddress createOrder(OrderType type, BaseCreateOrderReq req, MerchantVo merchantVo) {

        AssetType assetType = assetTypeCache.getBySymbol(req.getChain(), req.getSymbol());

        if (assetType == null) throw new ServiceException("Unsupported symbol or chain");

        if(merchantVo.getAccountType() == MerchantAccountType.MAIN) {
            if(assetType.getNetwork() == BlockchainNetwork.TEST) {
                log.warn("正式账户不支持测试网络创建支付订单 merchant name {}", merchantVo.getName());
                throw new ServiceException("Production accounts do not support test networks.");
            }
        }else {
            if(assetType.getNetwork() == BlockchainNetwork.MAIN && type == OrderType.PAYOUT) {
                log.warn("测试账户不支持正式网络创建代付订单 merchant name {}", merchantVo.getName());
                throw new ServiceException("Test accounts do not support main networks.");
            }
        }

        // 创建订单
        String orderId = req.getOrderId();
        if(StrUtil.isNotBlank(req.getOrderId())){
            if(baseMapper.exists(new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getMerchantOrderId, req.getOrderId()).eq(PaymentOrder::getMerchantId, merchantVo.getId()))) throw new ServiceException("Order ID already exists.");
        }else {
            Snowflake snowflake = IdUtil.getSnowflake(1, 1);
            orderId = snowflake.nextIdStr();
        }

        PaymentOrder order = new PaymentOrder();
        order.setOrderType(type);
        order.setMerchantOrderId(orderId);
        order.setUid(req.getUid());
        order.setMerchantId(merchantVo.getId());
        order.setAssetTypeId(assetType.getId());
        order.setChain(assetType.getChain());
        order.setSymbol(assetType.getSymbol());
        order.setAmount(req.getAmount());

        BigDecimal fee = BigDecimal.ZERO;

        if(type == OrderType.PAYOUT) {
            if(req instanceof CreatePayoutOrderReq){
                String receiveAddress = ((CreatePayoutOrderReq) req).getReceiveAddress();
                order.setReceiveAddress(receiveAddress);
            }
            if(merchantVo.getMerchantSysVersion() == MerchantSysVersion.V3){
                boolean isNativeToken = assetTypeCache.isNativeToken(req.getChain(), req.getSymbol());
                fee = FeeUtils.getPlatformFee(isNativeToken, req.getChain(), req.getSymbol(), order.getAmount(), merchantVo.getFeeRatio(), order.getReceiveAddress());

                MerchantAssets merchantAssets =  merchantAssetsService.getBalance(merchantVo.getId(), order.getSymbol());
                if(merchantAssets.getBalance().compareTo(order.getAmount().add(fee)) < 0) throw new ServiceException(" Insufficient funds. balance: "+merchantAssets.getBalance()+" Total required: "+order.getAmount().add(fee)+" ("+order.getAmount()+" amount + "+fee+" fee).");

                SimpleTransfer simpleTransfer = new SimpleTransfer();
                simpleTransfer.setAmount(order.getAmount());
                simpleTransfer.setTransactionNo(orderId);
                simpleTransfer.setType(AssetOperType.PAYOUT_REQUEST);
                simpleTransfer.setRate(BigDecimal.ZERO);
                simpleTransfer.setMerchantId(merchantVo.getId());
                simpleTransfer.setRemark("商家付款申请");
                simpleTransfer.setFeeRate(merchantVo.getFeeRatio());
                simpleTransfer.setFee(fee);
                simpleTransfer.setFeeSymbol(order.getSymbol());
                simpleTransfer.setSymbol(order.getSymbol());
                simpleTransfer.setChain(order.getChain());
                simpleTransfer.setNetwork(assetType.getNetwork().name());
                merchantAssetsService.transfer(simpleTransfer);
            }else {
                fee = FeeUtils.getPlatformFee(order.getAmount(), merchantVo.getFeeRatio());
            }
        }
        else{
            fee = FeeUtils.getPlatformFee(order.getAmount(), merchantVo.getFeeRatio());
            if(merchantVo.getGeneratedAddressType() == GeneratedAddressType.ORDER){
                order.setReceiveAddress(userAddressService.getUserAddress(req.getChain(), req.getSymbol(), merchantVo.getId()));
            }else
                order.setReceiveAddress(userAddressService.getUserAddress(req.getChain(), req.getSymbol(), merchantVo.getId(), req.getUid()));
        }

        if(fee.compareTo(BigDecimal.ZERO) > 0) {
            order.setHandingRate(merchantVo.getFeeRatio());
            order.setHandingFee(fee);
        }else {
            order.setHandingRate(BigDecimal.ZERO);
            order.setHandingFee(BigDecimal.ZERO);
        }

        order.setNotifyStatus(NotifyStatus.INIT);
        order.setCallbackUrl(merchantVo.getCallbackUrl());
        Long timestamp = DateUtil.currentSeconds() + xPayConfig.getOrderExpiredTime();
        order.setExpiredTime(timestamp);
        order.setCreateTime(DateUtil.date());
        order.setUpdateTime(DateUtil.date());
        order.setStatus(OrderStatus.INIT);
        baseMapper.insert(order);


        PaymentAddress paymentAddress = new PaymentAddress();
        paymentAddress.setAddress(order.getReceiveAddress());
        paymentAddress.setChain(order.getChain());
        paymentAddress.setAmount(order.getAmount().toPlainString());
        paymentAddress.setSymbol(order.getSymbol());
        paymentAddress.setOrderId(order.getMerchantOrderId());
        paymentAddress.setUid(order.getUid());
        paymentAddress.setExpiredTime(order.getExpiredTime());
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", order.getMerchantOrderId());
        String payDomain = xPayConfig.getPayDomain();
        if(StrUtil.isBlank(payDomain)) payDomain = "";
        paymentAddress.setPaymentUrl(payDomain + "?orderId="+order.getMerchantOrderId()+"&sign="+ SkipSign.sign(params));
        return paymentAddress;
    }

    @Transactional
    @Override
    public PaymentAddress createCollection(ReqPayload<CreateCollectionOrderReq> payload, MerchantVo merchant) {
        return createOrder(OrderType.COLLECTION, payload.getData(), merchant);
    }

    @Transactional
    @Override
    public PaymentAddress createPayout(ReqPayload<CreatePayoutOrderReq> payload, MerchantVo merchant) {
        if(payload.getData().getAmount().compareTo(new BigDecimal("1000")) > 0) throw new ServiceException("The maximum payment amount is 1,000 USDT");
        return createOrder(OrderType.PAYOUT, payload.getData(), merchant);
    }

    @Override
    public NotifyOrder getStatus(String orderId) {
        Assert.notBlank(orderId);
        PaymentOrder paymentOrder = baseMapper.selectOne(new LambdaQueryWrapper<PaymentOrder>()
            .eq(PaymentOrder::getMerchantOrderId, orderId));
        TxRecord txRecord = null;
        if(paymentOrder != null) {
            txRecord = txRecordMapper.selectOne(new LambdaQueryWrapper<TxRecord>().eq(TxRecord::getOrderId, paymentOrder.getMerchantOrderId()));
        }else throw new ServiceException("Order not found "+orderId);
        AssetType assetType = assetTypeCache.getBySymbol(paymentOrder.getChain(), paymentOrder.getSymbol());
        NotifyOrder notifyOrder = new NotifyOrder();
        notifyOrder.setOrderId(paymentOrder.getMerchantOrderId());
        notifyOrder.setUid(paymentOrder.getUid());
        notifyOrder.setOrderType(paymentOrder.getOrderType());
        notifyOrder.setReason(paymentOrder.getReason());
        notifyOrder.setStatus(paymentOrder.getStatus());
        notifyOrder.setAmount(paymentOrder.getAmount());
        notifyOrder.setActualAmount(paymentOrder.getActualAmount());
        notifyOrder.setFee(paymentOrder.getHandingFee());
        notifyOrder.setTransaction(Transaction.getTransaction(paymentOrder, txRecord, assetType.getDecimals()));
//        log.info("get status {}", notifyOrder);
        return notifyOrder;
    }

    /**
     * 查询通用支付订单
     *
     * @param id 主键
     * @return 通用支付订单
     */
    @Override
    public PaymentOrderVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询通用支付订单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 通用支付订单分页列表
     */
    @Override
    public TableDataInfo<PaymentOrderVo> queryPageList(PaymentOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<PaymentOrder> lqw = buildQueryWrapper(bo);
        Page<PaymentOrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的通用支付订单列表
     *
     * @param bo 查询条件
     * @return 通用支付订单列表
     */
    @Override
    public List<PaymentOrderVo> queryList(PaymentOrderBo bo) {
        LambdaQueryWrapper<PaymentOrder> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<PaymentOrder> buildQueryWrapper(PaymentOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<PaymentOrder> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(PaymentOrder::getCreateTime);
        lqw.eq(bo.getMerchantId() != null, PaymentOrder::getMerchantId, bo.getMerchantId());
        lqw.eq(StringUtils.isNotBlank(bo.getMerchantOrderId()), PaymentOrder::getMerchantOrderId, bo.getMerchantOrderId());
        lqw.eq(bo.getAssetTypeId() != null, PaymentOrder::getAssetTypeId, bo.getAssetTypeId());
        lqw.eq(StringUtils.isNotBlank(bo.getPayAddress()), PaymentOrder::getPayAddress, bo.getPayAddress());
        lqw.eq(StringUtils.isNotBlank(bo.getReceiveAddress()), PaymentOrder::getReceiveAddress, bo.getReceiveAddress());
        lqw.eq(bo.getAmount() != null, PaymentOrder::getAmount, bo.getAmount());
        lqw.eq(bo.getStatus() != null, PaymentOrder::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getTxId()), PaymentOrder::getTxId, bo.getTxId());
        lqw.eq(bo.getNotifyStatus() != null, PaymentOrder::getNotifyStatus, bo.getNotifyStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getCallbackUrl()), PaymentOrder::getCallbackUrl, bo.getCallbackUrl());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            PaymentOrder::getCreateTime ,params.get("beginCreateTime"), params.get("endCreateTime"));
//        lqw.between(params.get("beginNotifyTime") != null && params.get("endNotifyTime") != null,
//            PaymentOrder::getNotifyTime ,params.get("beginNotifyTime"), params.get("endNotifyTime"));
        return lqw;
    }

    /**
     * 新增通用支付订单
     *
     * @param bo 通用支付订单
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(PaymentOrderBo bo) {
        PaymentOrder add = MapstructUtils.convert(bo, PaymentOrder.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改通用支付订单
     *
     * @param bo 通用支付订单
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(PaymentOrderBo bo) {
        PaymentOrder update = MapstructUtils.convert(bo, PaymentOrder.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(PaymentOrder entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除通用支付订单信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
