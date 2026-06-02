package com.yan.user.service;

import com.yan.user.domain.vo.UserVo;
import com.yan.user.domain.bo.UserBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户信息Service接口
 *
 * @author Yan
 * @date 2025-06-09
 */
public interface IUserService {

    /**
     * 查询用户信息
     *
     * @param userId 主键
     * @return 用户信息
     */
    UserVo queryById(Long userId);

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    UserVo queryByUsername(String username);

    /**
     * 根据用户名，phone，email查询用户
     * @param name
     * @return
     */
    UserVo getUser(String name);

    /**
     * 根据父ID查询用户信息
     * @param parentId
     * @return
     */
    UserVo queryByParentId(Long parentId);

    /**
     *获取直推人数
     * @param userId
     * @return
     */
    Long getFirstChildCount(Long userId);

    /**
     * 获取所有下级用户人数
     * @param userId
     * @return
     */
    Long countByReferrerIds(String userId);

    /**
     * 分页查询用户信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户信息分页列表
     */
    TableDataInfo<UserVo> queryPageList(UserBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户信息列表
     *
     * @param bo 查询条件
     * @return 用户信息列表
     */
    List<UserVo> queryList(UserBo bo);

    /**
     * 新增用户信息
     *
     * @param bo 用户信息
     * @return 是否新增成功
     */
    Boolean insertByBo(UserBo bo);

    /**
     * 修改用户信息
     *
     * @param bo 用户信息
     * @return 是否修改成功
     */
    Boolean updateByBo(UserBo bo);

    /**
     * 校验并批量删除用户信息信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @param parentInviteCode 父邀请码
     * @return 结果
     */
    UserVo registerUser(UserBo user, String parentInviteCode);

    /**
     * 綁定上下級關係
     * @param user 当前用户
     * @param inviteCode 上级用户邀请码
     * @return
     */
    boolean bindRelation(UserBo user, String inviteCode);

    /**
     * 綁定上下級關係
     * @param user
     * @param parentUser
     * @return
     */
    boolean bindRelation(UserBo user, UserBo parentUser);

    /**
     * 綁定上下級關係
     * @param user
     * @return
     */
    boolean bindRelation(UserBo user);

    /**
     * 根据邀请码查询用户
     * @param inviteCode
     * @return
     */
    UserVo getUserByInviteCode(String inviteCode);

    /**
     * 获取用户邀请码
     * @param userId
     * @return
     */
    String getInviteCode(Long userId);

    /**
     * 生成邀请码
     *
     * @return
     */
    String generateInviteCode();
}
