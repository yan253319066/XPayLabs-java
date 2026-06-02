package com.yan.xpay.service.impl;

import cn.hutool.core.lang.Assert;
import com.yan.xpay.domain.AssetType;
import com.yan.xpay.domain.vo.MerchantAddressVo2;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.service.IAssetTypeService;
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
import com.yan.xpay.domain.bo.MerchantAddressBo;
import com.yan.xpay.domain.vo.MerchantAddressVo;
import com.yan.xpay.domain.MerchantAddress;
import com.yan.xpay.mapper.MerchantAddressMapper;
import com.yan.xpay.service.IMerchantAddressService;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商家钱包地址Service业务层处理
 *
 * @author Yan
 * @date 2025-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantAddressServiceImpl implements IMerchantAddressService {

    private final MerchantAddressMapper baseMapper;
    private final IAssetTypeService assetTypeService;

    @Override
    public List<MerchantAddressVo> getHotAddressByAddress(Chain chain, String symbol, String address) {
        return baseMapper.selectVoList(new LambdaQueryWrapper<MerchantAddress>().eq(MerchantAddress::getHotAddress, address).eq(MerchantAddress::getChain, chain).eq(MerchantAddress::getSymbol, symbol));
    }

    @Override
    public MerchantAddressVo getColdAddressByAddress(Chain chain, String symbol, String address) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<MerchantAddress>().eq(MerchantAddress::getColdAddress, address).eq(MerchantAddress::getChain, chain).eq(MerchantAddress::getSymbol, symbol));
    }

    // 获取不重复的cold_address
    public Map<String, MerchantAddressVo2> getUniqueColdAddresses() {
        return baseMapper.findDistinctColdAddresses()
            .stream()
            .collect(Collectors.toMap(
                vo -> vo.getAddress().toLowerCase(), // Key: 地址
                Function.identity(),             // Value: 对象本身
                (existing, replacement) -> existing // 重复键处理策略（保留先出现的）
            ));
    }

    // 获取不重复的hot_address
    public Map<String, MerchantAddressVo2> getUniqueHotAddresses() {
        return baseMapper.findDistinctHotAddresses()
            .stream()
            .collect(Collectors.toMap(
                vo -> vo.getAddress().toLowerCase(), // Key: 地址
                Function.identity(),             // Value: 对象本身
                (existing, replacement) -> existing // 重复键处理策略（保留先出现的）
            ));
    }

    @Override
    public List<MerchantAddressVo> getMerchantAddressList(Long merchantId) {
        Assert.notNull(merchantId);
        List<AssetType> assetTypeList = assetTypeService.initAssetTypeList();
        MerchantAddressBo merchantAddressBo = new MerchantAddressBo();
        merchantAddressBo.setMerchantId(merchantId);
        List<MerchantAddressVo> merchantAddressVoList = queryList(merchantAddressBo);

        return merchantAddressVoList;
    }

    /**
     * 查询商家钱包地址
     *
     * @param id 主键
     * @return 商家钱包地址
     */
    @Override
    public MerchantAddressVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商家钱包地址列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家钱包地址分页列表
     */
    @Override
    public TableDataInfo<MerchantAddressVo> queryPageList(MerchantAddressBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<MerchantAddress> lqw = buildQueryWrapper(bo);
        Page<MerchantAddressVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商家钱包地址列表
     *
     * @param bo 查询条件
     * @return 商家钱包地址列表
     */
    @Override
    public List<MerchantAddressVo> queryList(MerchantAddressBo bo) {
        LambdaQueryWrapper<MerchantAddress> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<MerchantAddress> buildQueryWrapper(MerchantAddressBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<MerchantAddress> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(MerchantAddress::getId);
        lqw.eq(bo.getMerchantId() != null, MerchantAddress::getMerchantId, bo.getMerchantId());
        lqw.eq(bo.getChain() != null, MerchantAddress::getChain, bo.getChain());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), MerchantAddress::getSymbol, bo.getSymbol());
        lqw.eq(StringUtils.isNotBlank(bo.getColdAddress()), MerchantAddress::getColdAddress, bo.getColdAddress());
        lqw.eq(bo.getCollectAmount() != null, MerchantAddress::getCollectAmount, bo.getCollectAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getHotAddress()), MerchantAddress::getHotAddress, bo.getHotAddress());
        return lqw;
    }

    /**
     * 新增商家钱包地址
     *
     * @param bo 商家钱包地址
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(MerchantAddressBo bo) {
        MerchantAddress add = MapstructUtils.convert(bo, MerchantAddress.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商家钱包地址
     *
     * @param bo 商家钱包地址
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(MerchantAddressBo bo) {
        MerchantAddress update = MapstructUtils.convert(bo, MerchantAddress.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(MerchantAddress entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商家钱包地址信息
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
