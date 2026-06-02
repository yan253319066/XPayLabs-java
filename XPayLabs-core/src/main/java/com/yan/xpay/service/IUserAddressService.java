package com.yan.xpay.service;

import com.yan.xpay.domain.UserAddress;
import com.yan.xpay.domain.vo.PendingCollectionVO;
import com.yan.xpay.domain.vo.UserAddressVo;
import com.yan.xpay.domain.bo.UserAddressBo;
import com.yan.xpay.enums.Chain;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户地址Service接口
 *
 * @author Yan
 * @date 2025-07-28
 */
public interface IUserAddressService {

    String getUserAddress(Chain chain, String symbol, Long merchantId, String userId);
    String getUserAddress(Chain chain, String symbol, Long merchantId);
    UserAddress getUserAddress(Chain chain, String symbol, String address);
    List<PendingCollectionVO> getPendingCollectionBalances(Long merchantId);

    /**
     * 查询用户地址
     *
     * @param id 主键
     * @return 用户地址
     */
    UserAddressVo queryById(Long id);

    /**
     * 分页查询用户地址列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户地址分页列表
     */
    TableDataInfo<UserAddressVo> queryPageList(UserAddressBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户地址列表
     *
     * @param bo 查询条件
     * @return 用户地址列表
     */
    List<UserAddressVo> queryList(UserAddressBo bo);

    /**
     * 新增用户地址
     *
     * @param bo 用户地址
     * @return 是否新增成功
     */
    Boolean insertByBo(UserAddressBo bo);

    /**
     * 修改用户地址
     *
     * @param bo 用户地址
     * @return 是否修改成功
     */
    Boolean updateByBo(UserAddressBo bo);
    Boolean updateById(UserAddress userAddress);

    /**
     * 校验并批量删除用户地址信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
