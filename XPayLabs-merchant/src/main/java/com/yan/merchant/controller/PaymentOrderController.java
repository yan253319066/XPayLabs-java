package com.yan.merchant.controller;

import java.util.List;

import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.service.IMerchantService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import com.yan.xpay.domain.vo.PaymentOrderVo;
import com.yan.xpay.domain.bo.PaymentOrderBo;
import com.yan.xpay.service.IPaymentOrderService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 支付订单
 *
 * @author Yan
 * @date 2025-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/paymentOrder")
public class PaymentOrderController extends BaseController {

    private final IPaymentOrderService paymentOrderService;
    private final IMerchantService merchantService;

    /**
     * 查询支付订单列表
     */
    @SaCheckPermission("xpay:paymentOrder:list")
    @GetMapping("/list")
    public TableDataInfo<PaymentOrderVo> list(PaymentOrderBo bo, PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        if(1 != userId) {
            MerchantVo merchantVo = merchantService.getMerchantByUserId(userId);
            if(merchantVo == null) return new TableDataInfo<>();
            bo.setMerchantId(merchantVo.getId());
        }
        return paymentOrderService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出支付订单列表
     */
    @SaCheckPermission("xpay:paymentOrder:export")
    @Log(title = "支付订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PaymentOrderBo bo, HttpServletResponse response) {
        List<PaymentOrderVo> list = paymentOrderService.queryList(bo);
        ExcelUtil.exportExcel(list, "支付订单", PaymentOrderVo.class, response);
    }

    /**
     * 获取支付订单详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:paymentOrder:query")
    @GetMapping("/{id}")
    public R<PaymentOrderVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(paymentOrderService.queryById(id));
    }

    /**
     * 新增支付订单
     */
//    @SaCheckPermission("xpay:paymentOrder:add")
//    @Log(title = "支付订单", businessType = BusinessType.INSERT)
//    @RepeatSubmit()
//    @PostMapping()
//    public R<Void> add(@Validated(AddGroup.class) @RequestBody PaymentOrderBo bo) {
//        return toAjax(paymentOrderService.insertByBo(bo));
//    }

    /**
     * 修改支付订单
     */
//    @SaCheckPermission("xpay:paymentOrder:edit")
//    @Log(title = "支付订单", businessType = BusinessType.UPDATE)
//    @RepeatSubmit()
//    @PutMapping()
//    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PaymentOrderBo bo) {
//        return toAjax(paymentOrderService.updateByBo(bo));
//    }

    /**
     * 删除支付订单
     *
     * @param ids 主键串
     */
//    @SaCheckPermission("xpay:paymentOrder:remove")
//    @Log(title = "支付订单", businessType = BusinessType.DELETE)
//    @DeleteMapping("/{ids}")
//    public R<Void> remove(@NotEmpty(message = "主键不能为空")
//                          @PathVariable Long[] ids) {
//        return toAjax(paymentOrderService.deleteWithValidByIds(List.of(ids), true));
//    }
}
