package com.iwhalecloud.bote.doc.module.document.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentActivityDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentActivityQueryParams;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentActivityEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 文档动态记录相关数据库操作
 *
 * @author Aiqing
 * @since 2025-08-21
 */
public interface DocumentActivityMapper {

  /**
   * 根据主键查询
   *
   * @param activityId 动态记录ID
   * @return 文档动态记录
   */
  DocumentActivityEntity selectByActivityId(@Param("activityId") Long activityId);

  /**
   * 插入记录
   *
   * @param documentActivity 文档动态记录
   * @return 影响行数
   */
  int insert(@Param("documentActivity") DocumentActivityEntity documentActivity);

  /**
   * 根据文档ID查询动态记录列表
   *
   * @param documentId 文档ID
   * @param limit 限制条数
   * @return 动态记录列表
   */
  List<DocumentActivityEntity> selectByDocumentId(@Param("documentId") String documentId,
                                                  @Param("limit") Integer limit);

  /**
   * 根据用户ID查询动态记录列表
   *
   * @param userId 用户ID
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param limit 限制条数
   * @return 动态记录列表
   */
  List<DocumentActivityEntity> selectByUserId(@Param("userId") Long userId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("limit") Integer limit);

  /**
   * 根据文档库ID查询动态记录列表
   *
   * @param libraryId 文档库ID
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param limit 限制条数
   * @return 动态记录列表
   */
  List<DocumentActivityEntity> selectByLibraryId(@Param("libraryId") String libraryId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("limit") Integer limit);

  /**
   * 根据操作类型查询动态记录列表
   *
   * @param actionType 操作类型
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param limit 限制条数
   * @return 动态记录列表
   */
  List<DocumentActivityEntity> selectByActionType(@Param("actionType") String actionType,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("limit") Integer limit);

  /**
   * 统计文档访问次数
   *
   * @param documentId 文档ID
   * @param actionType 操作类型
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 访问次数
   */
  Long countByDocumentIdAndActionType(@Param("documentId") String documentId,
                                      @Param("actionType") String actionType,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

  /**
   * 根据条件统计动态记录数量
   *
   * @param documentId 文档ID
   * @param libraryId 文档库ID
   * @param userId 用户ID
   * @param actionType 操作类型
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 记录数量
   */
  Long countByCondition(@Param("documentId") String documentId,
                        @Param("libraryId") String libraryId,
                        @Param("userId") Long userId,
                        @Param("actionType") String actionType,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

  /**
   * 分页查询文档动态记录， 由于可能查询被删除的记录，此处查询不过滤已删除状态的文档
   *
   * @param queryParams 查询参数
   * @return 动态记录分页结果
   */
  Page<DocumentActivityDTO> selectActivityPage(@Param("queryParams") DocumentActivityQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询指定时间范围内有更新的文档ID列表（去重）
   *
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 文档ID列表
   */
  List<String> selectUpdatedDocumentIdsByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);
}
