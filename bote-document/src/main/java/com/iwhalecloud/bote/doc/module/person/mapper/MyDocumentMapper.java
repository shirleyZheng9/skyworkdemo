package com.iwhalecloud.bote.doc.module.person.mapper;

import com.iwhalecloud.bote.doc.module.person.dto.MyDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.MyDocumentQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 我的文档相关数据库操作
 *
 * @author yangran
 * @since 2025-08-18
 */
public interface MyDocumentMapper {

  /**
   * 查询我的文档, 不分页
   */
  List<MyDocumentDTO> selectMyDocumentList(@Param("queryParams") MyDocumentQueryParams queryParams);

  /**
   * 检查用户个人文档库是否存在
   */
  String existsUserDocumentLibrary(@Param("userId") Long userId, @Param("spaceId") Long spaceId);

  /**
   * 检查用户的内置文件夹是否存在
   */
  boolean existsBuiltinFolder(@Param("userId") Long userId, @Param("spaceId") Long spaceId, @Param("builtinType") String builtinType,
                              @Param("libraryId") String libraryId);
}
