package com.iwhalecloud.bote.entity.base;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 缓存配置
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@Schema(description = "缓存配置")
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_cache_cfg")
public class CacheConfigEntity extends BaseEntity {
  @Schema(description = "缓存配置ID")
  private Long cacheConfigId;
  @Schema(description = "分组编码")
  private String groupCode;
  @Schema(description = "分组名称")
  private String groupName;
  @Schema(description = "缓存描述")
  private String viewDesc;
  @Schema(description = "缓存key")
  private String cacheKey;
  @Schema(description = "缓存名称")
  private String cacheName;
  @Schema(description = "是否支持一键刷新")
  private String refrenshAllFlag;
  @Schema(description = "缓存key前缀")
  private String cacheKeyPrefix;
}
