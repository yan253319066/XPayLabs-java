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
import com.yan.xpay.domain.vo.MerchantAddressVo;
import com.yan.xpay.domain.bo.MerchantAddressBo;
import com.yan.xpay.service.IMerchantAddressService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商家钱包地址
 *
 * @author Yan
 * @date 2025-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/merchantAddress")
public class MerchantAddressController extends BaseController {

    private final IMerchantAddressService merchantAddressService;
    private final IMerchantService merchantService;

    /**
     * 获取登录商家的地址信息
     * @return
     */
    @GetMapping("/myAddressList")
    public R<List<MerchantAddressVo>> myAddressList() {
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        return R.ok(merchantAddressService.getMerchantAddressList(merchantVo.getId()));
    }

    /**
     * 查询商家钱包地址列表
     */
    @SaCheckPermission("xpay:merchantAddress:list")
    @GetMapping("/list")
    public TableDataInfo<MerchantAddressVo> list(MerchantAddressBo bo, PageQuery pageQuery) {
        return merchantAddressService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商家钱包地址列表
     */
    @SaCheckPermission("xpay:merchantAddress:export")
    @Log(title = "商家钱包地址", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(MerchantAddressBo bo, HttpServletResponse response) {
        List<MerchantAddressVo> list = merchantAddressService.queryList(bo);
        ExcelUtil.exportExcel(list, "商家钱包地址", MerchantAddressVo.class, response);
    }

    /**
     * 获取商家钱包地址详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:merchantAddress:query")
    @GetMapping("/{id}")
    public R<MerchantAddressVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(merchantAddressService.queryById(id));
    }

    /**
     * 新增商家钱包地址
     */
    @SaCheckPermission("xpay:merchantAddress:add")
    @Log(title = "商家钱包地址", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody MerchantAddressBo bo) {
        return toAjax(merchantAddressService.insertByBo(bo));
    }

    /**
     * 修改商家钱包地址
     */
    @SaCheckPermission("xpay:merchantAddress:edit")
    @Log(title = "商家钱包地址", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody MerchantAddressBo bo) {
        return toAjax(merchantAddressService.updateByBo(bo));
    }

    /**
     * 删除商家钱包地址
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:merchantAddress:remove")
    @Log(title = "商家钱包地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(merchantAddressService.deleteWithValidByIds(List.of(ids), true));
    }
}
