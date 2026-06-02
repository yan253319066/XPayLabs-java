package com.yan.merchant.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
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
import com.yan.xpay.domain.vo.FiatcurrencyOrderVo;
import com.yan.xpay.domain.bo.FiatcurrencyOrderBo;
import com.yan.xpay.service.IFiatcurrencyOrderService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 法币订单
 *
 * @author Yan
 * @date 2025-10-10
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/fiatcurrencyOrder")
public class FiatcurrencyOrderController extends BaseController {

    private final IFiatcurrencyOrderService fiatcurrencyOrderService;

    /**
     * 查询法币订单列表
     */
    @SaCheckPermission("xpay:fiatcurrencyOrder:list")
    @GetMapping("/list")
    public TableDataInfo<FiatcurrencyOrderVo> list(FiatcurrencyOrderBo bo, PageQuery pageQuery) {
        return fiatcurrencyOrderService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出法币订单列表
     */
    @SaCheckPermission("xpay:fiatcurrencyOrder:export")
    @Log(title = "法币订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(FiatcurrencyOrderBo bo, HttpServletResponse response) {
        List<FiatcurrencyOrderVo> list = fiatcurrencyOrderService.queryList(bo);
        ExcelUtil.exportExcel(list, "法币订单", FiatcurrencyOrderVo.class, response);
    }

    /**
     * 获取法币订单详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:fiatcurrencyOrder:query")
    @GetMapping("/{id}")
    public R<FiatcurrencyOrderVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(fiatcurrencyOrderService.queryById(id));
    }

    /**
     * 新增法币订单
     */
    @SaCheckPermission("xpay:fiatcurrencyOrder:add")
    @Log(title = "法币订单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody FiatcurrencyOrderBo bo) {
        return toAjax(fiatcurrencyOrderService.insertByBo(bo));
    }

    /**
     * 修改法币订单
     */
    @SaCheckPermission("xpay:fiatcurrencyOrder:edit")
    @Log(title = "法币订单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody FiatcurrencyOrderBo bo) {
        return toAjax(fiatcurrencyOrderService.updateByBo(bo));
    }

    /**
     * 删除法币订单
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:fiatcurrencyOrder:remove")
    @Log(title = "法币订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(fiatcurrencyOrderService.deleteWithValidByIds(List.of(ids), true));
    }
}
