package com.yan.xpay.service.impl;

import com.yan.xpay.domain.Merchant;
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
import com.yan.xpay.domain.bo.CollectRecordBo;
import com.yan.xpay.domain.vo.CollectRecordVo;
import com.yan.xpay.domain.CollectRecord;
import com.yan.xpay.mapper.CollectRecordMapper;
import com.yan.xpay.service.ICollectRecordService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 链上归集记录Service业务层处理
 *
 * @author Yan
 * @date 2025-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CollectRecordServiceImpl implements ICollectRecordService {

    private final CollectRecordMapper baseMapper;

    /**
     * 查询链上归集记录
     *
     * @param id 主键
     * @return 链上归集记录
     */
    @Override
    public CollectRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询链上归集记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 链上归集记录分页列表
     */
    @Override
    public TableDataInfo<CollectRecordVo> queryPageList(CollectRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CollectRecord> lqw = buildQueryWrapper(bo);
        Page<CollectRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的链上归集记录列表
     *
     * @param bo 查询条件
     * @return 链上归集记录列表
     */
    @Override
    public List<CollectRecordVo> queryList(CollectRecordBo bo) {
        LambdaQueryWrapper<CollectRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CollectRecord> buildQueryWrapper(CollectRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CollectRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CollectRecord::getId);
        lqw.eq(bo.getMerchantId() != null, CollectRecord::getMerchantId, bo.getMerchantId());
        lqw.eq(bo.getBlockNumber() != null, CollectRecord::getBlockNumber, bo.getBlockNumber());
        lqw.eq(StringUtils.isNotBlank(bo.getFromAddress()), CollectRecord::getFromAddress, bo.getFromAddress());
        lqw.eq(StringUtils.isNotBlank(bo.getToAddress()), CollectRecord::getToAddress, bo.getToAddress());
        lqw.eq(bo.getChain() != null, CollectRecord::getChain, bo.getChain());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), CollectRecord::getSymbol, bo.getSymbol());
        lqw.eq(bo.getAmount() != null, CollectRecord::getAmount, bo.getAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getTxId()), CollectRecord::getTxId, bo.getTxId());
        lqw.eq(StringUtils.isNotBlank(bo.getContractAddress()), CollectRecord::getContractAddress, bo.getContractAddress());
        lqw.eq(bo.getTxFee() != null, CollectRecord::getTxFee, bo.getTxFee());
        lqw.eq(bo.getConfirmedNum() != null, CollectRecord::getConfirmedNum, bo.getConfirmedNum());
        lqw.eq(bo.getStatus() != null, CollectRecord::getStatus, bo.getStatus());
        lqw.eq(bo.getBlockTime() != null, CollectRecord::getBlockTime, bo.getBlockTime());
        lqw.eq(bo.getCollectAmount() != null, CollectRecord::getCollectAmount, bo.getCollectAmount());
        lqw.eq(bo.getFee() != null, CollectRecord::getFee, bo.getFee());
        lqw.eq(bo.getFeeRatio() != null, CollectRecord::getFeeRatio, bo.getFeeRatio());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            CollectRecord::getCreateTime ,params.get("beginCreateTime"), params.get("endCreateTime"));
        return lqw;
    }

    /**
     * 新增链上归集记录
     *
     * @param bo 链上归集记录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CollectRecordBo bo) {
        CollectRecord add = MapstructUtils.convert(bo, CollectRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改链上归集记录
     *
     * @param bo 链上归集记录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CollectRecordBo bo) {
        CollectRecord update = MapstructUtils.convert(bo, CollectRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CollectRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除链上归集记录信息
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
