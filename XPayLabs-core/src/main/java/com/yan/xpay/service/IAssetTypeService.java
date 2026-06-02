package com.yan.xpay.service;

import com.yan.xpay.domain.AssetType;
import com.yan.xpay.domain.vo.AssetTypeVo;
import com.yan.xpay.domain.bo.AssetTypeBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 支持的币种资产类型Service接口
 *
 * @author Yan
 * @date 2025-07-12
 */
public interface IAssetTypeService {

    List<AssetType> initAssetTypeList();

    /**
     * 查询支持的币种资产类型
     *
     * @param id 主键
     * @return 支持的币种资产类型
     */
    AssetTypeVo queryById(Long id);

    /**
     * 分页查询支持的币种资产类型列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 支持的币种资产类型分页列表
     */
    TableDataInfo<AssetTypeVo> queryPageList(AssetTypeBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的支持的币种资产类型列表
     *
     * @param bo 查询条件
     * @return 支持的币种资产类型列表
     */
    List<AssetTypeVo> queryList(AssetTypeBo bo);

    /**
     * 新增支持的币种资产类型
     *
     * @param bo 支持的币种资产类型
     * @return 是否新增成功
     */
    Boolean insertByBo(AssetTypeBo bo);

    /**
     * 修改支持的币种资产类型
     *
     * @param bo 支持的币种资产类型
     * @return 是否修改成功
     */
    Boolean updateByBo(AssetTypeBo bo);

    /**
     * 校验并批量删除支持的币种资产类型信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
