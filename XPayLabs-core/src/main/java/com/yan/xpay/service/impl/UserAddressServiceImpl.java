package com.yan.xpay.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.vo.PendingCollectionVO;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.service.IAddressPoolService;
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
import com.yan.xpay.domain.bo.UserAddressBo;
import com.yan.xpay.domain.vo.UserAddressVo;
import com.yan.xpay.domain.UserAddress;
import com.yan.xpay.mapper.UserAddressMapper;
import com.yan.xpay.service.IUserAddressService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Optional;

/**
 * 用户地址Service业务层处理
 *
 * @author Yan
 * @date 2025-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserAddressServiceImpl implements IUserAddressService {

    private final UserAddressMapper baseMapper;
    private final IAddressPoolService addressPoolService;
    private final LockTemplate lockTemplate;
    private final AssetTypeCache assetTypeCache;

    @Transactional
    @Override
    public String getUserAddress(Chain chain, String symbol, Long merchantId, String userId) {
        Assert.notBlank(userId, "UserId cannot be blank");
        Assert.notNull(chain, "Chain cannot be null");
        Assert.notBlank(symbol, "Symbol cannot be blank");
        Assert.notNull(merchantId, "MerchantId cannot be null");

        String lockKey = "xpay:lock:address:" + chain.name() + ":" + symbol;

        // 获取分布式锁
        final LockInfo lockInfo = lockTemplate.lock(lockKey, 30000L, 5000L, RedissonLockExecutor.class);
        if (lockInfo == null) {
            throw new ServiceException("System is busy, please try again later.");
        }

        try {
            LambdaQueryWrapper<UserAddress> lqw = new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getChain, chain)
                .eq(UserAddress::getUserId, userId);
            if(userId.equals("0")) lqw.eq(UserAddress::getStatus, AddressStatus.UNUSED);
            if(!userId.equals("0")) lqw.eq(UserAddress::getMerchantId, merchantId);//V2版本需要加上merchantId查询
            // 1. 尝试复用已有地址
            List<UserAddress> existingAddresses = baseMapper.selectList(lqw);

            if (CollUtil.isNotEmpty(existingAddresses)) {
                // 查找是否已有目标symbol的记录
                Optional<UserAddress> matched = existingAddresses.stream()
                    .filter(ua -> symbol.equals(ua.getSymbol()))
                    .findFirst();

                if (matched.isPresent()) {
                    // 直接标记为USED
                    UserAddress ua = matched.get();
                    if(userId.equals("0")) {
                        int updated = baseMapper.update(
                            null,
                            new LambdaUpdateWrapper<UserAddress>()
                                .set(UserAddress::getStatus, AddressStatus.USED)
                                .set(UserAddress::getMerchantId, merchantId)
                                .eq(UserAddress::getId, ua.getId())
                                .eq(UserAddress::getUserId, "0")
                                .eq(UserAddress::getStatus, AddressStatus.UNUSED)
                        );
                        if (updated > 0) {
                            return ua.getAddress();
                        }
                    }else {
                        return ua.getAddress();
                    }
                }
            }

            // 2. 分配新地址（从地址池抢占）
            String address = addressPoolService.getUnAddress(chain);
            if(StrUtil.isEmpty(address)) throw new ServiceException("No available address is available at the moment. Please try again later.");

            assetTypeCache.getAssetsByChain(chain).forEach(assetType -> {
                // 抢占成功，写入用户地址表
                UserAddress userAddress = new UserAddress();
                userAddress.setMerchantId(merchantId);
                userAddress.setUserId(userId);
                userAddress.setAddress(address);
                userAddress.setChain(assetType.getChain());
                userAddress.setSymbol(assetType.getSymbol());
                if(assetType.getSymbol().equals(symbol))
                    userAddress.setStatus(AddressStatus.USED);
                else userAddress.setStatus(AddressStatus.UNUSED);
                baseMapper.insert(userAddress);
            });
            return address;

        } finally {
            lockTemplate.releaseLock(lockInfo); // 释放锁
        }
    }

    @Transactional
    @Override
    public String getUserAddress(Chain chain, String symbol, Long merchantId) {
        return getUserAddress(chain, symbol, merchantId, "0");
    }


    @Override
    public UserAddress getUserAddress(Chain chain, String symbol, String address) {
        Assert.notNull(chain);
        Assert.notBlank(symbol);
        Assert.notBlank(address);
        return baseMapper.selectOne(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getChain, chain).eq(UserAddress::getSymbol, symbol).eq(UserAddress::getAddress, address));
    }

    @Override
    public List<PendingCollectionVO> getPendingCollectionBalances(Long merchantId) {
        Assert.notNull(merchantId);
        return baseMapper.getPendingCollectionBalances(merchantId);
    }

    /**
     * 查询用户地址
     *
     * @param id 主键
     * @return 用户地址
     */
    @Override
    public UserAddressVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户地址列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户地址分页列表
     */
    @Override
    public TableDataInfo<UserAddressVo> queryPageList(UserAddressBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<UserAddress> lqw = buildQueryWrapper(bo);
        Page<UserAddressVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户地址列表
     *
     * @param bo 查询条件
     * @return 用户地址列表
     */
    @Override
    public List<UserAddressVo> queryList(UserAddressBo bo) {
        LambdaQueryWrapper<UserAddress> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<UserAddress> buildQueryWrapper(UserAddressBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<UserAddress> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(UserAddress::getId);
        lqw.eq(bo.getMerchantId() != null, UserAddress::getMerchantId, bo.getMerchantId());
        lqw.eq(StringUtils.isNotBlank(bo.getUserId()), UserAddress::getUserId, bo.getUserId());
        lqw.eq(bo.getChain() != null, UserAddress::getChain, bo.getChain());
        lqw.eq(StringUtils.isNotBlank(bo.getAddress()), UserAddress::getAddress, bo.getAddress());
        lqw.eq(bo.getCollectible() != null, UserAddress::getCollectible, bo.getCollectible());
        return lqw;
    }

    /**
     * 新增用户地址
     *
     * @param bo 用户地址
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(UserAddressBo bo) {
        UserAddress add = MapstructUtils.convert(bo, UserAddress.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户地址
     *
     * @param bo 用户地址
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(UserAddressBo bo) {
        UserAddress update = MapstructUtils.convert(bo, UserAddress.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean updateById(UserAddress userAddress) {
        return baseMapper.updateById(userAddress) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(UserAddress entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户地址信息
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
