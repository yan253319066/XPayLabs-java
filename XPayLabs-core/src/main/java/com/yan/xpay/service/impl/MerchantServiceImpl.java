package com.yan.xpay.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.*;
import com.yan.xpay.domain.bo.UserAddressBo;
import com.yan.xpay.domain.vo.ApiKeyVo;
import com.yan.xpay.domain.vo.AssetTypeVo;
import com.yan.xpay.domain.vo.MerchantAddressVo;
import com.yan.xpay.enums.*;
import com.yan.xpay.mapper.MerchantAddressMapper;
import com.yan.xpay.mapper.MerchantAssetTypeMapper;
import com.yan.xpay.mapper.MerchantRechargeWithdrawMapper;
import com.yan.xpay.service.IAddressPoolService;
import com.yan.xpay.service.IMerchantAssetsService;
import com.yan.xpay.service.IUserAddressService;
import com.yan.xpay.utils.FeeUtils;
import com.yan.xpay.utils.IpWhitelistUtil;
import com.yan.xpay.utils.WebhookSignUtil;
import org.dromara.common.core.enums.Status;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.yan.xpay.domain.bo.MerchantBo;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.service.IMerchantService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Collection;
import java.util.Map;

/**
 * 商户信息Service业务层处理
 *
 * @author Yan
 * @date 2025-07-12
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantServiceImpl implements IMerchantService {

    private final MerchantMapper baseMapper;
    private final MerchantAddressMapper merchantAddressMapper;
    private final IAddressPoolService addressPoolService;
    private final AssetTypeCache assetTypeCache;
    private final MerchantRechargeWithdrawMapper merchantRechargeWithdrawMapper;
    private final IMerchantAssetsService merchantAssetsService;
    private final IUserAddressService userAddressService;
    private final MerchantAssetTypeMapper merchantAssetTypeMapper;

    @Override
    public List<AssetTypeVo> merchantAssetTypeList(Long merchantId, MerchantAccountType accountType) {
        Assert.notNull(merchantId);
        return merchantAssetTypeMapper.merchantAssetTypeList(merchantId, accountType == null ? null : accountType.name());
    }

    @Transactional
    @Override
    public void withdrawal(MerchantVo merchant, Chain chain, String symbol, BigDecimal amount) {
        Assert.notNull(merchant);
        Assert.notNull(chain);
        Assert.notBlank(symbol);
        Assert.notNull(amount);

        AssetType assetType = assetTypeCache.getBySymbol(chain,  symbol);
        if(assetType == null) throw new ServiceException("Unsupported asset type");

        if(merchant.getAccountType() == MerchantAccountType.MAIN) {
            if(assetType.getNetwork() == BlockchainNetwork.TEST) {
                log.warn("正式账户不支持测试网络提现 merchant name {}", merchant.getName());
                throw new ServiceException("Production accounts do not support test networks.");
            }
        }else {
            if(assetType.getNetwork() == BlockchainNetwork.MAIN) {
                log.warn("测试账户不支持正式网络提现 merchant name {}", merchant.getName());
                throw new ServiceException("Test accounts do not support main networks.");
            }
        }

        MerchantAddress merchantAddress = merchantAddressMapper.selectOne(new LambdaQueryWrapper<MerchantAddress>().eq(MerchantAddress::getChain, chain).eq(MerchantAddress::getSymbol, symbol).eq(
            MerchantAddress::getMerchantId, merchant.getId()));
        if(merchantAddress == null || StrUtil.isBlank(merchantAddress.getColdAddress()) || StrUtil.isBlank(merchantAddress.getHotAddress()))
            throw new ServiceException("Parameter error");

        IpWhitelistUtil.ipIsAllowed(merchant.getEnableWhitelistIp(), merchant.getWhiteListIp());

        MerchantRechargeWithdraw merchantRechargeWithdraw = new MerchantRechargeWithdraw();
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        String transactionNo = snowflake.nextIdStr();
        merchantRechargeWithdraw.setTransactionNo(transactionNo);
        merchantRechargeWithdraw.setMerchantId(merchant.getId());
        merchantRechargeWithdraw.setType(RechargeWithdraw.WITHDRAW);
        merchantRechargeWithdraw.setChain(chain);
        merchantRechargeWithdraw.setSymbol(symbol);
        if("USDT".equals(symbol) && amount.compareTo(new BigDecimal("200")) <= 0)
            merchantRechargeWithdraw.setStatus(RechargeWithdrawStatus.APPROVED);
        else merchantRechargeWithdraw.setStatus(RechargeWithdrawStatus.INIT);

        if(StrUtil.isBlank(merchantAddress.getColdAddress())) throw new ServiceException("请先设置冷钱包地址");
        merchantRechargeWithdraw.setReceiveAddress(merchantAddress.getColdAddress());
        merchantRechargeWithdraw.setAmount(amount);

        BigDecimal fee = BigDecimal.ZERO;

        if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3){
            boolean isNativeToken = assetTypeCache.isNativeToken(chain, symbol);
            fee = FeeUtils.getPlatformFee(isNativeToken, chain, symbol, amount, merchant.getFeeRatio(), merchantRechargeWithdraw.getReceiveAddress());
            MerchantAssets merchantAssets =  merchantAssetsService.getBalance(merchant.getId(), symbol);
            if(merchantAssets.getBalance().compareTo(amount.add(fee)) < 0) {
                log.error("余额不足 merchant symbol {} balance {} total {} fee {} amount {}",symbol, merchantAssets.getBalance(), fee.add(amount), fee, amount);
                throw new ServiceException("余额不足");
            }

            SimpleTransfer simpleTransfer = new SimpleTransfer();
            simpleTransfer.setAmount(amount);
            simpleTransfer.setTransactionNo(transactionNo);
            simpleTransfer.setType(AssetOperType.WITHDRAW_REQUEST);
            simpleTransfer.setRate(BigDecimal.ZERO);
            simpleTransfer.setMerchantId(merchant.getId());
            simpleTransfer.setRemark("商家提现申请");
            simpleTransfer.setFeeRate(merchant.getFeeRatio());
            simpleTransfer.setFee(fee);
            simpleTransfer.setFeeSymbol(symbol);
            simpleTransfer.setSymbol(symbol);
            simpleTransfer.setChain(chain);
            simpleTransfer.setNetwork(assetType.getNetwork().name());
            merchantAssetsService.transfer(simpleTransfer);
        }else {
            fee = FeeUtils.getPlatformFee(amount, merchant.getFeeRatio());
        }
        if(fee.compareTo(BigDecimal.ZERO) > 0)
            merchantRechargeWithdraw.setRate(merchant.getFeeRatio());
        else merchantRechargeWithdraw.setRate(BigDecimal.ZERO);
        merchantRechargeWithdraw.setFee(fee);
        merchantRechargeWithdraw.setPayAddress("");
        merchantRechargeWithdrawMapper.insert(merchantRechargeWithdraw);
    }

    @Override
    public MerchantVo getMerchantByUserId(Long userId) {
        Assert.notNull(userId);
        return baseMapper.selectVoOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getSysUserId, userId));
    }

    @Override
    public MerchantVo getMerchantByName(String name) {
        Assert.notBlank(name);
        return baseMapper.selectVoOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getName, name));
    }

    @Override
    public ApiKeyVo merchantApiKey(String name) {
        Assert.notBlank(name);
        Merchant merchant = baseMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getName, name));
        ApiKeyVo apiKeyVo = new ApiKeyVo();
        apiKeyVo.setApiKey(merchant.getToken());
        apiKeyVo.setWebhookSecret(merchant.getWebhookSecret());
        return apiKeyVo;
    }

    @Override
    public Boolean updateColdAddress(MerchantVo merchantVo, Chain chain, String coldAddress) {
        List<MerchantAddress> merchantAddressList = merchantAddressMapper.selectList(new LambdaQueryWrapper<MerchantAddress>().eq(MerchantAddress::getMerchantId, merchantVo.getId()).eq(MerchantAddress::getChain, chain));
        if(!CollUtil.isEmpty(merchantAddressList)) {
            merchantAddressList.forEach(merchantAddress->{
                merchantAddress.setColdAddress(coldAddress);
            });
            return merchantAddressMapper.updateBatchById(merchantAddressList);
        }
        return false;
    }

    @Override
    public Boolean setWhitelistIp(String username, String[] ips) {
        Assert.notBlank(username);
        Merchant merchant = baseMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getName, username));
        // 标准化为\n分隔的字符串
        String normalized = String.join(",", ips);
        merchant.setWhiteListIp(normalized);
        baseMapper.updateById(merchant);
        return true;
    }

    @Override
    public List<MerchantAddressVo> getV3MerchantsWithAddress() {
        // 1. 查询符合条件的商户
        List<Merchant> merchants = baseMapper.selectList(
            Wrappers.<Merchant>lambdaQuery()
                .eq(Merchant::getMerchantSysVersion, MerchantSysVersion.V3)
        );

        // 2. 提取商户ID集合
        List<Long> merchantIds = merchants.stream()
            .map(Merchant::getId)
            .toList();

        List<MerchantAddressVo> merchantAddressVos = merchantAddressMapper.selectVoList(new LambdaQueryWrapper<MerchantAddress>().in(MerchantAddress::getMerchantId, merchantIds));

        return merchantAddressVos;
    }

    @Transactional
    @Override
    public ApiKeyVo registerMerchant(MerchantBo bo) {
        String token = "x"+IdUtil.fastSimpleUUID();
        if(baseMapper.exists(new LambdaQueryWrapper<Merchant>().eq(Merchant::getName, bo.getName()))) throw new ServiceException("Name is already taken.");

        bo.setToken(token);
        bo.setWebhookSecret(WebhookSignUtil.generateHmacSha256Key());
        if(bo.getMerchantSysVersion() == MerchantSysVersion.V3) bo.setIntoType(IntoType.PLATFORM);
        else throw new ServiceException("暂时只能注册V3版本商家");//要注册V2版本去掉这一行就可以了。
        Merchant add = MapstructUtils.convert(bo, Merchant.class);
        baseMapper.insert(add);
        ApiKeyVo apiKeyVo = new ApiKeyVo();
        apiKeyVo.setApiKey(bo.getToken());
        apiKeyVo.setWebhookSecret(bo.getWebhookSecret());

        assetTypeCache.getChains().forEach(chain -> {
            // 获取一个未使用的地址
            String addr = addressPoolService.getUnAddress(chain);
            if (StrUtil.isBlank(addr)) {
                log.error("地址池没有地址 chain {} used {} type {}", chain, AddressStatus.UNUSED, AddressType.GENERAL);
                throw new ServiceException("No available address is available at the moment. Please try again later.");
            }

            assetTypeCache.getAssetsByChain(chain).forEach(assetType -> {
                //添加商户地址
                MerchantAddress merchantAddress = new MerchantAddress();
                merchantAddress.setMerchantId(add.getId());
                merchantAddress.setHotAddress(addr);
                merchantAddress.setChain(assetType.getChain());
                merchantAddress.setSymbol(assetType.getSymbol());
                merchantAddress.setCollectAmount(new BigDecimal(50));
                merchantAddressMapper.insert(merchantAddress);

                //添加商户资产类型
                MerchantAssetType merchantAssetType = new MerchantAssetType();
                merchantAssetType.setAssetTypeId(assetType.getId());
                merchantAssetType.setMerchantId(add.getId());
                merchantAssetType.setStatus(Status.ENABLED);
                merchantAssetTypeMapper.insert(merchantAssetType);

                //为了能归集才加入这个表的AddressStatus不能修改成UNUSED
                UserAddressBo userAddressBo = new UserAddressBo();
                userAddressBo.setMerchantId(add.getId());
                userAddressBo.setAddress(addr);
                userAddressBo.setStatus(AddressStatus.USED);
                userAddressBo.setSymbol(assetType.getSymbol());
                userAddressBo.setChain(assetType.getChain());
                userAddressBo.setAmount(BigDecimal.ZERO);
                userAddressBo.setCollectible(UserAddressCollectible.NO);
                userAddressBo.setUserId("HOT");
                userAddressService.insertByBo(userAddressBo);
            });
        });

        return apiKeyVo;
    }

    @Override
    public MerchantVo getByToken(String token) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<Merchant>()
            .eq(Merchant::getToken, token));
    }

    /**
     * 查询商户信息
     *
     * @param id 主键
     * @return 商户信息
     */
    @Override
    public MerchantVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商户信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商户信息分页列表
     */
    @Override
    public TableDataInfo<MerchantVo> queryPageList(MerchantBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Merchant> lqw = buildQueryWrapper(bo);
        Page<MerchantVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商户信息列表
     *
     * @param bo 查询条件
     * @return 商户信息列表
     */
    @Override
    public List<MerchantVo> queryList(MerchantBo bo) {
        LambdaQueryWrapper<Merchant> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Merchant> buildQueryWrapper(MerchantBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Merchant> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Merchant::getId);
        lqw.ne(Merchant::getId, 1L);
        lqw.eq(bo.getMerchantSysVersion() != null, Merchant::getMerchantSysVersion, bo.getMerchantSysVersion());
        lqw.like(StringUtils.isNotBlank(bo.getName()), Merchant::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getToken()), Merchant::getToken, bo.getToken());
        lqw.eq(StringUtils.isNotBlank(bo.getCallbackUrl()), Merchant::getCallbackUrl, bo.getCallbackUrl());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            Merchant::getCreateTime ,params.get("beginCreateTime"), params.get("endCreateTime"));
        return lqw;
    }

    /**
     * 新增商户信息
     *
     * @param bo 商户信息
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(MerchantBo bo) {
        Merchant add = MapstructUtils.convert(bo, Merchant.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商户信息
     *
     * @param bo 商户信息
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(MerchantBo bo) {
        Merchant update = MapstructUtils.convert(bo, Merchant.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Merchant entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商户信息信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
