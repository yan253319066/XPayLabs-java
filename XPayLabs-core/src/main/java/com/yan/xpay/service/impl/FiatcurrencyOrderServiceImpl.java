package com.yan.xpay.service.impl;

import cn.hutool.core.lang.Assert;
import com.yan.xpay.enums.FiatcurrencyOrderStatus;
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
import com.yan.xpay.domain.bo.FiatcurrencyOrderBo;
import com.yan.xpay.domain.vo.FiatcurrencyOrderVo;
import com.yan.xpay.domain.FiatcurrencyOrder;
import com.yan.xpay.mapper.FiatcurrencyOrderMapper;
import com.yan.xpay.service.IFiatcurrencyOrderService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 法币订单Service业务层处理
 *
 * @author Yan
 * @date 2025-10-10
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FiatcurrencyOrderServiceImpl implements IFiatcurrencyOrderService {

    private final FiatcurrencyOrderMapper baseMapper;

    @Override
    public List<FiatcurrencyOrderVo> queryUnfilledOrder(Collection<FiatcurrencyOrderStatus> coll) {
        LambdaQueryWrapper<FiatcurrencyOrder> lqw = Wrappers.lambdaQuery();
        lqw.in(FiatcurrencyOrder::getStatus, coll);
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public boolean saveFiatCurrency(FiatcurrencyOrderBo bo) {
        FiatcurrencyOrder add = MapstructUtils.convert(bo, FiatcurrencyOrder.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public boolean updateFiatCurrency(FiatcurrencyOrder order) {
        return baseMapper.updateById(order) > 0;
    }

    @Override
    public FiatcurrencyOrderVo queryByOrderNoAndMerchantId(String orderNo, Long merchantId) {
        Assert.notBlank(orderNo);
        Assert.notNull(merchantId);
        return baseMapper.selectVoOne(new LambdaQueryWrapper<FiatcurrencyOrder>().eq(FiatcurrencyOrder::getOrderNo, orderNo).eq(FiatcurrencyOrder::getMerchantId, merchantId));
    }

    /**
     * 查询法币订单
     *
     * @param id 主键
     * @return 法币订单
     */
    @Override
    public FiatcurrencyOrderVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询法币订单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 法币订单分页列表
     */
    @Override
    public TableDataInfo<FiatcurrencyOrderVo> queryPageList(FiatcurrencyOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<FiatcurrencyOrder> lqw = buildQueryWrapper(bo);
        Page<FiatcurrencyOrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的法币订单列表
     *
     * @param bo 查询条件
     * @return 法币订单列表
     */
    @Override
    public List<FiatcurrencyOrderVo> queryList(FiatcurrencyOrderBo bo) {
        LambdaQueryWrapper<FiatcurrencyOrder> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<FiatcurrencyOrder> buildQueryWrapper(FiatcurrencyOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<FiatcurrencyOrder> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(FiatcurrencyOrder::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), FiatcurrencyOrder::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getMerchantId() != null, FiatcurrencyOrder::getMerchantId, bo.getMerchantId());
        lqw.eq(bo.getAmount() != null, FiatcurrencyOrder::getAmount, bo.getAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getCurrency()), FiatcurrencyOrder::getCurrency, bo.getCurrency());
        lqw.like(StringUtils.isNotBlank(bo.getPayerName()), FiatcurrencyOrder::getPayerName, bo.getPayerName());
        lqw.eq(StringUtils.isNotBlank(bo.getPayerAccount()), FiatcurrencyOrder::getPayerAccount, bo.getPayerAccount());
        lqw.eq(StringUtils.isNotBlank(bo.getPayerPhone()), FiatcurrencyOrder::getPayerPhone, bo.getPayerPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getPayerEmail()), FiatcurrencyOrder::getPayerEmail, bo.getPayerEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getPayerCode()), FiatcurrencyOrder::getPayerCode, bo.getPayerCode());
        lqw.eq(StringUtils.isNotBlank(bo.getExtra()), FiatcurrencyOrder::getExtra, bo.getExtra());
        lqw.like(StringUtils.isNotBlank(bo.getPayeeName()), FiatcurrencyOrder::getPayeeName, bo.getPayeeName());
        lqw.eq(StringUtils.isNotBlank(bo.getPayeeAccount()), FiatcurrencyOrder::getPayeeAccount, bo.getPayeeAccount());
        lqw.eq(StringUtils.isNotBlank(bo.getPayeePhone()), FiatcurrencyOrder::getPayeePhone, bo.getPayeePhone());
        lqw.eq(StringUtils.isNotBlank(bo.getPayeeEmail()), FiatcurrencyOrder::getPayeeEmail, bo.getPayeeEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getPayeeCode()), FiatcurrencyOrder::getPayeeCode, bo.getPayeeCode());
        lqw.eq(bo.getStatus() != null, FiatcurrencyOrder::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getChannelCode()), FiatcurrencyOrder::getChannelCode, bo.getChannelCode());
        lqw.eq(StringUtils.isNotBlank(bo.getNotifyUrl()), FiatcurrencyOrder::getNotifyUrl, bo.getNotifyUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getThirdPartyResponse()), FiatcurrencyOrder::getThirdPartyResponse, bo.getThirdPartyResponse());
        lqw.eq(StringUtils.isNotBlank(bo.getCallbackContent()), FiatcurrencyOrder::getCallbackContent, bo.getCallbackContent());
        return lqw;
    }

    /**
     * 新增法币订单
     *
     * @param bo 法币订单
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(FiatcurrencyOrderBo bo) {
        FiatcurrencyOrder add = MapstructUtils.convert(bo, FiatcurrencyOrder.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改法币订单
     *
     * @param bo 法币订单
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(FiatcurrencyOrderBo bo) {
        FiatcurrencyOrder update = MapstructUtils.convert(bo, FiatcurrencyOrder.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(FiatcurrencyOrder entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除法币订单信息
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
