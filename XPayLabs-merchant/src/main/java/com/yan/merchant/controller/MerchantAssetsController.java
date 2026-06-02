package com.yan.merchant.controller;

import java.util.List;

import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.service.IMerchantService;
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
import com.yan.xpay.domain.vo.MerchantAssetsVo;
import com.yan.xpay.domain.bo.MerchantAssetsBo;
import com.yan.xpay.service.IMerchantAssetsService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商家资产
 *
 * @author Yan
 * @date 2025-09-15
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/merchantAssets")
public class MerchantAssetsController extends BaseController {

    private final IMerchantAssetsService merchantAssetsService;
    private final IMerchantService merchantService;

    /**
     * 根据当前登录用户获取商家资产
     * @return
     */
    @GetMapping("/merchantAssets")
    public R<List<MerchantAssetsVo>> merchantAssets() {
        Long userId = LoginHelper.getUserId();
        MerchantVo merchantVo = merchantService.getMerchantByUserId(userId);
        if(merchantVo == null) return R.fail("当前用户未绑定商家");
        return R.ok(merchantAssetsService.getBalanceList(merchantVo.getId()));
    }

    /**
     * 查询商家资产列表
     */
    @SaCheckPermission("xpay:merchantAssets:list")
    @GetMapping("/list")
    public TableDataInfo<MerchantAssetsVo> list(MerchantAssetsBo bo, PageQuery pageQuery) {
        return merchantAssetsService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商家资产列表
     */
    @SaCheckPermission("xpay:merchantAssets:export")
    @Log(title = "商家资产", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(MerchantAssetsBo bo, HttpServletResponse response) {
        List<MerchantAssetsVo> list = merchantAssetsService.queryList(bo);
        ExcelUtil.exportExcel(list, "商家资产", MerchantAssetsVo.class, response);
    }

    /**
     * 获取商家资产详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:merchantAssets:query")
    @GetMapping("/{id}")
    public R<MerchantAssetsVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(merchantAssetsService.queryById(id));
    }

    /**
     * 新增商家资产
     */
    @SaCheckPermission("xpay:merchantAssets:add")
    @Log(title = "商家资产", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody MerchantAssetsBo bo) {
        return toAjax(merchantAssetsService.insertByBo(bo));
    }

    /**
     * 修改商家资产
     */
    @SaCheckPermission("xpay:merchantAssets:edit")
    @Log(title = "商家资产", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody MerchantAssetsBo bo) {
        return toAjax(merchantAssetsService.updateByBo(bo));
    }

    /**
     * 删除商家资产
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:merchantAssets:remove")
    @Log(title = "商家资产", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(merchantAssetsService.deleteWithValidByIds(List.of(ids), true));
    }
}
