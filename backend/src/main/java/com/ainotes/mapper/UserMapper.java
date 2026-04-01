package com.ainotes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ainotes.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
