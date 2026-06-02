package com.yan.merchant.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.domain.bo.*;
import com.yan.xpay.domain.req.CreateCollectionOrderReq;
import com.yan.xpay.domain.vo.ApiKeyVo;
import com.yan.xpay.domain.vo.AssetTypeVo;
import com.yan.xpay.enums.GoogleStatus;
import com.yan.xpay.enums.MerchantAccountType;
import com.yan.xpay.service.GoogleAuthService;
import com.yan.xpay.utils.FeeeUtil;
import com.yan.xpay.utils.WebhookSignUtil;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.enums.Status;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.encrypt.annotation.ApiEncrypt;
import org.dromara.common.log.event.LogininforEvent;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
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
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.service.IMerchantService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商户信息
 *
 * @author Yan
 * @date 2025-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/merchant")
public class MerchantController extends BaseController {

    private final IMerchantService merchantService;
    private final ISysUserService sysUserService;
    private final SysUserMapper userMapper;
    private final GoogleAuthService googleAuthService;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Value("${xpaylabs.api-domain}")
    String apiDomain = "https://api.xpaylabs.com";

    @GetMapping("/testUserGetOrderStatus/{orderNo}")
    @RateLimiter(count = 100, time = 10, limitType = LimitType.IP)
    public String testUserGetOrderStatus(@PathVariable String orderNo) {
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        if(activeProfile.contains("dev")) {
            return HttpRequest.get("http://localhost:8077/v1/order/status/"+orderNo).contentType("application/json").header("X-API-TOKEN", merchantVo.getToken()).execute().body();
        } else {
            return HttpRequest.get(apiDomain+"/v1/order/status/"+orderNo).contentType("application/json").header("X-API-TOKEN", merchantVo.getToken()).execute().body();
        }
    }

    @PostMapping("/testUserRecharge")
    @RateLimiter(count = 100, time = 10, limitType = LimitType.IP)
    public String testUserRecharge(@RequestBody @Validated CreateCollectionOrderReq req) {
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);

        Map<String, Object> parmsMap = JSONUtil.toBean(JSONUtil.parseObj(req),  Map.class);

        long timestamp = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString().replace("-", "");
        // Create parameters map for signature generation
        Map<String, Object> signParams = new HashMap<>();
        signParams.put("data", parmsMap);
        signParams.put("nonce", nonce);
        signParams.put("timestamp", timestamp);
        String sign = WebhookSignUtil.getSignature(merchantVo.getWebhookSecret(), signParams);
        signParams.put("sign", sign);

