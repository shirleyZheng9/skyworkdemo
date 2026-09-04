package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书多维表格查询记录插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-25
 */
@Getter
@Setter
@ToString
public class LarkSearchRecordPluginParams extends AbstractLarkPluginParams {

  /** 用户 ID 类型 */
  private String userIdType;
  /** 分页标记，第一次请求不填，后续请求填上一次请求返回的 page_token */
  private String pageToken;
  /** 每页大小，最大值 500，默认值：20 */
  private Integer pageSize;
  /** 多维表格中视图的唯一标识 */
  private String viewId;
  /** 排序条件 */
  private SortDTO sort;
  /** 字段名称，用于指定本次查询返回记录中包含的字段 */
  private List<String> fieldNames;
  /** 筛选条件 */
  private FilterInfoDTO filter;

  public LarkSearchRecordPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_SEARCH_RECORD, "飞书多维表格查询记录插件");
  }

  @Getter
  @Setter
  @ToString
  private static final class SortDTO {
    /** 字段名称 */
    private String fieldName;
    /** 是否倒序排序 */
    private boolean desc;
  }

  @Getter
  @Setter
  @ToString
  private static final class FilterInfoDTO {
    /** 表示条件之间的逻辑连接词 ，可选值有 and：满足全部条件 or：满足任一条件 */
    private String conjunction;
    /** 筛选条件集合 */
    private List<ConditionDTO> conditions;
  }

  @Getter
  @Setter
  @ToString
  private static final class ConditionDTO {
    /** 筛选条件的左值，值为字段的名称 */
    private String fieldName;
    /** 条件运算符 */
    private String operator;
    /** 条件的值，可以是单个值或多个值的数组 */
    private List<String> value;
  }
}
