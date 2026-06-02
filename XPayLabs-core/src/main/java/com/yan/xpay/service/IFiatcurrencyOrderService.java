package com.yan.xpay.service;

import com.yan.xpay.domain.FiatcurrencyOrder;
import com.yan.xpay.domain.vo.FiatcurrencyOrderVo;
import com.yan.xpay.domain.bo.FiatcurrencyOrderBo;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.enums.FiatcurrencyOrderStatus;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 法币订单Service接口
 *
 * @author Yan
 * @date 2025-10-10
 */
public interface IFiatcurrencyOrderService {

    /**
     * 查询未完成订单
     * @param coll
     * @return
     */
    List<FiatcurrencyOrderVo> queryUnfilledOrder(Collection<FiatcurrencyOrderStatus> coll);

    /**
     * 保存法币代收代付请求
     * @param bo
     * @return
     */
    boolean saveFiatCurrency(FiatcurrencyOrderBo bo);

    /**
     * 修改法币数据
     * @param order
     * @return
     */
    boolean updateFiatCurrency(FiatcurrencyOrder order);

    /**
     * 查询订单信息
     * @param orderNo
     * @param merchantId
     * @return
     */
    FiatcurrencyOrderVo queryByOrderNoAndMerchantId(String orderNo, Long merchantId);

    /**
     * 查询法币订单
     *
     * @param id 主键
     * @return 法币订单
     */
    FiatcurrencyOrderVo queryById(Long id);

    /**
     * 分页查询法币订单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 法币订单分页列表
     */
    TableDataInfo<FiatcurrencyOrderVo> queryPageList(FiatcurrencyOrderBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的法币订单列表
     *
     * @param bo 查询条件
     * @return 法币订单列表
     */
    List<FiatcurrencyOrderVo> queryList(FiatcurrencyOrderBo bo);

    /**
     * 新增法币订单
     *
     * @param bo 法币订单
     * @return 是否新增成功
     */
    Boolean insertByBo(FiatcurrencyOrderBo bo);

    /**
     * 修改法币订单
     *
     * @param bo 法币订单
     * @return 是否修改成功
     */
    Boolean updateByBo(FiatcurrencyOrderBo bo);

    /**
     * 校验并批量删除法币订单信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
