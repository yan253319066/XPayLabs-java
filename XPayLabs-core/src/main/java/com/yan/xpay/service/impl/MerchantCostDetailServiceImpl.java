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
import com.yan.xpay.domain.bo.MerchantCostDetailBo;
import com.yan.xpay.domain.vo.MerchantCostDetailVo;
import com.yan.xpay.domain.MerchantCostDetail;
import com.yan.xpay.mapper.MerchantCostDetailMapper;
import com.yan.xpay.service.IMerchantCostDetailService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商家费用明细Service业务层处理
 *
 * @author Yan
 * @date 2025-08-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantCostDetailServiceImpl implements IMerchantCostDetailService {

    private final MerchantCostDetailMapper baseMapper;

    /**
     * 查询商家费用明细
     *
     * @param id 主键
     * @return 商家费用明细
     */
    @Override
    public MerchantCostDetailVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商家费用明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家费用明细分页列表
     */
    @Override
    public TableDataInfo<MerchantCostDetailVo> queryPageList(MerchantCostDetailBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<MerchantCostDetail> lqw = buildQueryWrapper(bo);
        Page<MerchantCostDetailVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商家费用明细列表
     *
     * @param bo 查询条件
     * @return 商家费用明细列表
     */
    @Override
    public List<MerchantCostDetailVo> queryList(MerchantCostDetailBo bo) {
        LambdaQueryWrapper<MerchantCostDetail> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<MerchantCostDetail> buildQueryWrapper(MerchantCostDetailBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<MerchantCostDetail> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(MerchantCostDetail::getId);
        lqw.eq(bo.getMerchantId() != null, MerchantCostDetail::getMerchantId, bo.getMerchantId());
        lqw.eq(bo.getCostType() != null, MerchantCostDetail::getCostType, bo.getCostType());
        lqw.eq(bo.getChain() != null, MerchantCostDetail::getChain, bo.getChain());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), MerchantCostDetail::getSymbol, bo.getSymbol());
        lqw.eq(bo.getAmount() != null, MerchantCostDetail::getAmount, bo.getAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessId()), MerchantCostDetail::getBusinessId, bo.getBusinessId());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            MerchantCostDetail::getCreateTime ,params.get("beginCreateTime"), params.get("endCreateTime"));
        return lqw;
    }

    /**
     * 新增商家费用明细
     *
     * @param bo 商家费用明细
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(MerchantCostDetailBo bo) {
        MerchantCostDetail add = MapstructUtils.convert(bo, MerchantCostDetail.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商家费用明细
     *
     * @param bo 商家费用明细
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(MerchantCostDetailBo bo) {
        MerchantCostDetail update = MapstructUtils.convert(bo, MerchantCostDetail.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(MerchantCostDetail entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商家费用明细信息
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
