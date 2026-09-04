package com.iwhalecloud.bote.mapper.beyond;

import com.iwhalecloud.bote.entity.beyond.BeyondFileEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 百应文件相关数据库操作
 *
 * @author bianjp
 * @since 2025-07-19
 */
public interface BeyondFileMapper {

  /**
   * 插入记录
   */
  int insert(@Param("file") BeyondFileEntity file);

  /**
   * 根据百应文件 ID 列表批量查询对应的博特文件 ID
   */
  List<BeyondFileEntity> selectFileIdsByBeyondFileIds(@Param("beyondFileIds") List<Long> beyondFileIds);

}
