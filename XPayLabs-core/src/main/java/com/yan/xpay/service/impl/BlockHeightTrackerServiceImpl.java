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
import com.yan.xpay.domain.bo.BlockHeightTrackerBo;
import com.yan.xpay.domain.vo.BlockHeightTrackerVo;
import com.yan.xpay.domain.BlockHeightTracker;
import com.yan.xpay.mapper.BlockHeightTrackerMapper;
import com.yan.xpay.service.IBlockHeightTrackerService;

import java.util.List;
import java.util.Collection;

/**
 * 区块监听高度追踪Service业务层处理
 *
 * @author Yan
 * @date 2025-07-12
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BlockHeightTrackerServiceImpl implements IBlockHeightTrackerService {

    private final BlockHeightTrackerMapper baseMapper;

    /**
     * 查询区块监听高度追踪
     *
     * @param id 主键
     * @return 区块监听高度追踪
     */
    @Override
    public BlockHeightTrackerVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询区块监听高度追踪列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 区块监听高度追踪分页列表
     */
    @Override
    public TableDataInfo<BlockHeightTrackerVo> queryPageList(BlockHeightTrackerBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BlockHeightTracker> lqw = buildQueryWrapper(bo);
        Page<BlockHeightTrackerVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的区块监听高度追踪列表
     *
     * @param bo 查询条件
     * @return 区块监听高度追踪列表
     */
    @Override
    public List<BlockHeightTrackerVo> queryList(BlockHeightTrackerBo bo) {
        LambdaQueryWrapper<BlockHeightTracker> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BlockHeightTracker> buildQueryWrapper(BlockHeightTrackerBo bo) {
//        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BlockHeightTracker> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(BlockHeightTracker::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getChain()), BlockHeightTracker::getChain, bo.getChain());
        lqw.eq(bo.getLastHeight() != null, BlockHeightTracker::getLastHeight, bo.getLastHeight());
//        lqw.between(params.get("beginUpdateTime") != null && params.get("endUpdateTime") != null,
//            BlockHeightTracker::getUpdateTime ,params.get("beginUpdateTime"), params.get("endUpdateTime"));
        return lqw;
    }

    /**
     * 新增区块监听高度追踪
     *
     * @param bo 区块监听高度追踪
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BlockHeightTrackerBo bo) {
        BlockHeightTracker add = MapstructUtils.convert(bo, BlockHeightTracker.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改区块监听高度追踪
     *
     * @param bo 区块监听高度追踪
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BlockHeightTrackerBo bo) {
        BlockHeightTracker update = MapstructUtils.convert(bo, BlockHeightTracker.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BlockHeightTracker entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除区块监听高度追踪信息
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
