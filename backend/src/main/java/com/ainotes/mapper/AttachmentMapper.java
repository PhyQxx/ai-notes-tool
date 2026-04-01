package com.ainotes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ainotes.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 附件Mapper接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Mapper
public interface AttachmentMapper extends BaseMapper<Attachment> {

}
