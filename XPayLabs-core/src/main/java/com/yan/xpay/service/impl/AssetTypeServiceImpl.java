package com.yan.xpay.service.impl;

import org.dromara.common.core.enums.Status;
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
import com.yan.xpay.domain.bo.AssetTypeBo;
import com.yan.xpay.domain.vo.AssetTypeVo;
import com.yan.xpay.domain.AssetType;
import com.yan.xpay.mapper.AssetTypeMapper;
import com.yan.xpay.service.IAssetTypeService;

import java.util.List;
import java.util.Collection;

/**
 * 支持的币种资产类型Service业务层处理
 *
 * @author Yan
 * @date 2025-07-12
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AssetTypeServiceImpl implements IAssetTypeService {

    private final AssetTypeMapper baseMapper;

    @Override
    public List<AssetType> initAssetTypeList() {
        List<AssetType> assets = baseMapper.selectList(
            new LambdaQueryWrapper<AssetType>()
                .eq(AssetType::getEnabled, Status.ENABLED.name()));
        return assets;
    }

    /**
     * 查询支持的币种资产类型
     *
     * @param id 主键
     * @return 支持的币种资产类型
     */
    @Override
    public AssetTypeVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询支持的币种资产类型列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 支持的币种资产类型分页列表
     */
    @Override
    public TableDataInfo<AssetTypeVo> queryPageList(AssetTypeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AssetType> lqw = buildQueryWrapper(bo);
        Page<AssetTypeVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的支持的币种资产类型列表
     *
     * @param bo 查询条件
     * @return 支持的币种资产类型列表
     */
    @Override
    public List<AssetTypeVo> queryList(AssetTypeBo bo) {
        LambdaQueryWrapper<AssetType> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<AssetType> buildQueryWrapper(AssetTypeBo bo) {
//        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<AssetType> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(AssetType::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), AssetType::getSymbol, bo.getSymbol());
        lqw.eq(bo.getChain() != null, AssetType::getChain, bo.getChain());
        lqw.eq(StringUtils.isNotBlank(bo.getContractAddress()), AssetType::getContractAddress, bo.getContractAddress());
        lqw.eq(bo.getDecimals() != null, AssetType::getDecimals, bo.getDecimals());
        lqw.eq(bo.getEnabled() != null, AssetType::getEnabled, bo.getEnabled());
        lqw.eq(bo.getNetwork() != null, AssetType::getNetwork, bo.getNetwork());
        return lqw;
    }

    /**
     * 新增支持的币种资产类型
     *
     * @param bo 支持的币种资产类型
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(AssetTypeBo bo) {
        AssetType add = MapstructUtils.convert(bo, AssetType.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改支持的币种资产类型
     *
     * @param bo 支持的币种资产类型
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(AssetTypeBo bo) {
        AssetType update = MapstructUtils.convert(bo, AssetType.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(AssetType entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除支持的币种资产类型信息
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
