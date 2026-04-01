package com.ainotes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ainotes.entity.Note;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记Mapper接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {

}
