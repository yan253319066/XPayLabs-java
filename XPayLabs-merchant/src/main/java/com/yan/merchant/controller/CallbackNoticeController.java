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
import com.yan.xpay.domain.vo.CallbackNoticeVo;
import com.yan.xpay.domain.bo.CallbackNoticeBo;
import com.yan.xpay.service.ICallbackNoticeService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 回调通知
 *
 * @author Yan
 * @date 2026-06-02
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/callbackNotice")
public class CallbackNoticeController extends BaseController {

    private final ICallbackNoticeService callbackNoticeService;

    /**
     * 查询回调通知列表
     */
    @SaCheckPermission("xpay:callbackNotice:list")
    @GetMapping("/list")
    public TableDataInfo<CallbackNoticeVo> list(CallbackNoticeBo bo, PageQuery pageQuery) {
        return callbackNoticeService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出回调通知列表
     */
    @SaCheckPermission("xpay:callbackNotice:export")
    @Log(title = "回调通知", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CallbackNoticeBo bo, HttpServletResponse response) {
        List<CallbackNoticeVo> list = callbackNoticeService.queryList(bo);
        ExcelUtil.exportExcel(list, "回调通知", CallbackNoticeVo.class, response);
    }

    /**
     * 获取回调通知详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:callbackNotice:query")
    @GetMapping("/{id}")
    public R<CallbackNoticeVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(callbackNoticeService.queryById(id));
    }

    /**
     * 新增回调通知
     */
    @SaCheckPermission("xpay:callbackNotice:add")
    @Log(title = "回调通知", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CallbackNoticeBo bo) {
        return toAjax(callbackNoticeService.insertByBo(bo));
    }

    /**
     * 修改回调通知
     */
    @SaCheckPermission("xpay:callbackNotice:edit")
    @Log(title = "回调通知", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CallbackNoticeBo bo) {
        return toAjax(callbackNoticeService.updateByBo(bo));
    }

    /**
     * 删除回调通知
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:callbackNotice:remove")
    @Log(title = "回调通知", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(callbackNoticeService.deleteWithValidByIds(List.of(ids), true));
    }
}
