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
import com.yan.xpay.domain.vo.MerchantCostDetailVo;
import com.yan.xpay.domain.bo.MerchantCostDetailBo;
import com.yan.xpay.service.IMerchantCostDetailService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商家费用明细
 *
 * @author Yan
 * @date 2025-08-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/merchantCostDetail")
public class MerchantCostDetailController extends BaseController {

    private final IMerchantCostDetailService merchantCostDetailService;

    /**
     * 查询商家费用明细列表
     */
    @SaCheckPermission("xpay:merchantCostDetail:list")
    @GetMapping("/list")
    public TableDataInfo<MerchantCostDetailVo> list(MerchantCostDetailBo bo, PageQuery pageQuery) {
        return merchantCostDetailService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商家费用明细列表
     */
    @SaCheckPermission("xpay:merchantCostDetail:export")
    @Log(title = "商家费用明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(MerchantCostDetailBo bo, HttpServletResponse response) {
        List<MerchantCostDetailVo> list = merchantCostDetailService.queryList(bo);
        ExcelUtil.exportExcel(list, "商家费用明细", MerchantCostDetailVo.class, response);
    }

    /**
     * 获取商家费用明细详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:merchantCostDetail:query")
    @GetMapping("/{id}")
    public R<MerchantCostDetailVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(merchantCostDetailService.queryById(id));
    }

    /**
     * 新增商家费用明细
     */
    @SaCheckPermission("xpay:merchantCostDetail:add")
    @Log(title = "商家费用明细", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody MerchantCostDetailBo bo) {
        return toAjax(merchantCostDetailService.insertByBo(bo));
    }

    /**
     * 修改商家费用明细
     */
    @SaCheckPermission("xpay:merchantCostDetail:edit")
    @Log(title = "商家费用明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody MerchantCostDetailBo bo) {
        return toAjax(merchantCostDetailService.updateByBo(bo));
    }

    /**
     * 删除商家费用明细
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:merchantCostDetail:remove")
    @Log(title = "商家费用明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(merchantCostDetailService.deleteWithValidByIds(List.of(ids), true));
    }
}
