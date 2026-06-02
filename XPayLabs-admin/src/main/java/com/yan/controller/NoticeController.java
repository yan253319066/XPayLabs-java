package com.yan.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yan.user.domain.bo.NoticeBo;
import com.yan.user.domain.vo.NoticeVo;
import com.yan.user.service.INoticeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户通知公告
 *
 * @author Yan
 * @date 2025-10-26
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/notice")
public class NoticeController extends BaseController {

    private final INoticeService noticeService;

    /**
     * 查询用户通知公告列表
     */
    @SaCheckPermission("app:notice:list")
    @GetMapping("/list")
    public TableDataInfo<NoticeVo> list(NoticeBo bo, PageQuery pageQuery) {
        return noticeService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户通知公告列表
     */
    @SaCheckPermission("app:notice:export")
    @Log(title = "用户通知公告", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(NoticeBo bo, HttpServletResponse response) {
        List<NoticeVo> list = noticeService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户通知公告", NoticeVo.class, response);
    }

    /**
     * 获取用户通知公告详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("app:notice:query")
    @GetMapping("/{id}")
    public R<NoticeVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(noticeService.queryById(id));
    }

    /**
     * 新增用户通知公告
     */
    @SaCheckPermission("app:notice:add")
    @Log(title = "用户通知公告", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody NoticeBo bo) {
        return toAjax(noticeService.insertByBo(bo));
    }

    /**
     * 修改用户通知公告
     */
    @SaCheckPermission("app:notice:edit")
    @Log(title = "用户通知公告", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody NoticeBo bo) {
        return toAjax(noticeService.updateByBo(bo));
    }

    /**
     * 删除用户通知公告
     *
     * @param ids 主键串
     */
    @SaCheckPermission("app:notice:remove")
    @Log(title = "用户通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(noticeService.deleteWithValidByIds(List.of(ids), true));
    }
}
