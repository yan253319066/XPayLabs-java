package com.yan.user.service;

import com.yan.user.domain.vo.NoticeVo;
import com.yan.user.domain.bo.NoticeBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户通知公告Service接口
 *
 * @author Yan
 * @date 2025-10-26
 */
public interface INoticeService {

    /**
     * 用户读公告
     * @param userId
     * @param id
     * @return
     */
    Boolean read(Long userId, Long id);

    /**
     * 查询用户通知公告
     *
     * @param id 主键
     * @return 用户通知公告
     */
    NoticeVo queryById(Long id);

    /**
     * 根据用户ID和公告ID查询
     * @param userId
     * @param id
     * @return
     */
    NoticeVo queryById(Long userId, Long id);

    /**
     * 分页查询用户通知公告列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户通知公告分页列表
     */
    TableDataInfo<NoticeVo> queryPageList(NoticeBo bo, PageQuery pageQuery);

    /**
     * app端查询自己和通用的公告
     * @param userId
     * @param pageQuery
     * @return
     */
    TableDataInfo<NoticeVo> queryPageList(Long userId, PageQuery pageQuery);

    /**
     * 查询符合条件的用户通知公告列表
     *
     * @param bo 查询条件
     * @return 用户通知公告列表
     */
    List<NoticeVo> queryList(NoticeBo bo);

    /**
     * 新增用户通知公告
     *
     * @param bo 用户通知公告
     * @return 是否新增成功
     */
    Boolean insertByBo(NoticeBo bo);

    /**
     * 修改用户通知公告
     *
     * @param bo 用户通知公告
     * @return 是否修改成功
     */
    Boolean updateByBo(NoticeBo bo);

    /**
     * 校验并批量删除用户通知公告信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
