package com.iwhalecloud.bote.mapper.publish;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 资源发布记录管理
 *
 * @author lizuyin
 * @since 2025-07-25
 */
public interface ResourcePublishRecordMapper {

  /**
   * 新增资源发布记录
   *
   * @param record 资源发布记录
   * @return 结果
   */
  int insertResourcePublishRecord(@Param("dto") ResourcePublishRecordDTO record);

  /**
   * 根据资源ID和发布渠道查询发布记录
   *
   * @param resourceId 资源ID
   * @param publishChannel 发布渠道
   * @param tenantId 租户ID
   * @return 资源发布记录
   */
  ResourcePublishRecordDTO getRecordByResourceAndChannel(@Param("resourceId") Long resourceId,
                                                         @Param("publishChannel") String publishChannel,
                                                         @Param("tenantId") Long tenantId);

  /**
   * 根据外部资源ID(代理码)与渠道获取发布记录
   *
   * @param callbackCode 代理码（callbackCode）
   * @param publishChannel 发布渠道
   * @return 资源发布记录
   */
  ResourcePublishRecordDTO getRecordByExtResourceIdAndChannel(@Param("callbackCode") String callbackCode,
                                                              @Param("publishChannel") String publishChannel);

  /**
   * 更新资源发布记录
   *
   * @param record 资源发布记录
   * @return 结果
   */
  int updateResourcePublishRecord(@Param("dto") ResourcePublishRecordDTO record);

  /**
   * 删除资源发布记录
   */
  int deleteResourcePublishRecord(@Param("tenantId") Long tenantId, @Param("recordId") Long recordId, @Param("updatorId") Long updatorId);

  /**
   * 查询指定资源的发布记录
   */
  List<ResourcePublishRecordDTO> selectPublishRecordsByResourceId(@Param("tenantId") Long tenantId, @Param("resourceType") String resourceType,
                                                                  @Param("resourceId") Long resourceId);
  /**
   * 分页查询资源发布记录
   *
   * @param params 查询参数
   * @param rowBounds 分页参数
   * @return 资源发布记录列表
   */
  Page<ResourcePublishRecordDTO> selectResourcePublishRecordPage(@Param("params") ResourcePublishRecordQueryParams params,
                                                                 @Param("rowBounds") RowBounds rowBounds);

  /**
   * 根据回调编码获取发布记录
   *
   * @param callbackCode 回调编码
   * @return 资源发布记录
   */
  ResourcePublishRecordDTO getRecordByCallbackCode(@Param("callbackCode") String callbackCode);

  /**
   * 获取所有活跃的发布记录（用于定时任务检查和恢复连接）
   *
   * @return 活跃的发布记录列表
   */
  List<ResourcePublishRecordDTO> getActivePublishRecords();

  /**
   * 更新最后访问时间
   *
   * @param callbackCode 回调编码
   * @return 更新结果
   */
  int updateLastAccessTime(@Param("callbackCode") String callbackCode);

  /**
   * 更新发布参数（用于 WeClawBot 等渠道存储运行时状态如 cursor）
   *
   * @param callbackCode 回调编码
   * @param publishParams 发布参数 JSON
   * @return 更新结果
   */
  int updatePublishParams(@Param("callbackCode") String callbackCode, @Param("publishParams") String publishParams);

  /**
   * 根据记录ID查询发布记录
   *
   * @param recordId 记录ID
   * @return 资源发布记录
   */
  ResourcePublishRecordDTO selectById(@Param("recordId") Long recordId);

  /**
   * 根据条件查询发布记录
   *
   * @return 资源发布记录
   */
  ResourcePublishRecordDTO selectRecordForAiAgent(@Param("publishChannel") String publishChannel, @Param("tenantId") Long tenantId,
    @Param("botId") Long botId, @Param("creatorId") Long creatorId);

}
