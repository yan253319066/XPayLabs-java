package com.yan.xpay.service;

import com.yan.xpay.domain.vo.BlockHeightTrackerVo;
import com.yan.xpay.domain.bo.BlockHeightTrackerBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 区块监听高度追踪Service接口
 *
 * @author Yan
 * @date 2025-07-12
 */
public interface IBlockHeightTrackerService {

    /**
     * 查询区块监听高度追踪
     *
     * @param id 主键
     * @return 区块监听高度追踪
     */
    BlockHeightTrackerVo queryById(Long id);

    /**
     * 分页查询区块监听高度追踪列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 区块监听高度追踪分页列表
     */
    TableDataInfo<BlockHeightTrackerVo> queryPageList(BlockHeightTrackerBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的区块监听高度追踪列表
     *
     * @param bo 查询条件
     * @return 区块监听高度追踪列表
     */
    List<BlockHeightTrackerVo> queryList(BlockHeightTrackerBo bo);

    /**
     * 新增区块监听高度追踪
     *
     * @param bo 区块监听高度追踪
     * @return 是否新增成功
     */
    Boolean insertByBo(BlockHeightTrackerBo bo);

    /**
     * 修改区块监听高度追踪
     *
     * @param bo 区块监听高度追踪
     * @return 是否修改成功
     */
    Boolean updateByBo(BlockHeightTrackerBo bo);

    /**
     * 校验并批量删除区块监听高度追踪信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
