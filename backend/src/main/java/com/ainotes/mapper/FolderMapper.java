package com.ainotes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ainotes.entity.Folder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件夹Mapper接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Mapper
public interface FolderMapper extends BaseMapper<Folder> {

}
