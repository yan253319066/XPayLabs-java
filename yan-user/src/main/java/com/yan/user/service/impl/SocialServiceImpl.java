package com.yan.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.user.domain.Social;
import com.yan.user.domain.bo.SocialBo;
import com.yan.user.domain.vo.SocialVo;
import com.yan.user.mapper.SocialMapper;
import com.yan.user.service.ISocialService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 社会化关系Service业务层处理
 *
 * @author thiszhc
 * @date 2023-06-12
 */
@RequiredArgsConstructor
@Service
public class SocialServiceImpl implements ISocialService {

    private final SocialMapper baseMapper;


    /**
     * 查询社会化关系
     */
    @Override
    public SocialVo queryById(String id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 授权列表
     */
    @Override
    public List<SocialVo> queryList(SocialBo bo) {
        LambdaQueryWrapper<Social> lqw = new LambdaQueryWrapper<Social>()
            .eq(ObjectUtil.isNotNull(bo.getUserId()), Social::getUserId, bo.getUserId())
            .eq(StringUtils.isNotBlank(bo.getAuthId()), Social::getAuthId, bo.getAuthId())
            .eq(StringUtils.isNotBlank(bo.getSource()), Social::getSource, bo.getSource());
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public List<SocialVo> queryListByUserId(Long userId) {
        return baseMapper.selectVoList(new LambdaQueryWrapper<Social>().eq(Social::getUserId, userId));
    }


    /**
     * 新增社会化关系
     */
    @Override
    public Boolean insertByBo(SocialBo bo) {
        Social add = MapstructUtils.convert(bo, Social.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            if (add != null) {
                bo.setId(add.getId());
            } else {
                return false;
            }
        }
        return flag;
    }

    /**
     * 更新社会化关系
     */
    @Override
    public Boolean updateByBo(SocialBo bo) {
        Social update = MapstructUtils.convert(bo, Social.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Social entity) {
        //TODO 做一些数据校验,如唯一约束
    }


    /**
     * 删除社会化关系
     */
    @Override
    public Boolean deleteWithValidById(Long id) {
        return baseMapper.deleteById(id) > 0;
    }


    /**
     * 根据 authId 查询用户信息
     *
     * @param authId 认证id
     * @return 授权信息
     */
    @Override
    public List<SocialVo> selectByAuthId(String authId) {
        return baseMapper.selectVoList(new LambdaQueryWrapper<Social>().eq(Social::getAuthId, authId));
    }

}
