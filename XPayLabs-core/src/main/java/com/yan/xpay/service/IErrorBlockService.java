package com.yan.xpay.service;

import com.yan.xpay.domain.vo.ErrorBlockVo;
import com.yan.xpay.domain.bo.ErrorBlockBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 错误的区块Service接口
 *
 * @author Yan
 * @date 2025-07-28
 */
public interface IErrorBlockService {

    /**
     * 查询错误的区块
     *
     * @param id 主键
     * @return 错误的区块
     */
    ErrorBlockVo queryById(Long id);

    /**
     * 分页查询错误的区块列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 错误的区块分页列表
     */
    TableDataInfo<ErrorBlockVo> queryPageList(ErrorBlockBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的错误的区块列表
     *
     * @param bo 查询条件
     * @return 错误的区块列表
     */
    List<ErrorBlockVo> queryList(ErrorBlockBo bo);

    /**
     * 新增错误的区块
     *
     * @param bo 错误的区块
     * @return 是否新增成功
     */
    Boolean insertByBo(ErrorBlockBo bo);

    /**
     * 修改错误的区块
     *
     * @param bo 错误的区块
     * @return 是否修改成功
     */
    Boolean updateByBo(ErrorBlockBo bo);

    /**
     * 校验并批量删除错误的区块信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
