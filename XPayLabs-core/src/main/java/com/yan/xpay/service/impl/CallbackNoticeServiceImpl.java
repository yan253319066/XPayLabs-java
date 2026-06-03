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
import com.yan.xpay.domain.bo.CallbackNoticeBo;
import com.yan.xpay.domain.vo.CallbackNoticeVo;
import com.yan.xpay.domain.CallbackNotice;
import com.yan.xpay.mapper.CallbackNoticeMapper;
import com.yan.xpay.service.ICallbackNoticeService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 回调通知Service业务层处理
 *
 * @author Yan
 * @date 2026-06-02
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CallbackNoticeServiceImpl implements ICallbackNoticeService {

    private final CallbackNoticeMapper baseMapper;

    /**
     * 查询回调通知
     *
     * @param id 主键
     * @return 回调通知
     */
    @Override
    public CallbackNoticeVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询回调通知列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 回调通知分页列表
     */
    @Override
    public TableDataInfo<CallbackNoticeVo> queryPageList(CallbackNoticeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CallbackNotice> lqw = buildQueryWrapper(bo);
        Page<CallbackNoticeVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的回调通知列表
     *
     * @param bo 查询条件
     * @return 回调通知列表
     */
    @Override
    public List<CallbackNoticeVo> queryList(CallbackNoticeBo bo) {
        LambdaQueryWrapper<CallbackNotice> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CallbackNotice> buildQueryWrapper(CallbackNoticeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CallbackNotice> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CallbackNotice::getId);
        lqw.eq(bo.getOrderId() != null, CallbackNotice::getOrderId, bo.getOrderId());
        lqw.eq(StringUtils.isNotBlank(bo.getCallbackUrl()), CallbackNotice::getCallbackUrl, bo.getCallbackUrl());
        lqw.eq(bo.getNotifyStatus() != null, CallbackNotice::getNotifyStatus, bo.getNotifyStatus());
        lqw.eq(bo.getNotifyTime() != null, CallbackNotice::getNotifyTime, bo.getNotifyTime());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            CallbackNotice::getCreateTime ,params.get("beginCreateTime"), params.get("endCreateTime"));
        return lqw;
    }

    /**
     * 新增回调通知
     *
     * @param bo 回调通知
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CallbackNoticeBo bo) {
        CallbackNotice add = MapstructUtils.convert(bo, CallbackNotice.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改回调通知
     *
     * @param bo 回调通知
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CallbackNoticeBo bo) {
        CallbackNotice update = MapstructUtils.convert(bo, CallbackNotice.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CallbackNotice entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除回调通知信息
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
