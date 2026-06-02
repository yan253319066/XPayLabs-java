package com.yan.xpay.service.impl;

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
import com.yan.xpay.domain.bo.MerchantAssetDetailsBo;
import com.yan.xpay.domain.vo.MerchantAssetDetailsVo;
import com.yan.xpay.domain.MerchantAssetDetails;
import com.yan.xpay.mapper.MerchantAssetDetailsMapper;
import com.yan.xpay.service.IMerchantAssetDetailsService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 资产变动明细Service业务层处理
 *
 * @author Yan
 * @date 2025-09-15
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantAssetDetailsServiceImpl implements IMerchantAssetDetailsService {

    private final MerchantAssetDetailsMapper baseMapper;

    /**
     * 查询资产变动明细
     *
     * @param id 主键
     * @return 资产变动明细
     */
    @Override
    public MerchantAssetDetailsVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询资产变动明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 资产变动明细分页列表
     */
    @Override
    public TableDataInfo<MerchantAssetDetailsVo> queryPageList(MerchantAssetDetailsBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<MerchantAssetDetails> lqw = buildQueryWrapper(bo);
        Page<MerchantAssetDetailsVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的资产变动明细列表
     *
     * @param bo 查询条件
     * @return 资产变动明细列表
     */
    @Override
    public List<MerchantAssetDetailsVo> queryList(MerchantAssetDetailsBo bo) {
        LambdaQueryWrapper<MerchantAssetDetails> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<MerchantAssetDetails> buildQueryWrapper(MerchantAssetDetailsBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<MerchantAssetDetails> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(MerchantAssetDetails::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getTransactionNo()), MerchantAssetDetails::getTransactionNo, bo.getTransactionNo());
        lqw.eq(bo.getMerchantId() != null, MerchantAssetDetails::getMerchantId, bo.getMerchantId());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), MerchantAssetDetails::getSymbol, bo.getSymbol());
        lqw.eq(bo.getAmount() != null, MerchantAssetDetails::getAmount, bo.getAmount());
        lqw.eq(bo.getOldBalance() != null, MerchantAssetDetails::getOldBalance, bo.getOldBalance());
        lqw.eq(bo.getNewBalance() != null, MerchantAssetDetails::getNewBalance, bo.getNewBalance());
        lqw.eq(bo.getOldFrozen() != null, MerchantAssetDetails::getOldFrozen, bo.getOldFrozen());
        lqw.eq(bo.getNewFrozen() != null, MerchantAssetDetails::getNewFrozen, bo.getNewFrozen());
        lqw.eq(bo.getType() != null, MerchantAssetDetails::getType, bo.getType());
        lqw.eq(StringUtils.isNotBlank(bo.getRemark()), MerchantAssetDetails::getRemark, bo.getRemark());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            MerchantAssetDetails::getCreateTime ,params.get("beginCreateTime"), params.get("endCreateTime"));
        return lqw;
    }

    /**
     * 新增资产变动明细
     *
     * @param bo 资产变动明细
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(MerchantAssetDetailsBo bo) {
        MerchantAssetDetails add = MapstructUtils.convert(bo, MerchantAssetDetails.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改资产变动明细
     *
     * @param bo 资产变动明细
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(MerchantAssetDetailsBo bo) {
        MerchantAssetDetails update = MapstructUtils.convert(bo, MerchantAssetDetails.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(MerchantAssetDetails entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除资产变动明细信息
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
