package com.iwhalecloud.bote.service.plugin.runner;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.OcrUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.FileReadPluginParams;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 文件内容读取插件执行器
 *
 * @author qian.sisheng
 * @since 2025-04-09
 */
@Component
public class FileReadPlugin extends AbstractPlugin<FileReadPluginParams> {

  public FileReadPlugin() {
    super(FileReadPluginParams.class);
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
    return PluginConsts.PLUGIN_CODE_FILE_READ;
  }

  @Override
  public void validateParams(FileReadPluginParams params) {
    Assert.notNull(params.getFileId(), "文件ID不能为空");
  }


  @Override
  @SuppressWarnings("unchecked")
  public Object doRun(FileReadPluginParams plugin) {
    Assert.notNull(plugin.getFileId(), "文件ID不能为空");
    Object content = OcrUtil.identifyWords(plugin.getFileId());
    if (content instanceof List) {
      List<String> contentList = (List<String>) content;
      // 拼接字符串
      return ImmutableMap.of("fileContent", String.join("\n", contentList));
    }
    return ImmutableMap.of("fileContent", content);
  }
}
