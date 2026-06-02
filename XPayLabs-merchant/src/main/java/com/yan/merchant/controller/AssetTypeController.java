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
import com.yan.xpay.domain.vo.AssetTypeVo;
import com.yan.xpay.domain.bo.AssetTypeBo;
import com.yan.xpay.service.IAssetTypeService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 支持的币种资产类型
 *
 * @author Yan
 * @date 2025-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/assetType")
public class AssetTypeController extends BaseController {

    private final IAssetTypeService assetTypeService;

    /**
     * 查询支持的币种资产类型列表
     */
    @SaCheckPermission("xpay:assetType:list")
    @GetMapping("/list")
    public TableDataInfo<AssetTypeVo> list(AssetTypeBo bo, PageQuery pageQuery) {
        return assetTypeService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出支持的币种资产类型列表
     */
    @SaCheckPermission("xpay:assetType:export")
    @Log(title = "支持的币种资产类型", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AssetTypeBo bo, HttpServletResponse response) {
        List<AssetTypeVo> list = assetTypeService.queryList(bo);
        ExcelUtil.exportExcel(list, "支持的币种资产类型", AssetTypeVo.class, response);
    }

    /**
     * 获取支持的币种资产类型详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:assetType:query")
    @GetMapping("/{id}")
    public R<AssetTypeVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(assetTypeService.queryById(id));
    }

    /**
     * 新增支持的币种资产类型
     */
    @SaCheckPermission("xpay:assetType:add")
    @Log(title = "支持的币种资产类型", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AssetTypeBo bo) {
        return toAjax(assetTypeService.insertByBo(bo));
    }

    /**
     * 修改支持的币种资产类型
     */
    @SaCheckPermission("xpay:assetType:edit")
    @Log(title = "支持的币种资产类型", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AssetTypeBo bo) {
        return toAjax(assetTypeService.updateByBo(bo));
    }

    /**
     * 删除支持的币种资产类型
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:assetType:remove")
    @Log(title = "支持的币种资产类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(assetTypeService.deleteWithValidByIds(List.of(ids), true));
    }
}
