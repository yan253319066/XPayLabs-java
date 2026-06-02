package com.yan.xpay.service;

import com.yan.xpay.domain.vo.MerchantAssetDetailsVo;
import com.yan.xpay.domain.bo.MerchantAssetDetailsBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 资产变动明细Service接口
 *
 * @author Yan
 * @date 2025-09-15
 */
public interface IMerchantAssetDetailsService {

    /**
     * 查询资产变动明细
     *
     * @param id 主键
     * @return 资产变动明细
     */
    MerchantAssetDetailsVo queryById(Long id);

    /**
     * 分页查询资产变动明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 资产变动明细分页列表
     */
    TableDataInfo<MerchantAssetDetailsVo> queryPageList(MerchantAssetDetailsBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的资产变动明细列表
     *
     * @param bo 查询条件
     * @return 资产变动明细列表
     */
    List<MerchantAssetDetailsVo> queryList(MerchantAssetDetailsBo bo);

    /**
     * 新增资产变动明细
     *
     * @param bo 资产变动明细
     * @return 是否新增成功
     */
    Boolean insertByBo(MerchantAssetDetailsBo bo);

    /**
     * 修改资产变动明细
     *
     * @param bo 资产变动明细
     * @return 是否修改成功
     */
    Boolean updateByBo(MerchantAssetDetailsBo bo);

    /**
     * 校验并批量删除资产变动明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
