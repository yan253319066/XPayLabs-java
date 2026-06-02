package com.yan.xpay.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.yan.xpay.domain.MerchantAssetDetails;
import com.yan.xpay.domain.SimpleTransfer;
import com.yan.xpay.enums.AssetOperType;
import com.yan.xpay.enums.InOut;
import com.yan.xpay.mapper.MerchantAssetDetailsMapper;
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
import com.yan.xpay.domain.bo.MerchantAssetsBo;
import com.yan.xpay.domain.vo.MerchantAssetsVo;
import com.yan.xpay.domain.MerchantAssets;
import com.yan.xpay.mapper.MerchantAssetsMapper;
import com.yan.xpay.service.IMerchantAssetsService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商家资产Service业务层处理
 *
 * @author Yan
 * @date 2025-09-15
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantAssetsServiceImpl implements IMerchantAssetsService {

    private final MerchantAssetsMapper baseMapper;
    private final MerchantAssetDetailsMapper  assetDetailsMapper;
    private final LockTemplate lockTemplate;

    @Override
    public MerchantAssets getBalance(Long merchantId, String symbol) {
        Assert.notNull(merchantId);
        Assert.notBlank(symbol);
        MerchantAssets assets = baseMapper.selectOne(new LambdaQueryWrapper<MerchantAssets>().eq(MerchantAssets::getMerchantId, merchantId).eq(MerchantAssets::getSymbol, symbol));
        if(assets == null) {
            assets = new MerchantAssets();
            assets.setMerchantId(merchantId);
            assets.setSymbol(symbol);
            assets.setBalance(BigDecimal.ZERO);
            assets.setFrozenBalance(BigDecimal.ZERO);
            baseMapper.insert(assets);
            assets.setTotalBalance(BigDecimal.ZERO);
        }
        return assets;
    }

    @Override
    public MerchantAssetsVo getVoBalance(Long merchantId, String symbol) {
        Assert.notNull(merchantId);
        Assert.notBlank(symbol);
        MerchantAssetsVo assets = baseMapper.selectVoOne(new LambdaQueryWrapper<MerchantAssets>().eq(MerchantAssets::getMerchantId, merchantId).eq(MerchantAssets::getSymbol, symbol));
        return assets;
    }

    @Override
    public List<MerchantAssetsVo> getBalanceList(Long merchantId) {
        Assert.notNull(merchantId);
        List<MerchantAssetsVo> list = baseMapper.selectVoList(new LambdaQueryWrapper<MerchantAssets>().eq(MerchantAssets::getMerchantId, merchantId));
        if(CollUtil.isEmpty(list)) {
            MerchantAssets assets = new MerchantAssets();
            assets.setMerchantId(merchantId);
            assets.setSymbol("USDT");
            assets.setBalance(BigDecimal.ZERO);
            assets.setFrozenBalance(BigDecimal.ZERO);
            baseMapper.insert(assets);
            assets.setTotalBalance(BigDecimal.ZERO);
            list.add(BeanUtil.copyProperties(assets, MerchantAssetsVo.class));
        }
        return list;
    }

    @Override
    @Transactional
    public Boolean transfer(SimpleTransfer transfer) {
        String lockKey = "xpay:lock:merchant:" + transfer.getMerchantId() + ":" + transfer.getSymbol();
        // 获取分布式锁
        final LockInfo lockInfo = lockTemplate.lock(lockKey, 30000L, 5000L, RedissonLockExecutor.class);
        if (lockInfo == null) {
            throw new ServiceException("System is busy, please try again later.");
        }
        try {
            MerchantAssets assets = getBalance(transfer.getMerchantId(), transfer.getSymbol());

            MerchantAssetDetails tx = new MerchantAssetDetails();
            tx.setTransactionNo(transfer.getType().name() + "-" +transfer.getTransactionNo());
            tx.setMerchantId(transfer.getMerchantId());
            tx.setSymbol(transfer.getSymbol());
            tx.setAmount(transfer.getAmount());
            tx.setOldBalance(assets.getBalance());
            tx.setOldFrozen(assets.getFrozenBalance());
            tx.setType(transfer.getType());
            tx.setRemark(transfer.getRemark());
            tx.setFee(transfer.getFee());
            tx.setFeeRate(transfer.getFeeRate());
            tx.setFeeSymbol(transfer.getFeeSymbol());
            tx.setRate(transfer.getRate());
            tx.setNetwork(transfer.getNetwork());
            tx.setChain(transfer.getChain());

            setAssets(transfer.getType(), transfer.getAmount(), transfer.getFee(), assets, tx);
            int a = baseMapper.updateById(assets);
            Assert.isTrue(a > 0, "update balance error");

            tx.setNewBalance(assets.getBalance());
            tx.setNewFrozen(assets.getFrozenBalance());
            a = assetDetailsMapper.insert(tx);
            Assert.isTrue(a > 0, "insert tx error");
            return a > 0;
        }finally {
            lockTemplate.releaseLock(lockInfo);
        }
    }

    private void setAssets(AssetOperType type, BigDecimal amount, BigDecimal fee, MerchantAssets assets, MerchantAssetDetails tx) {
        switch (type) {
            case RECHARGE:
                assets.setBalance(assets.getBalance().add(amount));
//                assets.setTotalBalance(assets.getTotalBalance().add(amount));
                tx.setInOut(InOut.IN);
                break;
            case PAYIN, FIAT_CURRENCY_PAYIN:
                if(assets.getBalance().add(amount).subtract(fee).compareTo(BigDecimal.ZERO) < 0) {
                    log.error("not sufficient funds merchantId:{} symbol:{}  balance:{} amount:{} fee:{}", assets.getMerchantId(), assets.getSymbol(), assets.getBalance(), amount, fee);
                    throw  new ServiceException("not sufficient funds");
                }
                assets.setBalance(assets.getBalance().add(amount).subtract(fee));
//                assets.setTotalBalance(assets.getTotalBalance().add(amount).subtract(fee));
                tx.setInOut(InOut.IN);
                break;
            case WITHDRAW_REQUEST, PAYOUT_REQUEST, FIAT_CURRENCY_PAYOUT_REQUEST:
                if(assets.getBalance().subtract(amount).subtract(fee).compareTo(BigDecimal.ZERO) < 0) {
                    log.error("not sufficient funds merchantId:{} symbol:{}  balance:{} amount:{} fee:{}", assets.getMerchantId(), assets.getSymbol(), assets.getBalance(), amount, fee);
                    throw  new ServiceException("not sufficient funds");
                }
                assets.setBalance(assets.getBalance().subtract(amount).subtract(fee));
                assets.setFrozenBalance(assets.getFrozenBalance().add(amount).add(fee));
                tx.setInOut(InOut.TO_FROZEN);
                break;
            case WITHDRAW, PAYOUT, FIAT_CURRENCY_PAYOUT:
                if(assets.getFrozenBalance().subtract(amount).subtract(fee).compareTo(BigDecimal.ZERO) < 0) {
                    log.error("not sufficient funds merchantId:{} symbol:{} frozenBalance:{} amount:{} fee:{}", assets.getMerchantId(), assets.getSymbol(), assets.getFrozenBalance(), amount, fee);
                    throw  new ServiceException("not sufficient funds");
                }
                assets.setFrozenBalance(assets.getFrozenBalance().subtract(amount).subtract(fee));
//                assets.setTotalBalance(assets.getTotalBalance().subtract(amount).subtract(fee));
                tx.setInOut(InOut.OUT);
                break;
            case WITHDRAW_REFUND, PAYOUT_REFUND, FIAT_CURRENCY_PAYOUT_REFUND:
                assets.setBalance(assets.getBalance().add(amount).add(fee));
                assets.setFrozenBalance(assets.getFrozenBalance().subtract(amount).subtract(fee));
                tx.setInOut(InOut.TO_UNFROZEN);
        }


    }

    /**
     * 查询商家资产
     *
     * @param id 主键
     * @return 商家资产
     */
    @Override
    public MerchantAssetsVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商家资产列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家资产分页列表
     */
    @Override
    public TableDataInfo<MerchantAssetsVo> queryPageList(MerchantAssetsBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<MerchantAssets> lqw = buildQueryWrapper(bo);
        Page<MerchantAssetsVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商家资产列表
     *
     * @param bo 查询条件
     * @return 商家资产列表
     */
    @Override
    public List<MerchantAssetsVo> queryList(MerchantAssetsBo bo) {
        LambdaQueryWrapper<MerchantAssets> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<MerchantAssets> buildQueryWrapper(MerchantAssetsBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<MerchantAssets> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(MerchantAssets::getId);
        lqw.eq(bo.getMerchantId() !=null, MerchantAssets::getMerchantId, bo.getMerchantId());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), MerchantAssets::getSymbol, bo.getSymbol());
        lqw.eq(bo.getBalance() != null, MerchantAssets::getBalance, bo.getBalance());
        lqw.eq(bo.getFrozenBalance() != null, MerchantAssets::getFrozenBalance, bo.getFrozenBalance());
        lqw.eq(bo.getTotalBalance() != null, MerchantAssets::getTotalBalance, bo.getTotalBalance());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            MerchantAssets::getCreateTime ,params.get("beginCreateTime"), params.get("endCreateTime"));
        lqw.between(params.get("beginUpdateTime") != null && params.get("endUpdateTime") != null,
            MerchantAssets::getUpdateTime ,params.get("beginUpdateTime"), params.get("endUpdateTime"));
        return lqw;
    }

    /**
     * 新增商家资产
     *
     * @param bo 商家资产
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(MerchantAssetsBo bo) {
        MerchantAssets add = MapstructUtils.convert(bo, MerchantAssets.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商家资产
     *
     * @param bo 商家资产
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(MerchantAssetsBo bo) {
        MerchantAssets update = MapstructUtils.convert(bo, MerchantAssets.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(MerchantAssets entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商家资产信息
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
