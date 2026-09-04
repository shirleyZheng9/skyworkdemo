package com.iwhalecloud.bote.service.plugin.runner;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.JuzhiOcrUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.JuzhiDictFileParsePluginParams;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * 聚智平台DICT项目团队专用的文件内容解析插件执行器
 *
 * @author tingyun.wang
 * @since 2025-07-16
 */
@Component
@ConditionalOnBooleanProperty("juzhi2.dict.enabled")
public class JuzhiDictFileParsePlugin extends AbstractPlugin<JuzhiDictFileParsePluginParams> {

  public JuzhiDictFileParsePlugin() {
    super(JuzhiDictFileParsePluginParams.class);
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("fileId", "文件ID", AttrDataType.INTEGER)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("fileContent", "文件内容", AttrDataType.STRING)));
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_JUZHI_DICT_FILE_PARSE;
  }

  @Override
  public void validateParams(JuzhiDictFileParsePluginParams params) {
    Assert.notNull(params.getFileId(), "文件ID不能为空");
  }


  @Override
  @SuppressWarnings("unchecked")
  public Object doRun(JuzhiDictFileParsePluginParams plugin) {
    Assert.notNull(plugin.getFileId(), "文件ID不能为空");
    Object content = JuzhiOcrUtil.parseFileForDict(plugin.getFileId());
    if (content instanceof List) {
      List<String> contentList = (List<String>) content;
      // 拼接字符串
      return ImmutableMap.of("fileContent", String.join("\n", contentList));
    }
    return ImmutableMap.of("fileContent", content);
  }
}
