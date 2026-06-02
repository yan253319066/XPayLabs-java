package com.yan.xpay.service;

import com.yan.xpay.domain.MerchantAssets;
import com.yan.xpay.domain.SimpleTransfer;
import com.yan.xpay.domain.vo.MerchantAssetsVo;
import com.yan.xpay.domain.bo.MerchantAssetsBo;
import com.yan.xpay.enums.AssetOperType;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 商家资产Service接口
 *
 * @author Yan
 * @date 2025-09-15
 */
public interface IMerchantAssetsService {

    MerchantAssets getBalance(Long merchantId, String symbol);
    MerchantAssetsVo getVoBalance(Long merchantId, String symbol);
    List<MerchantAssetsVo> getBalanceList(Long merchantId);
    Boolean transfer(SimpleTransfer transfer);

    /**
     * 查询商家资产
     *
     * @param id 主键
     * @return 商家资产
     */
    MerchantAssetsVo queryById(Long id);

    /**
     * 分页查询商家资产列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家资产分页列表
     */
    TableDataInfo<MerchantAssetsVo> queryPageList(MerchantAssetsBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商家资产列表
     *
     * @param bo 查询条件
     * @return 商家资产列表
     */
    List<MerchantAssetsVo> queryList(MerchantAssetsBo bo);

    /**
     * 新增商家资产
     *
     * @param bo 商家资产
     * @return 是否新增成功
     */
    Boolean insertByBo(MerchantAssetsBo bo);

    /**
     * 修改商家资产
     *
     * @param bo 商家资产
     * @return 是否修改成功
     */
    Boolean updateByBo(MerchantAssetsBo bo);

    /**
     * 校验并批量删除商家资产信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
