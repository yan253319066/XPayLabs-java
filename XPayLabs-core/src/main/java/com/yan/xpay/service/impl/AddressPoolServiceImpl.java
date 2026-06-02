package com.yan.xpay.service.impl;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.AddressType;
import com.yan.xpay.enums.Chain;
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
import com.yan.xpay.domain.bo.AddressPoolBo;
import com.yan.xpay.domain.vo.AddressPoolVo;
import com.yan.xpay.domain.AddressPool;
import com.yan.xpay.mapper.AddressPoolMapper;
import com.yan.xpay.service.IAddressPoolService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collection;

/**
 * 地址池管理Service业务层处理
 *
 * @author Yan
 * @date 2025-07-12
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AddressPoolServiceImpl implements IAddressPoolService {

    private final AddressPoolMapper baseMapper;
    private final LockTemplate lockTemplate;

    @Transactional
    @Override
    public String getUnAddress(Chain chain) {
        String lockKey = "lock:unAddress:" + chain.name();
        LockInfo lockInfo = lockTemplate.lock(lockKey, 30000L, 5000L, RedissonLockExecutor.class);
        if (lockInfo == null) {
            throw new ServiceException("Address is being allocated, please try again later");
        }
        try {
            // 执行抢占逻辑
            int updated = baseMapper.update(
                null,
                new LambdaUpdateWrapper<AddressPool>()
                    .set(AddressPool::getUsed, AddressStatus.USED)
                    .eq(AddressPool::getChain, chain)
                    .eq(AddressPool::getType, AddressType.GENERAL)
                    .eq(AddressPool::getUsed, AddressStatus.UNUSED)
                    .orderByAsc(AddressPool::getId)
                    .last("LIMIT 1")
            );

            if (updated > 0) {
                // 查询刚刚抢到的地址（可以用最大的 ID 或刚刚更新的行）
                AddressPool candidate = baseMapper.selectOne(
                    new LambdaQueryWrapper<AddressPool>()
                        .eq(AddressPool::getChain, chain)
                        .eq(AddressPool::getType, AddressType.GENERAL)
                        .eq(AddressPool::getUsed, AddressStatus.USED)
                        .orderByDesc(AddressPool::getId)
                        .last("LIMIT 1")
                );
                return candidate.getAddress();
            }
            return null;
        } finally {
            lockTemplate.releaseLock(lockInfo);
        }
    }

    @Override
    public AddressPool getUserAddress(Chain chain, String toAddress) {
        return baseMapper.selectOne(
            new LambdaQueryWrapper<AddressPool>()
                .eq(AddressPool::getType, AddressType.GENERAL)
                .eq(AddressPool::getChain, chain)
                .eq(AddressPool::getAddress, toAddress));
    }

    @Override
    public AddressPool getPlatformHotAddress(Chain chain) {
        AddressPool addressPool = baseMapper.selectOne(new LambdaQueryWrapper<AddressPool>().eq(AddressPool::getChain, chain).eq(AddressPool::getType,
            AddressType.HOT));
        return addressPool;
    }

    /**
     * 查询地址池管理
     *
     * @param id 主键
     * @return 地址池管理
     */
    @Override
    public AddressPoolVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询地址池管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 地址池管理分页列表
     */
    @Override
    public TableDataInfo<AddressPoolVo> queryPageList(AddressPoolBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AddressPool> lqw = buildQueryWrapper(bo);
        Page<AddressPoolVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的地址池管理列表
     *
     * @param bo 查询条件
     * @return 地址池管理列表
     */
    @Override
    public List<AddressPoolVo> queryList(AddressPoolBo bo) {
        LambdaQueryWrapper<AddressPool> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<AddressPool> buildQueryWrapper(AddressPoolBo bo) {
//        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<AddressPool> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(AddressPool::getId);
        lqw.eq(bo.getChain() != null, AddressPool::getChain, bo.getChain());
        lqw.eq(StringUtils.isNotBlank(bo.getAddress()), AddressPool::getAddress, bo.getAddress());
        lqw.eq(StringUtils.isNotBlank(bo.getPath()), AddressPool::getPath, bo.getPath());
        lqw.eq(bo.getUsed() != null, AddressPool::getUsed, bo.getUsed());
        return lqw;
    }

    /**
     * 新增地址池管理
     *
     * @param bo 地址池管理
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(AddressPoolBo bo) {
        AddressPool add = MapstructUtils.convert(bo, AddressPool.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改地址池管理
     *
     * @param bo 地址池管理
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(AddressPoolBo bo) {
        AddressPool update = MapstructUtils.convert(bo, AddressPool.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(AddressPool entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除地址池管理信息
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
