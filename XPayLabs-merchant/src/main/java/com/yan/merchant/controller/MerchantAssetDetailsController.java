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
import com.yan.xpay.domain.vo.MerchantAssetDetailsVo;
import com.yan.xpay.domain.bo.MerchantAssetDetailsBo;
import com.yan.xpay.service.IMerchantAssetDetailsService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 资产变动明细
 *
 * @author Yan
 * @date 2025-09-15
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/merchantAssetDetails")
public class MerchantAssetDetailsController extends BaseController {

    private final IMerchantAssetDetailsService merchantAssetDetailsService;

    /**
     * 查询资产变动明细列表
     */
    @SaCheckPermission("xpay:merchantAssetDetails:list")
    @GetMapping("/list")
    public TableDataInfo<MerchantAssetDetailsVo> list(MerchantAssetDetailsBo bo, PageQuery pageQuery) {
        return merchantAssetDetailsService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出资产变动明细列表
     */
    @SaCheckPermission("xpay:merchantAssetDetails:export")
    @Log(title = "资产变动明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(MerchantAssetDetailsBo bo, HttpServletResponse response) {
        List<MerchantAssetDetailsVo> list = merchantAssetDetailsService.queryList(bo);
        ExcelUtil.exportExcel(list, "资产变动明细", MerchantAssetDetailsVo.class, response);
    }

    /**
     * 获取资产变动明细详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:merchantAssetDetails:query")
    @GetMapping("/{id}")
    public R<MerchantAssetDetailsVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(merchantAssetDetailsService.queryById(id));
    }

    /**
     * 新增资产变动明细
     */
    @SaCheckPermission("xpay:merchantAssetDetails:add")
    @Log(title = "资产变动明细", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody MerchantAssetDetailsBo bo) {
        return toAjax(merchantAssetDetailsService.insertByBo(bo));
    }

    /**
     * 修改资产变动明细
     */
    @SaCheckPermission("xpay:merchantAssetDetails:edit")
    @Log(title = "资产变动明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody MerchantAssetDetailsBo bo) {
        return toAjax(merchantAssetDetailsService.updateByBo(bo));
    }

    /**
     * 删除资产变动明细
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:merchantAssetDetails:remove")
    @Log(title = "资产变动明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(merchantAssetDetailsService.deleteWithValidByIds(List.of(ids), true));
    }
}
