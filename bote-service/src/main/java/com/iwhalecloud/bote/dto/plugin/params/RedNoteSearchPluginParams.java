package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 小红书搜索插件参数
 *
 * @author qian.sisheng
 * @since 2025-07-22
 */
@Getter
@Setter
@ToString
public class RedNoteSearchPluginParams extends AbstractPluginParams {
  /** Cookie */
  private String cookie;
  /** 关键词 */
  private String keyword;
  /** 总数 */
  private Integer total;
  /** 点赞数排序  desc:降序; asc:升序 */
  private String orderByLikedCount;

  public RedNoteSearchPluginParams() {
    super(PluginConsts.PLUGIN_CODE_RED_NOTE_SEARCH_COMMON);
  }
}
