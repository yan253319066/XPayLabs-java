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
import com.yan.xpay.domain.vo.BlockHeightTrackerVo;
import com.yan.xpay.domain.bo.BlockHeightTrackerBo;
import com.yan.xpay.service.IBlockHeightTrackerService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 区块监听高度追踪
 *
 * @author Yan
 * @date 2025-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/blockHeightTracker")
public class BlockHeightTrackerController extends BaseController {

    private final IBlockHeightTrackerService blockHeightTrackerService;

    /**
     * 查询区块监听高度追踪列表
     */
    @SaCheckPermission("xpay:blockHeightTracker:list")
    @GetMapping("/list")
    public TableDataInfo<BlockHeightTrackerVo> list(BlockHeightTrackerBo bo, PageQuery pageQuery) {
        return blockHeightTrackerService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出区块监听高度追踪列表
     */
    @SaCheckPermission("xpay:blockHeightTracker:export")
    @Log(title = "区块监听高度追踪", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BlockHeightTrackerBo bo, HttpServletResponse response) {
        List<BlockHeightTrackerVo> list = blockHeightTrackerService.queryList(bo);
        ExcelUtil.exportExcel(list, "区块监听高度追踪", BlockHeightTrackerVo.class, response);
    }

    /**
     * 获取区块监听高度追踪详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:blockHeightTracker:query")
    @GetMapping("/{id}")
    public R<BlockHeightTrackerVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(blockHeightTrackerService.queryById(id));
    }

    /**
     * 新增区块监听高度追踪
     */
    @SaCheckPermission("xpay:blockHeightTracker:add")
    @Log(title = "区块监听高度追踪", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BlockHeightTrackerBo bo) {
        return toAjax(blockHeightTrackerService.insertByBo(bo));
    }

    /**
     * 修改区块监听高度追踪
     */
    @SaCheckPermission("xpay:blockHeightTracker:edit")
    @Log(title = "区块监听高度追踪", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BlockHeightTrackerBo bo) {
        return toAjax(blockHeightTrackerService.updateByBo(bo));
    }

    /**
     * 删除区块监听高度追踪
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:blockHeightTracker:remove")
    @Log(title = "区块监听高度追踪", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(blockHeightTrackerService.deleteWithValidByIds(List.of(ids), true));
    }
}
