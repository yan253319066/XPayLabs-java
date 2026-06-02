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
import com.yan.xpay.domain.bo.ErrorBlockBo;
import com.yan.xpay.domain.vo.ErrorBlockVo;
import com.yan.xpay.domain.ErrorBlock;
import com.yan.xpay.mapper.ErrorBlockMapper;
import com.yan.xpay.service.IErrorBlockService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 错误的区块Service业务层处理
 *
 * @author Yan
 * @date 2025-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ErrorBlockServiceImpl implements IErrorBlockService {

    private final ErrorBlockMapper baseMapper;

    /**
     * 查询错误的区块
     *
     * @param id 主键
     * @return 错误的区块
     */
    @Override
    public ErrorBlockVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询错误的区块列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 错误的区块分页列表
     */
    @Override
    public TableDataInfo<ErrorBlockVo> queryPageList(ErrorBlockBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErrorBlock> lqw = buildQueryWrapper(bo);
        Page<ErrorBlockVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的错误的区块列表
     *
     * @param bo 查询条件
     * @return 错误的区块列表
     */
    @Override
    public List<ErrorBlockVo> queryList(ErrorBlockBo bo) {
        LambdaQueryWrapper<ErrorBlock> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ErrorBlock> buildQueryWrapper(ErrorBlockBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ErrorBlock> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(ErrorBlock::getId);
        lqw.eq(bo.getBlockNumber() != null, ErrorBlock::getBlockNumber, bo.getBlockNumber());
        return lqw;
    }

    /**
     * 新增错误的区块
     *
     * @param bo 错误的区块
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ErrorBlockBo bo) {
        ErrorBlock add = MapstructUtils.convert(bo, ErrorBlock.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改错误的区块
     *
     * @param bo 错误的区块
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ErrorBlockBo bo) {
        ErrorBlock update = MapstructUtils.convert(bo, ErrorBlock.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ErrorBlock entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除错误的区块信息
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
