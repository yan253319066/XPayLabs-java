package com.yan.xpay.service;

import com.yan.xpay.domain.vo.CallbackNoticeVo;
import com.yan.xpay.domain.bo.CallbackNoticeBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 回调通知Service接口
 *
 * @author Yan
 * @date 2026-06-02
 */
public interface ICallbackNoticeService {

    /**
     * 查询回调通知
     *
     * @param id 主键
     * @return 回调通知
     */
    CallbackNoticeVo queryById(Long id);

    /**
     * 分页查询回调通知列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 回调通知分页列表
     */
    TableDataInfo<CallbackNoticeVo> queryPageList(CallbackNoticeBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的回调通知列表
     *
     * @param bo 查询条件
     * @return 回调通知列表
     */
    List<CallbackNoticeVo> queryList(CallbackNoticeBo bo);

    /**
     * 新增回调通知
     *
     * @param bo 回调通知
     * @return 是否新增成功
     */
    Boolean insertByBo(CallbackNoticeBo bo);

    /**
     * 修改回调通知
     *
     * @param bo 回调通知
     * @return 是否修改成功
     */
    Boolean updateByBo(CallbackNoticeBo bo);

    /**
     * 校验并批量删除回调通知信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
