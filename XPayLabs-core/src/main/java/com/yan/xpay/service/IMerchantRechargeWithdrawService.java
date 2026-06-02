package com.yan.xpay.service;

import com.yan.xpay.domain.MerchantRechargeWithdraw;
import com.yan.xpay.domain.vo.MerchantRechargeWithdrawVo;
import com.yan.xpay.domain.bo.MerchantRechargeWithdrawBo;
import com.yan.xpay.enums.Chain;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 商家充值提现Service接口
 *
 * @author Yan
 * @date 2025-08-29
 */
public interface IMerchantRechargeWithdrawService {

    MerchantRechargeWithdraw getWithdrawByTxid(Chain chain, String symbol, String txid);

    /**
     * 審核通過
     * @param id
     * @return
     */
    boolean approve(Long id);
    /**
     * 審核不通過
     * @param id
     * @param reason
     * @return
     */
    boolean unapprove(Long id, String reason);

    /**
     * 查询商家充值提现
     *
     * @param id 主键
     * @return 商家充值提现
     */
    MerchantRechargeWithdrawVo queryById(Long id);

    /**
     * 分页查询商家充值提现列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家充值提现分页列表
     */
    TableDataInfo<MerchantRechargeWithdrawVo> queryPageList(MerchantRechargeWithdrawBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商家充值提现列表
     *
     * @param bo 查询条件
     * @return 商家充值提现列表
     */
    List<MerchantRechargeWithdrawVo> queryList(MerchantRechargeWithdrawBo bo);

    /**
     * 新增商家充值提现
     *
     * @param bo 商家充值提现
     * @return 是否新增成功
     */
    Boolean insertByBo(MerchantRechargeWithdrawBo bo);

    /**
     * 修改商家充值提现
     *
     * @param bo 商家充值提现
     * @return 是否修改成功
     */
    Boolean updateByBo(MerchantRechargeWithdrawBo bo);

    /**
     * 校验并批量删除商家充值提现信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
