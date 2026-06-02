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
import com.yan.xpay.domain.vo.ErrorBlockVo;
import com.yan.xpay.domain.bo.ErrorBlockBo;
import com.yan.xpay.service.IErrorBlockService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 错误的区块
 *
 * @author Yan
 * @date 2025-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/errorBlock")
public class ErrorBlockController extends BaseController {

    private final IErrorBlockService errorBlockService;

    /**
     * 查询错误的区块列表
     */
    @SaCheckPermission("xpay:errorBlock:list")
    @GetMapping("/list")
    public TableDataInfo<ErrorBlockVo> list(ErrorBlockBo bo, PageQuery pageQuery) {
        return errorBlockService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出错误的区块列表
     */
    @SaCheckPermission("xpay:errorBlock:export")
    @Log(title = "错误的区块", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ErrorBlockBo bo, HttpServletResponse response) {
        List<ErrorBlockVo> list = errorBlockService.queryList(bo);
        ExcelUtil.exportExcel(list, "错误的区块", ErrorBlockVo.class, response);
    }

    /**
     * 获取错误的区块详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:errorBlock:query")
    @GetMapping("/{id}")
    public R<ErrorBlockVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(errorBlockService.queryById(id));
    }

    /**
     * 新增错误的区块
     */
    @SaCheckPermission("xpay:errorBlock:add")
    @Log(title = "错误的区块", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ErrorBlockBo bo) {
        return toAjax(errorBlockService.insertByBo(bo));
    }

    /**
     * 修改错误的区块
     */
    @SaCheckPermission("xpay:errorBlock:edit")
    @Log(title = "错误的区块", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ErrorBlockBo bo) {
        return toAjax(errorBlockService.updateByBo(bo));
    }

    /**
     * 删除错误的区块
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:errorBlock:remove")
    @Log(title = "错误的区块", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(errorBlockService.deleteWithValidByIds(List.of(ids), true));
    }
}
