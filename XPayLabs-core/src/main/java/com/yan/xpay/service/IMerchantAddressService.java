package com.yan.xpay.service;

import com.yan.xpay.domain.vo.MerchantAddressVo;
import com.yan.xpay.domain.bo.MerchantAddressBo;
import com.yan.xpay.domain.vo.MerchantAddressVo2;
import com.yan.xpay.enums.Chain;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商家钱包地址Service接口
 *
 * @author Yan
 * @date 2025-07-28
 */
public interface IMerchantAddressService {

    /**
     * 获取商家热钱包地址
     * @param chain
     * @param symbol
     * @param address
     * @return
     */
    List<MerchantAddressVo> getHotAddressByAddress(Chain chain, String symbol, String address);
    MerchantAddressVo getColdAddressByAddress(Chain chain, String symbol, String address);
    Map<String, MerchantAddressVo2> getUniqueColdAddresses();
    Map<String, MerchantAddressVo2> getUniqueHotAddresses();

    /**
     * 获取商家地址列表，如果在assetType表有而merchantAddress表没有，则添加到merchantAddress表中
     * @param merchantId
     * @return
     */
    List<MerchantAddressVo> getMerchantAddressList(Long merchantId);

    /**
     * 查询商家钱包地址
     *
     * @param id 主键
     * @return 商家钱包地址
     */
    MerchantAddressVo queryById(Long id);

    /**
     * 分页查询商家钱包地址列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家钱包地址分页列表
     */
    TableDataInfo<MerchantAddressVo> queryPageList(MerchantAddressBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商家钱包地址列表
     *
     * @param bo 查询条件
     * @return 商家钱包地址列表
     */
    List<MerchantAddressVo> queryList(MerchantAddressBo bo);

    /**
     * 新增商家钱包地址
     *
     * @param bo 商家钱包地址
     * @return 是否新增成功
     */
    Boolean insertByBo(MerchantAddressBo bo);

    /**
     * 修改商家钱包地址
     *
     * @param bo 商家钱包地址
     * @return 是否修改成功
     */
    Boolean updateByBo(MerchantAddressBo bo);

    /**
     * 校验并批量删除商家钱包地址信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
