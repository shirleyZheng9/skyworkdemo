package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import com.iwhalecloud.bote.dto.plugin.message.dingding.AbstractDingDingMessage;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 钉钉工作通知插件
 *
 * @author qian.sisheng
 * @since 2025-04-12
 */
@Getter
@Setter
@ToString
public class DingDingWorkNoticePluginParams extends AbstractPluginParams {
  /** 应用 ID */
  private Long agentId;
  /** 应用 key */
  private String appKey;
  /** 应用密钥 */
  private String appSecret;
  /** 接收人 ID 列表 */
  private List<String> userIdList;
  /** 部门 ID 列表 */
  private List<String> deptIdList;
  /** 是否发送给企业全部用户 */
  private boolean toAllUser;
  /** 消息内容 */
  private AbstractDingDingMessage message;


  public DingDingWorkNoticePluginParams() {
    super(PluginConsts.PLUGIN_CODE_DING_DING_WORK_NOTICE);
  }

}
