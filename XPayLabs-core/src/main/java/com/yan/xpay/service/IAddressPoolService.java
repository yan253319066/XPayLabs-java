package com.yan.xpay.service;

import com.yan.xpay.domain.AddressPool;
import com.yan.xpay.domain.vo.AddressPoolVo;
import com.yan.xpay.domain.bo.AddressPoolBo;
import com.yan.xpay.enums.Chain;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 地址池管理Service接口
 *
 * @author Yan
 * @date 2025-07-12
 */
public interface IAddressPoolService {

    /**
     * 获取一个未使用的地址
     * @param chain
     * @return
     */
    String getUnAddress(Chain chain);

    AddressPool getUserAddress(Chain chain, String toAddress);

    AddressPool getPlatformHotAddress(Chain chain);

    /**
     * 查询地址池管理
     *
     * @param id 主键
     * @return 地址池管理
     */
    AddressPoolVo queryById(Long id);

    /**
     * 分页查询地址池管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 地址池管理分页列表
     */
    TableDataInfo<AddressPoolVo> queryPageList(AddressPoolBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的地址池管理列表
     *
     * @param bo 查询条件
     * @return 地址池管理列表
     */
    List<AddressPoolVo> queryList(AddressPoolBo bo);

    /**
     * 新增地址池管理
     *
     * @param bo 地址池管理
     * @return 是否新增成功
     */
    Boolean insertByBo(AddressPoolBo bo);

    /**
     * 修改地址池管理
     *
     * @param bo 地址池管理
     * @return 是否修改成功
     */
    Boolean updateByBo(AddressPoolBo bo);

    /**
     * 校验并批量删除地址池管理信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
