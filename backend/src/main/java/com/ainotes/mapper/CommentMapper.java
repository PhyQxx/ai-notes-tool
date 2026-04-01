package com.ainotes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ainotes.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}