        if(activeProfile.contains("dev")) {
            return HttpRequest.post("http://localhost:8077/v1/order/createCollection").contentType("application/json").header("X-API-TOKEN", merchantVo.getToken()).body(JSONUtil.toJsonStr(signParams)).execute().body();
        } else {
            return HttpRequest.post(apiDomain+"/v1/order/createCollection").contentType("application/json").header("X-API-TOKEN", merchantVo.getToken()).body(JSONUtil.toJsonStr(signParams)).execute().body();
        }
    }

    /**
     * 获取资产类型列表
     * @return
     */
    @GetMapping("/assetTypeList")
    public R<List<AssetTypeVo>> assetTypeList() {
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        return R.ok(merchantService.merchantAssetTypeList(merchantVo.getId(), merchantVo.getAccountType() == MerchantAccountType.TEST ? null : merchantVo.getAccountType()));
    }

    @Log(title = "修改能量租赁apikey", businessType = BusinessType.OTHER)
    @RateLimiter(count = 10, time = 60 * 60 * 24, limitType = LimitType.IP)
    @PostMapping("/setEnergyApikey")
    public R<Void> setEnergyApikey(@RequestBody @Validated EnergyApikeyBo bo) {
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        boolean b = googleAuthService.verifyCode(username, bo.getCode());
        if(!b) return R.fail("2FA code error");
        MerchantBo merchantBo = new MerchantBo();
        merchantBo.setId(merchantVo.getId());
        merchantBo.setEnergyApikey(bo.getApiKey());
        b = merchantService.updateByBo(merchantBo);
        if (b) return R.ok();
        else return R.fail();
    }

    /**
     * 获取能量租赁平台信息
     * @return
     */
    @GetMapping("/energyPlatformInfo")
    public R<JSONObject> energyPlatformInfo(){
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        if (StrUtil.isNotBlank(merchantVo.getEnergyApikey()))
            return R.ok(FeeeUtil.accountInfo(merchantVo.getEnergyApikey()));
        else return R.ok();
    }

    /**
     * 修改白名单IP
     * @param bo
     * @return
     */
    @Log(title = "修改白名单IP", businessType = BusinessType.OTHER)
    @RateLimiter(count = 10, time = 60 * 60 * 24, limitType = LimitType.IP)
    @PostMapping("/setWhitelistIp")
    public R<Void> setWhitelistIp(@RequestBody @Validated WhitelistIpBo bo) {
        if(bo.getIps().length > 10) R.fail("最多添加10个白名单IP");
        String username = LoginHelper.getUsername();
        boolean b = googleAuthService.verifyCode(username, bo.getCode());
        if(!b) return R.fail("2FA code error");
        merchantService.setWhitelistIp(username, bo.getIps());
        return R.ok();
    }
    /**
     * 修改回调地址
     * @param bo
     * @return
     */
    @Log(title = "修改回调地址", businessType = BusinessType.OTHER)
    @RateLimiter(count = 10, time = 60 * 60 * 24, limitType = LimitType.IP)
    @PostMapping("/updateCallbackUrl")
    public R<Void> updateCallbackUrl(@RequestBody @Validated CallbackUrlBo bo){
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        boolean b = googleAuthService.verifyCode(username, bo.getCode());
        if(!b) return R.fail("2FA code error");
        MerchantBo merchantBo = new MerchantBo();
        merchantBo.setId(merchantVo.getId());
        merchantBo.setCallbackUrl(bo.getCallbackUrl());
        b = merchantService.updateByBo(merchantBo);
        if (b) return R.ok();
        else return R.fail();
    }

    /**
     * 修改商家冷钱包地址
     * @param bo
     * @return
     */
    @Log(title = "修改冷钱包", businessType = BusinessType.OTHER)
    @RateLimiter(count = 30, time = 60 * 60 * 24, limitType = LimitType.IP)
    @PostMapping("/updateColdAddress")
    public R<Void> updateColdAddress(@RequestBody @Validated ColdAddressBo bo){
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        boolean b = googleAuthService.verifyCode(username, bo.getCode());
        if(!b) return R.fail("2FA code error");
        b = merchantService.updateColdAddress(merchantVo, bo.getChain(), bo.getColdAddress());
        if (b) return R.ok();
        else return R.fail();
    }

    /**
     * 商家提现
     * @param bo
     * @return
     */
    @Log(title = "商家提现", businessType = BusinessType.OTHER)
    @RateLimiter(count = 10, time = 60 * 60 * 24, limitType = LimitType.IP)
    @PostMapping("/withdrawal")
    public R<Void> withdrawal(@RequestBody @Validated WithdrawalBo bo) {
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        boolean b = googleAuthService.verifyCode(username, bo.getCode());
        if(!b) return R.fail("2FA code error");
        merchantService.withdrawal(merchantVo, bo.getChain(), bo.getSymbol(), bo.getAmount());
        return R.ok();
    }

    /**
     * 绑定2fa
     * @return
     */
    @GetMapping("/bind2fa")
    public R<Map<String, Object>> bind2fa() {
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        if(merchantVo.getGoogleStatus() == GoogleStatus.BOUND) return R.fail("Bound already");
        String secretKey;
        if(StrUtil.isNotBlank(merchantVo.getGoogleSecretkey())) secretKey = merchantVo.getGoogleSecretkey();
        else secretKey = googleAuthService.generateSecretKey(username);
        String qrCodeUrl = googleAuthService.getQRCodeUrl(username, secretKey);

        return R.ok(Map.of(
            "secretKey", secretKey,
            "qrCodeUrl", qrCodeUrl
        ));
    }

    /**
     * 验证 2FA 验证码
     * @param bo
     * @return
     */
    @RateLimiter(count = 100, time = 60 * 60 * 24, limitType = LimitType.IP)
    @PostMapping("/verify2fa")
    public R<Map<String, Object>> verify2FA(@Validated @RequestBody Verify2faBo bo) {
        String username = LoginHelper.getUsername();
        boolean b = googleAuthService.verifyCode(username, bo.getCode());
        return R.ok(Map.of(
            "verify", b
        ));
    }

    /**
     * 获取商户apikey
     * @return
     */
    @Log(title = "查看商家ApiKey", businessType = BusinessType.OTHER)
    @ApiEncrypt(response = true)
    @GetMapping("/merchantApiKey")
    public R<ApiKeyVo> merchantApiKey(@RequestParam("code") Integer code) {
        String username = LoginHelper.getUsername();
        boolean b = googleAuthService.verifyCode(username, code);
        if(!b) return R.fail("2FA code error");
        return R.ok(merchantService.merchantApiKey(username));
    }

    /**
     * 获取商户信息详细信息
     *
     */
    @GetMapping("/merchantInfo")
    public R<MerchantVo> merchantInfo() {
        String username = LoginHelper.getUsername();
        MerchantVo merchantVo = merchantService.getMerchantByName(username);
        return R.ok(merchantVo);
    }

    /**
     * 查询商户信息列表
     */
    @SaCheckPermission("xpay:merchant:list")
    @GetMapping("/list")
    public TableDataInfo<MerchantVo> list(MerchantBo bo, PageQuery pageQuery) {
        return merchantService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商户信息列表
     */
    @SaCheckPermission("xpay:merchant:export")
    @Log(title = "商户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(MerchantBo bo, HttpServletResponse response) {
        List<MerchantVo> list = merchantService.queryList(bo);
        ExcelUtil.exportExcel(list, "商户信息", MerchantVo.class, response);
    }

    /**
     * 获取商户信息详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("xpay:merchant:query")
    @GetMapping("/{id}")
    public R<MerchantVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(merchantService.queryById(id));
    }

    /**
     * 新增商户信息
     */
    @SaCheckPermission("xpay:merchant:add")
    @Log(title = "商户信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    @Transactional
    public R<Void> add(@Validated(AddGroup.class) @RequestBody MerchantBo bo) {
        String tenantId = "000000";
        SysUserBo sysUserBo = new SysUserBo();
        sysUserBo.setUserName(bo.getName());
        sysUserBo.setNickName(bo.getName());
        sysUserBo.setPassword(BCrypt.hashpw("123456"));
        sysUserBo.setUserType(UserType.SYS_USER);

        boolean exist = TenantHelper.dynamic(tenantId, () -> {
            return userMapper.exists(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, sysUserBo.getUserName()));
        });
        if (exist) {
            throw new UserException("user.register.save.error", sysUserBo.getUserName());
        }

        SysUser sysUser = MapstructUtils.convert(sysUserBo, SysUser.class);
        sysUser.setTenantId(tenantId);

        boolean regFlag = userMapper.insert(sysUser) > 0;
        if (!regFlag) {
            throw new UserException("user.register.error");
        }

        recordLogininfor(tenantId, sysUser.getUserName(), Constants.REGISTER, MessageUtils.message("user.register.success"));

        bo.setSysUserId(sysUser.getUserId());
        merchantService.registerMerchant(bo);
        return toAjax(true);
    }

    /**
     * 记录登录信息
     *
     * @param tenantId 租户ID
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     * @return
     */
    private void recordLogininfor(String tenantId, String username, String status, String message) {
        LogininforEvent logininforEvent = new LogininforEvent();
        logininforEvent.setTenantId(tenantId);
        logininforEvent.setUsername(username);
        logininforEvent.setStatus(status);
        logininforEvent.setMessage(message);
        logininforEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(logininforEvent);
    }

    /**
     * 修改商户信息
     */
    @SaCheckPermission("xpay:merchant:edit")
    @Log(title = "商户信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody MerchantBo bo) {
        return toAjax(merchantService.updateByBo(bo));
    }

    /**
     * 删除商户信息
     *
     * @param ids 主键串
     */
    @SaCheckPermission("xpay:merchant:remove")
    @Log(title = "商户信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Transactional
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            sysUserService.deleteUserById(merchantService.queryById(ids[i]).getSysUserId());
        }
        return toAjax(merchantService.deleteWithValidByIds(List.of(ids), true));
    }
}
