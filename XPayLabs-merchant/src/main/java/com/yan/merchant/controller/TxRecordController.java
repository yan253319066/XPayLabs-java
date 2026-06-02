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
import com.yan.xpay.domain.vo.TxRecordVo;
import com.yan.xpay.domain.bo.TxRecordBo;
import com.yan.xpay.service.ITxRecordService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 链上交易记录
 *
 * @author Yan
 * @date 2025-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/txRecord")
public class TxRecordController extends BaseController {

    private final ITxRecordService txRecordService;

    /**
     * 查询链上交易记录列表
     */
    @SaCheckPermission("xpay:txRecord:list")
    @GetMapping("/list")
    public TableDataInfo<TxRecordVo> list(TxRecordBo bo, PageQuery pageQuery) {
        return txRecordService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出链上交易记录列表
     */
    @SaCheckPermission("xpay:txRecord:export")
    @Log(title = "链上交易记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(TxRecordBo bo, HttpServletResponse response) {
        List<TxRecordVo> list = txRecordService.queryList(bo);
        ExcelUtil.exportExcel(list, "链上交易记录", TxRecordVo.class, response);
    }

    /**
     * 获取链上交易记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:txRecord:query")
    @GetMapping("/{id}")
    public R<TxRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(txRecordService.queryById(id));
    }

    /**
     * 新增链上交易记录
     */
    @SaCheckPermission("xpay:txRecord:add")
    @Log(title = "链上交易记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody TxRecordBo bo) {
        return toAjax(txRecordService.insertByBo(bo));
    }

    /**
     * 修改链上交易记录
     */
    @SaCheckPermission("xpay:txRecord:edit")
    @Log(title = "链上交易记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody TxRecordBo bo) {
        return toAjax(txRecordService.updateByBo(bo));
    }

    /**
     * 删除链上交易记录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:txRecord:remove")
    @Log(title = "链上交易记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(txRecordService.deleteWithValidByIds(List.of(ids), true));
    }
}
