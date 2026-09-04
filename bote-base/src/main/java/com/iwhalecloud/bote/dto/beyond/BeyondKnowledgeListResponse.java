package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

/**
 * 百应知识库列表查询响应
 *
 * @author he.duming
 * @since 2025/01/17
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BeyondKnowledgeListResponse extends BeyondResponse<BeyondKnowledgeListResponse.DataWrapper> {

  /**
   * 数据包装类
   */
  @Getter
  @Setter
  @ToString
  public static class DataWrapper {
    /** 当前页码 起始页码为1 */
    private Integer pageIndex;
    /** 每页条数 每页最大展示条数 */
    private Integer pageSize;
    /** 数据列表 当前页数据，为空数组时无数据 */
    private List<KnowledgeResource> rows;
    /** 总条数 符合条件的总数据量 */
    private Long total;
    /** 总页数 总页数=总条数/每页条数，向上取整 */
    private Integer totalPage;
  }

  /**
   * 知识库资源信息
   */
  @Getter
  @Setter
  @ToString
  public static class KnowledgeResource {
    /** 资源图标 */
    private String avatar;
    /** 所属目录ID 资源所属目录的标识 */
    private Long catalogId;
    /** 所属企业 资源归属的企业标识 */
    private Long comAcctId;
    /** 创建人ID 创建资源的用户标识 */
    private Long createBy;
    /** 创建时间 资源创建的时间戳 */
    private String createTime;
    /** 创建人名称 创建资源的用户名称 */
    private String createUserName;
    /** 服务模式 远程服务模式，可选值：hosted/本地local */
    private String hostType;
    /** 索引清单 资源关联的索引列表 */
    private String indexList;
    /** 归属组织 资源归属的组织标识 */
    private Long manOrgId;
    /** 授权管理员 负责管理资源的用户标识 */
    private Long manUserId;
    /** 资源业务类型 文档库类型，可选值：AGENT/智能体、DOC/文档库、PLGIN/插件等 */
    private String resourceBizType;
    /** 草稿版本号 资源草稿版本的编号 */
    private Long resourceDVerid;
    /** 资源描述 资源的详细描述信息 */
    private String resourceDesc;
    /** 资源标识 资源的唯一标识ID */
    private Long resourceId;
    /** 资源标识字符串 资源的唯一标识ID */
    private String resourceIdStr;
    /** 资源名称 资源的名称 */
    private String resourceName;
    /** 正式版本号 资源正式版本的编号 */
    private Long resourceRVerid;
    /** 来源资源ID 外部系统资源的原始ID */
    private Long resourceSourcePkId;
    /** 资源状态 资源的状态标识，具体含义需结合业务定义 */
    private Integer resourceStatus;
    /** 资源类型 原子资源，可选值：ATOM/原子资源、COMBIN/组合资源 */
    private String resourceType;
    /** 引用资源版本ID 引用的资源版本标识 */
    private String resourceVersionId;
    /** 是否可回滚 是否支持版本回滚，true表示可回滚 */
    private Boolean rollback;
    /** 常见问题 资源相关的常见问题说明 */
    private String sample;
    /** 外系统编码 所属系统标识，可选值：BYAI/百应、BOT/博特等 */
    private String systemCode;
    /** 标签 用于检索的关键字标签，多标签用逗号分隔 */
    private String tags;
    /** 更新人ID 最后更新资源的用户标识 */
    private Long updateBy;
    /** 更新时间 资源最后更新的时间戳 */
    private String updateTime;
  }
}
