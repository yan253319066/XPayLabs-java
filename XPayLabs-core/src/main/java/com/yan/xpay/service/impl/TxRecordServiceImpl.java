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
import com.yan.xpay.domain.bo.TxRecordBo;
import com.yan.xpay.domain.vo.TxRecordVo;
import com.yan.xpay.domain.TxRecord;
import com.yan.xpay.mapper.TxRecordMapper;
import com.yan.xpay.service.ITxRecordService;

import java.util.List;
import java.util.Collection;

/**
 * 链上交易记录Service业务层处理
 *
 * @author Yan
 * @date 2025-07-12
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TxRecordServiceImpl implements ITxRecordService {

    private final TxRecordMapper baseMapper;

    /**
     * 查询链上交易记录
     *
     * @param id 主键
     * @return 链上交易记录
     */
    @Override
    public TxRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询链上交易记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 链上交易记录分页列表
     */
    @Override
    public TableDataInfo<TxRecordVo> queryPageList(TxRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TxRecord> lqw = buildQueryWrapper(bo);
        Page<TxRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的链上交易记录列表
     *
     * @param bo 查询条件
     * @return 链上交易记录列表
     */
    @Override
    public List<TxRecordVo> queryList(TxRecordBo bo) {
        LambdaQueryWrapper<TxRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TxRecord> buildQueryWrapper(TxRecordBo bo) {
//        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TxRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(TxRecord::getId);
        lqw.eq(bo.getOrderId() != null, TxRecord::getOrderId, bo.getOrderId());
        lqw.eq(StringUtils.isNotBlank(bo.getFromAddress()), TxRecord::getFromAddress, bo.getFromAddress());
        lqw.eq(StringUtils.isNotBlank(bo.getToAddress()), TxRecord::getToAddress, bo.getToAddress());
        lqw.eq(bo.getAmount() != null, TxRecord::getAmount, bo.getAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getTxId()), TxRecord::getTxId, bo.getTxId());
        lqw.eq(bo.getTxType() != null, TxRecord::getTxType, bo.getTxType());
        lqw.eq(bo.getStatus() != null, TxRecord::getStatus, bo.getStatus());
//        lqw.between(params.get("beginBlockTime") != null && params.get("endBlockTime") != null,
//            TxRecord::getBlockTime ,params.get("beginBlockTime"), params.get("endBlockTime"));
        return lqw;
    }

    /**
     * 新增链上交易记录
     *
     * @param bo 链上交易记录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(TxRecordBo bo) {
        TxRecord add = MapstructUtils.convert(bo, TxRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改链上交易记录
     *
     * @param bo 链上交易记录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(TxRecordBo bo) {
        TxRecord update = MapstructUtils.convert(bo, TxRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(TxRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除链上交易记录信息
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
