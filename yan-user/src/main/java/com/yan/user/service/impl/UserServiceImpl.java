package com.yan.user.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.*;
import cn.hutool.crypto.digest.BCrypt;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.yan.user.domain.bo.UserBo;
import com.yan.user.domain.vo.UserVo;
import com.yan.user.domain.User;
import com.yan.user.mapper.UserMapper;
import com.yan.user.service.IUserService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户信息Service业务层处理
 *
 * @author Yan
 * @date 2025-06-09
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements IUserService {

    private final UserMapper baseMapper;

    /**
     * 查询用户信息
     *
     * @param userId 主键
     * @return 用户信息
     */
    @Override
    public UserVo queryById(Long userId){
        return baseMapper.selectVoById(userId);
    }

    @Override
    public UserVo queryByUsername(String username) {
        LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
        lqw.eq(User::getUserName, username);
        return baseMapper.selectVoOne(lqw);
    }

    @Override
    public UserVo getUser(String name) {
        UserVo user = baseMapper.selectVoOne(new LambdaQueryWrapper<User>().eq(User::getUserName, name).or().eq(User::getPhonenumber, name).or().eq(User::getEmail, name));
        if(user == null) {
            log.error("找不到账号 {}", name);
            throw new ServiceException("The account cannot be found.");
        }
        return user;
    }

    @Override
    public UserVo queryByParentId(Long parentId) {
        LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
        lqw.eq(User::getParentId, parentId);
        return baseMapper.selectVoOne(lqw);
    }

    @Override
    public Long getFirstChildCount(Long userId) {
        Assert.notNull(userId);
        LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
        lqw.eq(User::getParentId, userId);
        Long count = baseMapper.selectCount(lqw);
        return ObjectUtil.isNull(count) ? 0L : count;
    }

    @Override
    public Long countByReferrerIds(String userId) {
        Long count = baseMapper.countByReferrerIds(userId);
        return ObjectUtil.isNull(count) ? 0L : count;
    }

    /**
     * 分页查询用户信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户信息分页列表
     */
    @Override
    public TableDataInfo<UserVo> queryPageList(UserBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<User> lqw = buildQueryWrapper(bo);
        Page<UserVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户信息列表
     *
     * @param bo 查询条件
     * @return 用户信息列表
     */
    @Override
    public List<UserVo> queryList(UserBo bo) {
        LambdaQueryWrapper<User> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<User> buildQueryWrapper(UserBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(User::getUserId);
        lqw.like(StringUtils.isNotBlank(bo.getUserName()), User::getUserName, bo.getUserName());
        lqw.like(StringUtils.isNotBlank(bo.getNickName()), User::getNickName, bo.getNickName());
        lqw.eq(bo.getUserType() != null, User::getUserType, bo.getUserType());
        lqw.like(StringUtils.isNotBlank(bo.getEmail()), User::getEmail, bo.getEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getAreacode()), User::getAreacode, bo.getAreacode());
        lqw.like(StringUtils.isNotBlank(bo.getPhonenumber()), User::getPhonenumber, bo.getPhonenumber());
        lqw.eq(StringUtils.isNotBlank(bo.getSex()), User::getSex, bo.getSex());
        lqw.eq(bo.getAvatar() != null, User::getAvatar, bo.getAvatar());
        lqw.eq(StringUtils.isNotBlank(bo.getPassword()), User::getPassword, bo.getPassword());
        lqw.eq(bo.getVipLevel() != null, User::getVipLevel, bo.getVipLevel());
        lqw.eq(StringUtils.isNotBlank(bo.getPayPwd()), User::getPayPwd, bo.getPayPwd());
        lqw.eq(StringUtils.isNotBlank(bo.getInviteCode()), User::getInviteCode, bo.getInviteCode());
        lqw.eq(bo.getParentId() != null, User::getParentId, bo.getParentId());
        lqw.eq(StringUtils.isNotBlank(bo.getReferrerIds()), User::getReferrerIds, bo.getReferrerIds());
        lqw.eq(bo.getPoint() != null, User::getPoint, bo.getPoint());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), User::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getLoginIp()), User::getLoginIp, bo.getLoginIp());
        lqw.eq(bo.getLoginDate() != null, User::getLoginDate, bo.getLoginDate());
        return lqw;
    }

    /**
     * 新增用户信息
     *
     * @param bo 用户信息
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(UserBo bo) {
        User add = MapstructUtils.convert(bo, User.class);
        add.setUserCode(IdUtil.fastSimpleUUID());
        if(StrUtil.isNotBlank(bo.getPassword()))
            add.setPassword(BCrypt.hashpw(bo.getPassword()));
        if(StrUtil.isNotBlank(bo.getPayPwd()))
            add.setPayPwd(BCrypt.hashpw(bo.getPayPwd()));
        add.setInviteCode(generateInviteCode());
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setUserId(add.getUserId());
        }
        return flag;
    }

    /**
     * 修改用户信息
     *
     * @param bo 用户信息
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(UserBo bo) {
        if(StrUtil.isNotBlank(bo.getPassword()))
            bo.setPassword(BCrypt.hashpw(bo.getPassword()));
        if(StrUtil.isNotBlank(bo.getPayPwd()))
            bo.setPayPwd(BCrypt.hashpw(bo.getPayPwd()));
        if(ObjectUtil.isNotNull(bo.getParentId()))
            bindRelation(bo);
        User update = MapstructUtils.convert(bo, User.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(User entity){
        //TODO 做一些数据校验,如唯一约束
        LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
        lqw.eq(User::getUserName, entity.getUserName());
        if(baseMapper.exists(lqw)) throw new ServiceException("Username already exists.");
    }

    /**
     * 校验并批量删除用户信息信息
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

    @Override
    public UserVo registerUser(UserBo bo, String parentInviteCode) {
        bo.setCreateBy("System");
        bo.setUpdateBy("System");
        bo.setInviteCode(generateInviteCode());
        bo.setUserType(UserType.APP_USER);
        bo.setNickName(RandomUtil.randomString(8));
        bo.setUserCode(IdUtil.fastSimpleUUID());
        if(StrUtil.isNotBlank(parentInviteCode))
            bindRelation(bo, parentInviteCode);
        User user = MapstructUtils.convert(bo, User.class);
        validEntityBeforeSave(user);
        if(baseMapper.insert(user) <= 0) throw new UserException("user.register.error");;
        UserVo userVo = MapstructUtils.convert(user, UserVo.class);
        return userVo;
    }

    @Override
    public boolean bindRelation(UserBo user, String inviteCode) {
        Assert.notNull(user);
        Assert.notBlank(inviteCode);
        UserVo parentUserVo = getUserByInviteCode(inviteCode);
        if (ObjUtil.isNull(parentUserVo)) {
            log.warn("邀請碼是{}的上级用戶找不到", inviteCode);
            throw new ServiceException(MessageUtils.message("referralCode.error"));
        }
        UserBo parentUserBo = MapstructUtils.convert(parentUserVo, UserBo.class);
        return bindRelation(user, parentUserBo);
    }

    @Override
    public boolean bindRelation(UserBo user, UserBo parentUser) {
        Assert.notNull(user);
        Assert.notNull(parentUser);
        if (StringUtils.isNotBlank(user.getReferrerIds())) {
            log.warn("{}已有綁定關係", user.getUserName());
            return false;
        }
        // 不能自己邀请自己
        if (user.getUserId().equals(parentUser.getUserId())) {
            log.warn("不能自己邀请自己");
            throw new ServiceException(MessageUtils.message("referralCode.myself.error"));
        }
        user.setParentId(parentUser.getUserId());

        String[] referrerIds = StrUtil.splitToArray(
            StringUtils.isNotBlank(parentUser.getReferrerIds()) ? parentUser.getReferrerIds() : null, ",");
        referrerIds = ArrayUtil.insert(referrerIds, 0, parentUser.getUserId().toString());
        user.setReferrerIds(ArrayUtil.join(referrerIds, ","));
        return true;
    }

    @Override
    public boolean bindRelation(UserBo user) {
        Assert.notNull(user);
        UserVo parentUserVo = queryByParentId(user.getParentId());
        if (ObjUtil.isNull(parentUserVo)) {
            log.warn("parentId是{}的上级用戶找不到", user.getParentId());
            throw new ServiceException(MessageUtils.message("parentId.error"));
        }
        UserBo parentUserBo = MapstructUtils.convert(parentUserVo, UserBo.class);
        return bindRelation(user, parentUserBo);
    }

    @Override
    public UserVo getUserByInviteCode(String inviteCode) {
        LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
        lqw.eq(User::getInviteCode, inviteCode);
        return baseMapper.selectVoOne(lqw);
    }

    @Override
    public String getInviteCode(Long userId) {
        UserVo vo = baseMapper.selectVoById(userId);
        String inviteCode = null;
        if (StringUtils.isNotBlank(vo.getInviteCode())) {
            inviteCode = vo.getInviteCode();
        } else {
            inviteCode = generateInviteCode();
            vo.setInviteCode(inviteCode);
            User user = MapstructUtils.convert(vo, User.class);
            baseMapper.updateById(user);
        }
        return inviteCode;
    }

    @Override
    public String generateInviteCode() {
        String inviteCode = null;
        boolean b = true;
        while (b) {
            inviteCode = RandomUtil.randomStringUpper(6);
            LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
            lqw.eq(User::getInviteCode, inviteCode);
            User oldUser = baseMapper.selectOne(lqw);
            if (oldUser == null) {
                b = false;
            }
        }
        return inviteCode;
    }
}
