package com.yan.user.service;

import com.yan.user.domain.bo.SocialBo;
import com.yan.user.domain.vo.SocialVo;

import java.util.List;

/**
 * 社会化关系Service接口
 *
 * @author thiszhc
 */
public interface ISocialService {


    /**
     * 查询社会化关系
     */
    SocialVo queryById(String id);

    /**
     * 查询社会化关系列表
     */
    List<SocialVo> queryList(SocialBo bo);

    /**
     * 查询社会化关系列表
     */
    List<SocialVo> queryListByUserId(Long userId);

    /**
     * 新增授权关系
     */
    Boolean insertByBo(SocialBo bo);

    /**
     * 更新社会化关系
     */
    Boolean updateByBo(SocialBo bo);

    /**
     * 删除社会化关系信息
     */
    Boolean deleteWithValidById(Long id);


    /**
     * 根据 authId 查询
     */
    List<SocialVo> selectByAuthId(String authId);


}
