package com.yan.user.mapper;

import com.yan.user.domain.User;
import com.yan.user.domain.vo.UserVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 用户信息Mapper接口
 *
 * @author Yan
 * @date 2025-06-09
 */
public interface UserMapper extends BaseMapperPlus<User, UserVo> {

	@Select("SELECT * FROM `tb_user` WHERE FIND_IN_SET(#{userId}, referrer_ids) and mobile LIKE '%#{name}%' and username LIKE '%#{name}%' and email LIKE '%#{name}%' and nickname LIKE '%#{name}%' and status = 0")
	List<User> getReferrerIdsByName(@Param("userId") String userId, @Param("name") String name);

	/**
	 * 获取所有下级用户对象
	 * @param userId
	 * @return
	 */
	@Select("SELECT * FROM `tb_user` WHERE FIND_IN_SET(#{userId}, referrer_ids) and status = 0")
	List<User> listByReferrerIds(String userId);

	/**
	 * 获取所有下级用户人数
	 * @param userId
	 * @return
	 */
	@Select("SELECT COUNT(1) FROM `tb_user` WHERE FIND_IN_SET(#{userId}, referrer_ids) and status = 0")
	Long countByReferrerIds(String userId);
}
