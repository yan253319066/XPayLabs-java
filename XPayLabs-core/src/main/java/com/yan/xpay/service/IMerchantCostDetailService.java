package com.yan.xpay.service;

import com.yan.xpay.domain.vo.MerchantCostDetailVo;
import com.yan.xpay.domain.bo.MerchantCostDetailBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商家费用明细Service接口
 *
 * @author Yan
 * @date 2025-08-28
 */
public interface IMerchantCostDetailService {

    /**
     * 查询商家费用明细
     *
     * @param id 主键
     * @return 商家费用明细
     */
    MerchantCostDetailVo queryById(Long id);

    /**
     * 分页查询商家费用明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家费用明细分页列表
     */
    TableDataInfo<MerchantCostDetailVo> queryPageList(MerchantCostDetailBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商家费用明细列表
     *
     * @param bo 查询条件
     * @return 商家费用明细列表
     */
    List<MerchantCostDetailVo> queryList(MerchantCostDetailBo bo);

    /**
     * 新增商家费用明细
     *
     * @param bo 商家费用明细
     * @return 是否新增成功
     */
    Boolean insertByBo(MerchantCostDetailBo bo);

    /**
     * 修改商家费用明细
     *
     * @param bo 商家费用明细
     * @return 是否修改成功
     */
    Boolean updateByBo(MerchantCostDetailBo bo);

    /**
     * 校验并批量删除商家费用明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
