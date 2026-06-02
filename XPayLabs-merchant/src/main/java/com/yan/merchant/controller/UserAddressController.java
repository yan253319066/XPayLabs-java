package com.yan.merchant.controller;

import java.util.List;

import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.domain.vo.PendingCollectionVO;
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
import com.yan.xpay.domain.vo.UserAddressVo;
import com.yan.xpay.domain.bo.UserAddressBo;
import com.yan.xpay.service.IUserAddressService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户地址
 *
 * @author Yan
 * @date 2025-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/userAddress")
public class UserAddressController extends BaseController {

    private final IUserAddressService userAddressService;
    private final IMerchantService merchantService;

    /**
     * 根据商家获取待归集的token
     * @return
     */
    @GetMapping("/getPendingCollectionBalances")
    public R<List<PendingCollectionVO>> getPendingCollectionBalances(){
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        return R.ok(userAddressService.getPendingCollectionBalances(merchantVo.getId()));
    }

    /**
     * 查询用户地址列表
     */
    @SaCheckPermission("xpay:userAddress:list")
    @GetMapping("/list")
    public TableDataInfo<UserAddressVo> list(UserAddressBo bo, PageQuery pageQuery) {
        return userAddressService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户地址列表
     */
    @SaCheckPermission("xpay:userAddress:export")
    @Log(title = "用户地址", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(UserAddressBo bo, HttpServletResponse response) {
        List<UserAddressVo> list = userAddressService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户地址", UserAddressVo.class, response);
    }

    /**
     * 获取用户地址详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:userAddress:query")
    @GetMapping("/{id}")
    public R<UserAddressVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(userAddressService.queryById(id));
    }

    /**
     * 新增用户地址
     */
    @SaCheckPermission("xpay:userAddress:add")
    @Log(title = "用户地址", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody UserAddressBo bo) {
        return toAjax(userAddressService.insertByBo(bo));
    }

    /**
     * 修改用户地址
     */
    @SaCheckPermission("xpay:userAddress:edit")
    @Log(title = "用户地址", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody UserAddressBo bo) {
        return toAjax(userAddressService.updateByBo(bo));
    }

    /**
     * 删除用户地址
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:userAddress:remove")
    @Log(title = "用户地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(userAddressService.deleteWithValidByIds(List.of(ids), true));
    }
}
