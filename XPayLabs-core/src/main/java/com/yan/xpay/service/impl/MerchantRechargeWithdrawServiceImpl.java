package com.yan.xpay.service.impl;

import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.RechargeWithdrawStatus;
import org.dromara.common.core.exception.ServiceException;
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
import com.yan.xpay.domain.bo.MerchantRechargeWithdrawBo;
import com.yan.xpay.domain.vo.MerchantRechargeWithdrawVo;
import com.yan.xpay.domain.MerchantRechargeWithdraw;
import com.yan.xpay.mapper.MerchantRechargeWithdrawMapper;
import com.yan.xpay.service.IMerchantRechargeWithdrawService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商家充值提现Service业务层处理
 *
 * @author Yan
 * @date 2025-08-29
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantRechargeWithdrawServiceImpl implements IMerchantRechargeWithdrawService {

    private final MerchantRechargeWithdrawMapper baseMapper;

    @Override
    public MerchantRechargeWithdraw getWithdrawByTxid(Chain chain, String symbol, String txid) {
        return baseMapper.selectOne(new LambdaQueryWrapper<MerchantRechargeWithdraw>()
            .eq(MerchantRechargeWithdraw::getChain, chain)
            .eq(MerchantRechargeWithdraw::getSymbol, symbol)
            .eq(MerchantRechargeWithdraw::getTxId, txid));
    }

    @Override
    public boolean approve(Long id) {
        MerchantRechargeWithdraw mrw = baseMapper.selectById(id);
        if(mrw.getStatus() != RechargeWithdrawStatus.INIT) throw new ServiceException("状态异常");
        mrw.setStatus(RechargeWithdrawStatus.APPROVED);
        return baseMapper.updateById(mrw) > 0;
    }

    @Override
    public boolean unapprove(Long id, String reason) {
        MerchantRechargeWithdraw mrw = baseMapper.selectById(id);
        if(mrw.getStatus() != RechargeWithdrawStatus.INIT) throw new ServiceException("状态异常");
        mrw.setStatus(RechargeWithdrawStatus.REJECTED);
        mrw.setReason(reason);
        return baseMapper.updateById(mrw) > 0;
    }

    /**
     * 查询商家充值提现
     *
     * @param id 主键
     * @return 商家充值提现
     */
    @Override
    public MerchantRechargeWithdrawVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商家充值提现列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家充值提现分页列表
     */
    @Override
    public TableDataInfo<MerchantRechargeWithdrawVo> queryPageList(MerchantRechargeWithdrawBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<MerchantRechargeWithdraw> lqw = buildQueryWrapper(bo);
        Page<MerchantRechargeWithdrawVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商家充值提现列表
     *
     * @param bo 查询条件
     * @return 商家充值提现列表
     */
    @Override
    public List<MerchantRechargeWithdrawVo> queryList(MerchantRechargeWithdrawBo bo) {
        LambdaQueryWrapper<MerchantRechargeWithdraw> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<MerchantRechargeWithdraw> buildQueryWrapper(MerchantRechargeWithdrawBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<MerchantRechargeWithdraw> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(MerchantRechargeWithdraw::getId);
        lqw.eq(bo.getMerchantId() != null, MerchantRechargeWithdraw::getMerchantId, bo.getMerchantId());
        lqw.eq(bo.getType() != null, MerchantRechargeWithdraw::getType, bo.getType());
        lqw.eq(bo.getChain() != null, MerchantRechargeWithdraw::getChain, bo.getChain());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), MerchantRechargeWithdraw::getSymbol, bo.getSymbol());
        lqw.eq(StringUtils.isNotBlank(bo.getPayAddress()), MerchantRechargeWithdraw::getPayAddress, bo.getPayAddress());
        lqw.eq(StringUtils.isNotBlank(bo.getReceiveAddress()), MerchantRechargeWithdraw::getReceiveAddress, bo.getReceiveAddress());
        lqw.eq(bo.getAmount() != null, MerchantRechargeWithdraw::getAmount, bo.getAmount());
        lqw.eq(bo.getStatus() != null, MerchantRechargeWithdraw::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getReason()), MerchantRechargeWithdraw::getReason, bo.getReason());
        lqw.eq(StringUtils.isNotBlank(bo.getTxId()), MerchantRechargeWithdraw::getTxId, bo.getTxId());
        lqw.eq(bo.getTxGas() != null, MerchantRechargeWithdraw::getTxGas, bo.getTxGas());
        lqw.eq(bo.getFee() != null, MerchantRechargeWithdraw::getFee, bo.getFee());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            MerchantRechargeWithdraw::getCreateTime ,params.get("beginCreateTime"), params.get("endCreateTime"));
        return lqw;
    }

    /**
     * 新增商家充值提现
     *
     * @param bo 商家充值提现
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(MerchantRechargeWithdrawBo bo) {
        MerchantRechargeWithdraw add = MapstructUtils.convert(bo, MerchantRechargeWithdraw.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商家充值提现
     *
     * @param bo 商家充值提现
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(MerchantRechargeWithdrawBo bo) {
        MerchantRechargeWithdraw update = MapstructUtils.convert(bo, MerchantRechargeWithdraw.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(MerchantRechargeWithdraw entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商家充值提现信息
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
