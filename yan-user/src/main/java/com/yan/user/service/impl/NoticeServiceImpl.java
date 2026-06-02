package com.yan.user.service.impl;

import com.yan.user.enums.ReadStatus;
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
import com.yan.user.domain.bo.NoticeBo;
import com.yan.user.domain.vo.NoticeVo;
import com.yan.user.domain.Notice;
import com.yan.user.mapper.NoticeMapper;
import com.yan.user.service.INoticeService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户通知公告Service业务层处理
 *
 * @author Yan
 * @date 2025-10-26
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NoticeServiceImpl implements INoticeService {

    private final NoticeMapper baseMapper;

    @Override
    public Boolean read(Long userId, Long id) {
        LambdaQueryWrapper<Notice> lqw = Wrappers.lambdaQuery();
        lqw.eq(Notice::getUserId, userId);
        lqw.eq(Notice::getId, id);
        Notice notice = baseMapper.selectOne(lqw);
        notice.setId(id);
        notice.setReadStatus(ReadStatus.READ);
        return baseMapper.updateById(notice) > 0;
    }

    /**
     * 查询用户通知公告
     *
     * @param id 主键
     * @return 用户通知公告
     */
    @Override
    public NoticeVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public NoticeVo queryById(Long userId, Long id) {
        LambdaQueryWrapper<Notice> lqw = Wrappers.lambdaQuery();
        lqw.eq(Notice::getUserId, userId);
        lqw.eq(Notice::getId, id);
        return baseMapper.selectVoOne(lqw);
    }

    /**
     * 分页查询用户通知公告列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户通知公告分页列表
     */
    @Override
    public TableDataInfo<NoticeVo> queryPageList(NoticeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Notice> lqw = buildQueryWrapper(bo);
        Page<NoticeVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public TableDataInfo<NoticeVo> queryPageList(Long userId, PageQuery pageQuery) {
        LambdaQueryWrapper<Notice> lqw = Wrappers.lambdaQuery();
        lqw.in(Notice::getUserId,userId,0);
        Page<NoticeVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户通知公告列表
     *
     * @param bo 查询条件
     * @return 用户通知公告列表
     */
    @Override
    public List<NoticeVo> queryList(NoticeBo bo) {
        LambdaQueryWrapper<Notice> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Notice> buildQueryWrapper(NoticeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Notice> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Notice::getId);
        lqw.eq(bo.getUserId() != null, Notice::getUserId, bo.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getNoticeTitle()), Notice::getNoticeTitle, bo.getNoticeTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getNoticeType()), Notice::getNoticeType, bo.getNoticeType());
        lqw.eq(StringUtils.isNotBlank(bo.getNoticeContent()), Notice::getNoticeContent, bo.getNoticeContent());
        lqw.eq(bo.getStatus() != null, Notice::getStatus, bo.getStatus());
        lqw.eq(bo.getReadStatus() != null, Notice::getReadStatus, bo.getReadStatus());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            Notice::getCreateTime ,params.get("beginCreateTime"), params.get("endCreateTime"));
        return lqw;
    }

    /**
     * 新增用户通知公告
     *
     * @param bo 用户通知公告
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(NoticeBo bo) {
        Notice add = MapstructUtils.convert(bo, Notice.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户通知公告
     *
     * @param bo 用户通知公告
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(NoticeBo bo) {
        Notice update = MapstructUtils.convert(bo, Notice.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Notice entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户通知公告信息
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
