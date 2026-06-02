package com.yan.merchant.controller;

import java.util.List;
import java.util.Objects;

import com.yan.merchant.help.MerchantHelp;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
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
import com.yan.xpay.domain.vo.MerchantRechargeWithdrawVo;
import com.yan.xpay.domain.bo.MerchantRechargeWithdrawBo;
import com.yan.xpay.service.IMerchantRechargeWithdrawService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商家充值提现
 *
 * @author Yan
 * @date 2025-08-29
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/merchantRechargeWithdraw")
public class MerchantRechargeWithdrawController extends BaseController {

    private final IMerchantRechargeWithdrawService merchantRechargeWithdrawService;
    private final MerchantHelp merchantHelp;

    /**
     * 提幣审核通过
     */
    @SaCheckPermission("xpay:merchantRechargeWithdraw:approve")
    @Log(title = "提幣审核通过", businessType = BusinessType.OTHER)
    @RepeatSubmit()
    @PostMapping("/approve/{id}")
    public R<Void> approve(@NotNull(message = "主键不能为空")
    @PathVariable Long id) {
        return toAjax(merchantRechargeWithdrawService.approve(id));
    }
    /**
     * 提幣审核不通过
     */
    @SaCheckPermission("xpay:merchantRechargeWithdraw:unapprove")
    @Log(title = "提幣审核不通过", businessType = BusinessType.OTHER)
    @RepeatSubmit()
    @PostMapping("/unapprove/{id}")
    public R<Void> unapprove(@NotNull(message = "主键不能为空")
    @PathVariable Long id, String reason) {
        return toAjax(merchantRechargeWithdrawService.unapprove(id, reason));
    }

    /**
     * 查询商家充值提现列表
     */
    @SaCheckPermission("xpay:merchantRechargeWithdraw:list")
    @GetMapping("/list")
    public TableDataInfo<MerchantRechargeWithdrawVo> list(MerchantRechargeWithdrawBo bo, PageQuery pageQuery) {
        if(1 != LoginHelper.getUserId())
            bo.setMerchantId(merchantHelp.getMerchantId());
        return merchantRechargeWithdrawService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商家充值提现列表
     */
    @SaCheckPermission("xpay:merchantRechargeWithdraw:export")
    @Log(title = "商家充值提现", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(MerchantRechargeWithdrawBo bo, HttpServletResponse response) {
        if(1 != LoginHelper.getUserId())
            bo.setMerchantId(merchantHelp.getMerchantId());
        List<MerchantRechargeWithdrawVo> list = merchantRechargeWithdrawService.queryList(bo);
        ExcelUtil.exportExcel(list, "商家充值提现", MerchantRechargeWithdrawVo.class, response);
    }

    /**
     * 获取商家充值提现详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:merchantRechargeWithdraw:query")
    @GetMapping("/{id}")
    public R<MerchantRechargeWithdrawVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        MerchantRechargeWithdrawVo merchantRechargeWithdrawVo = merchantRechargeWithdrawService.queryById(id);
        if(1 == LoginHelper.getUserId()) {
            return R.ok(merchantRechargeWithdrawVo);
        }
        if(Objects.equals(merchantRechargeWithdrawVo.getMerchantId(), merchantHelp.getMerchantId()))
            return R.ok(merchantRechargeWithdrawVo);
        else return R.ok();
    }

    /**
     * 新增商家充值提现
     */
    @SaCheckPermission("xpay:merchantRechargeWithdraw:add")
    @Log(title = "商家充值提现", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody MerchantRechargeWithdrawBo bo) {
//        return toAjax(merchantRechargeWithdrawService.insertByBo(bo));
        return null;
    }

    /**
     * 修改商家充值提现
     */
    @SaCheckPermission("xpay:merchantRechargeWithdraw:edit")
    @Log(title = "商家充值提现", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody MerchantRechargeWithdrawBo bo) {
//        return toAjax(merchantRechargeWithdrawService.updateByBo(bo));
        return null;
    }

    /**
     * 删除商家充值提现
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:merchantRechargeWithdraw:remove")
    @Log(title = "商家充值提现", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
//        return toAjax(merchantRechargeWithdrawService.deleteWithValidByIds(List.of(ids), true));
        return null;
    }
}
