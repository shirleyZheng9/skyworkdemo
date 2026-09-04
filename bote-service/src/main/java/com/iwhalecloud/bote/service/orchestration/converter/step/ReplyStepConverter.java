package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.ContentTypeConfig;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.DownloadConfig;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.RelatedConfig;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * 输出内容步骤转换器
 *
 * @author chen.linfa
 * @since 2024-08-06
 */
public class ReplyStepConverter extends AbstractStepConverter<ReplyStep> {
  public ReplyStepConverter() {
    super(ReplyStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, ReplyStep step, ConverterContext context) {
    step.setMessageContent(parseRequiredJsonAttr(node, "messageContent", "消息内容", String.class));
    step.setDownload(parseJsonAttr(node, "download", DownloadConfig.class));
    step.setRelated(parseJsonAttr(node, "related", RelatedConfig.class));

    String contentType = parseJsonAttr(node, "contentType", String.class);
    ContentTypeConfig config = new ContentTypeConfig();
    config.setContentType(StringUtils.isEmpty(contentType) ? BaseConsts.REPLY_CONTENT_TYPE_MARKDOWN : contentType);
    step.setContentTypeConfig(config);
    // 规范化段落类型的配置参数
    if (BaseConsts.REPLY_CONTENT_TYPE_PARAGRAPH.equals(contentType)) {
      String group = parseJsonAttr(node, "paragraphGroup", String.class);
      String sortby = parseJsonAttr(node, "paragraphSortby", String.class);
      config.setGroup(StringUtils.isEmpty(group) ? "default" : group);
      config.setSortby(StringUtils.isEmpty(sortby) ? "1" : sortby);
    }
  }
}
