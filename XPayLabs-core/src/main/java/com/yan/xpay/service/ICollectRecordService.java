package com.yan.xpay.service;

import com.yan.xpay.domain.vo.CollectRecordVo;
import com.yan.xpay.domain.bo.CollectRecordBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 链上归集记录Service接口
 *
 * @author Yan
 * @date 2025-07-28
 */
public interface ICollectRecordService {

    /**
     * 查询链上归集记录
     *
     * @param id 主键
     * @return 链上归集记录
     */
    CollectRecordVo queryById(Long id);

    /**
     * 分页查询链上归集记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 链上归集记录分页列表
     */
    TableDataInfo<CollectRecordVo> queryPageList(CollectRecordBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的链上归集记录列表
     *
     * @param bo 查询条件
     * @return 链上归集记录列表
     */
    List<CollectRecordVo> queryList(CollectRecordBo bo);

    /**
     * 新增链上归集记录
     *
     * @param bo 链上归集记录
     * @return 是否新增成功
     */
    Boolean insertByBo(CollectRecordBo bo);

    /**
     * 修改链上归集记录
     *
     * @param bo 链上归集记录
     * @return 是否修改成功
     */
    Boolean updateByBo(CollectRecordBo bo);

    /**
     * 校验并批量删除链上归集记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
