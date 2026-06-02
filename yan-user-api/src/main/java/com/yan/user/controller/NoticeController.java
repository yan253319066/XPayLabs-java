package com.yan.user.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import com.yan.user.domain.vo.NoticeVo;
import com.yan.user.domain.bo.NoticeBo;
import com.yan.user.service.INoticeService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户通知公告
 *
 * @author Yan
 * @date 2025-10-26
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/notice")
public class NoticeController extends BaseController {

    private final INoticeService noticeService;

    /**
     * 查询用户通知公告列表
     */
    @GetMapping("/list")
    public TableDataInfo<NoticeVo> list(PageQuery pageQuery) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        NoticeBo bo = new NoticeBo();
        bo.setUserId(loginUser.getUserId());
        return noticeService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取用户通知公告详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<NoticeVo> getInfo(@NotNull(message = "ID cannot be left blank.")
                                     @PathVariable Long id) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        return R.ok(noticeService.queryById(loginUser.getUserId(), id));
    }

    /**
     * 标记消息为已读
     * @param id
     * @return
     */
    @GetMapping("/read")
    public R<Void> read(@NotNull(message = "ID cannot be left blank.") @PathVariable Long id) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        return toAjax(noticeService.read(loginUser.getUserId(), id));
    }
}
