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
import com.yan.xpay.domain.vo.CollectRecordVo;
import com.yan.xpay.domain.bo.CollectRecordBo;
import com.yan.xpay.service.ICollectRecordService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 链上归集记录
 *
 * @author Yan
 * @date 2025-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/collectRecord")
public class CollectRecordController extends BaseController {

    private final ICollectRecordService collectRecordService;

    /**
     * 查询链上归集记录列表
     */
    @SaCheckPermission("xpay:collectRecord:list")
    @GetMapping("/list")
    public TableDataInfo<CollectRecordVo> list(CollectRecordBo bo, PageQuery pageQuery) {
        return collectRecordService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出链上归集记录列表
     */
    @SaCheckPermission("xpay:collectRecord:export")
    @Log(title = "链上归集记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CollectRecordBo bo, HttpServletResponse response) {
        List<CollectRecordVo> list = collectRecordService.queryList(bo);
        ExcelUtil.exportExcel(list, "链上归集记录", CollectRecordVo.class, response);
    }

    /**
     * 获取链上归集记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:collectRecord:query")
    @GetMapping("/{id}")
    public R<CollectRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(collectRecordService.queryById(id));
    }

    /**
     * 新增链上归集记录
     */
    @SaCheckPermission("xpay:collectRecord:add")
    @Log(title = "链上归集记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CollectRecordBo bo) {
        return toAjax(collectRecordService.insertByBo(bo));
    }

    /**
     * 修改链上归集记录
     */
    @SaCheckPermission("xpay:collectRecord:edit")
    @Log(title = "链上归集记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CollectRecordBo bo) {
        return toAjax(collectRecordService.updateByBo(bo));
    }

    /**
     * 删除链上归集记录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:collectRecord:remove")
    @Log(title = "链上归集记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(collectRecordService.deleteWithValidByIds(List.of(ids), true));
    }
}
