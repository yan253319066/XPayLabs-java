package com.yan.controller;

import java.util.List;

import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.enums.UserType;
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
import com.yan.user.domain.vo.UserVo;
import com.yan.user.domain.bo.UserBo;
import com.yan.user.service.IUserService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 客户端用户信息
 *
 * @author Yan
 * @date 2025-06-09
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/user")
public class UserController extends BaseController {

    private final IUserService userService;

    /**
     * 查询用户信息列表
     */
    @SaCheckPermission("app:user:list")
    @GetMapping("/list")
    public TableDataInfo<UserVo> list(UserBo bo, PageQuery pageQuery) {
        return userService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户信息列表
     */
    @SaCheckPermission("app:user:export")
    @Log(title = "用户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(UserBo bo, HttpServletResponse response) {
        List<UserVo> list = userService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户信息", UserVo.class, response);
    }

    /**
     * 获取用户信息详细信息
     *
     * @param userId 主键
     */
    @SaCheckPermission("app:user:query")
    @GetMapping("/{userId}")
    public R<UserVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long userId) {
        return R.ok(userService.queryById(userId));
    }

    /**
     * 新增用户信息
     */
    @SaCheckPermission("app:user:add")
    @Log(title = "用户信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody UserBo bo) {
        bo.setUserType(UserType.APP_USER);
        bo.setNickName(RandomUtil.randomString(8));
        return toAjax(userService.insertByBo(bo));
    }

    /**
     * 修改用户信息
     */
    @SaCheckPermission("app:user:edit")
    @Log(title = "用户信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody UserBo bo) {
        return toAjax(userService.updateByBo(bo));
    }

    /**
     * 删除用户信息
     *
     * @param userIds 主键串
     */
    @SaCheckPermission("app:user:remove")
    @Log(title = "用户信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] userIds) {
        return toAjax(userService.deleteWithValidByIds(List.of(userIds), true));
    }
}
