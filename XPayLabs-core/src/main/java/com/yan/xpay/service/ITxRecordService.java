package com.yan.xpay.service;

import com.yan.xpay.domain.vo.TxRecordVo;
import com.yan.xpay.domain.bo.TxRecordBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 链上交易记录Service接口
 *
 * @author Yan
 * @date 2025-07-12
 */
public interface ITxRecordService {

    /**
     * 查询链上交易记录
     *
     * @param id 主键
     * @return 链上交易记录
     */
    TxRecordVo queryById(Long id);

    /**
     * 分页查询链上交易记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 链上交易记录分页列表
     */
    TableDataInfo<TxRecordVo> queryPageList(TxRecordBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的链上交易记录列表
     *
     * @param bo 查询条件
     * @return 链上交易记录列表
     */
    List<TxRecordVo> queryList(TxRecordBo bo);

    /**
     * 新增链上交易记录
     *
     * @param bo 链上交易记录
     * @return 是否新增成功
     */
    Boolean insertByBo(TxRecordBo bo);

    /**
     * 修改链上交易记录
     *
     * @param bo 链上交易记录
     * @return 是否修改成功
     */
    Boolean updateByBo(TxRecordBo bo);

    /**
     * 校验并批量删除链上交易记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
