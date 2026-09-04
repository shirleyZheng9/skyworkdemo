package com.iwhalecloud.bote.mapper.base;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.base.SensitiveWordDTO;
import com.iwhalecloud.bote.dto.base.query.SensitiveWordQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 敏感词管理相关数据库操作
 *
 * @author bianjp
 * @since 2024-08-02
 */
public interface SensitiveWordMapper {

  /**
   * 新增敏感词
   */
  int insert(@Param("dto") SensitiveWordDTO sensitiveWord);

  /**
   * 修改敏感词
   */
  int update(@Param("dto") SensitiveWordDTO sensitiveWord);

  /**
   * 删除敏感词
   */
  int delete(@Param("wordId") Long wordId, @Param("updatorId") Long updatorId);

  /**
   * 检查是否存在敏感词
   */
  boolean existsWord(@Param("word") String word);

  /**
   * 根据 ID 查询敏感词
   */
  @Nullable
  SensitiveWordDTO selectById(@Param("wordId") Long wordId);

  /**
   * 分页查询敏感词
   */
  Page<SensitiveWordDTO> selectSensitiveWordPage(@Param("query") SensitiveWordQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询黑名单列表
   */
  List<String> selectBlacklistWords();

  /**
   * 查询白名单列表
   */
  List<String> selectWhitelistWords();

}
