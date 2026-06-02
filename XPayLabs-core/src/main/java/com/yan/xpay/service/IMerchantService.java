package com.yan.xpay.service;

import com.yan.xpay.domain.vo.ApiKeyVo;
import com.yan.xpay.domain.vo.AssetTypeVo;
import com.yan.xpay.domain.vo.MerchantAddressVo;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.domain.bo.MerchantBo;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.MerchantAccountType;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 商户信息Service接口
 *
 * @author Yan
 * @date 2025-07-12
 */
public interface IMerchantService {

    /**
     * 获取商户支持的币种资产类型列表
     *
     * @param merchantId
     * @param accountType
     * @return
     */
    List<AssetTypeVo> merchantAssetTypeList(Long merchantId, MerchantAccountType accountType);

    void withdrawal(MerchantVo merchantVo, Chain chain, String symbol, BigDecimal amount);

    MerchantVo getMerchantByUserId(Long userId);
    MerchantVo getMerchantByName(String name);
    ApiKeyVo merchantApiKey(String name);
    Boolean updateColdAddress(MerchantVo merchantVo, Chain chain, String coldAddress);
    Boolean setWhitelistIp(String username, String[] ips);

    List<MerchantAddressVo> getV3MerchantsWithAddress();

    /**
     *
     * @return
     */
    ApiKeyVo registerMerchant(MerchantBo merchantBo);

    /**
     * 根据token获取商家信息
     * @param token
     * @return
     */
    MerchantVo getByToken(String token);

    /**
     * 查询商户信息
     *
     * @param id 主键
     * @return 商户信息
     */
    MerchantVo queryById(Long id);

    /**
     * 分页查询商户信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商户信息分页列表
     */
    TableDataInfo<MerchantVo> queryPageList(MerchantBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商户信息列表
     *
     * @param bo 查询条件
     * @return 商户信息列表
     */
    List<MerchantVo> queryList(MerchantBo bo);

    /**
     * 新增商户信息
     *
     * @param bo 商户信息
     * @return 是否新增成功
     */
    Boolean insertByBo(MerchantBo bo);

    /**
     * 修改商户信息
     *
     * @param bo 商户信息
     * @return 是否修改成功
     */
    Boolean updateByBo(MerchantBo bo);

    /**
     * 校验并批量删除商户信息信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
