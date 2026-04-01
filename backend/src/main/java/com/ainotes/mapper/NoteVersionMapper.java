package com.ainotes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ainotes.entity.NoteVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记版本Mapper接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Mapper
public interface NoteVersionMapper extends BaseMapper<NoteVersion> {

}
