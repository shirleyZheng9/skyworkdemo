package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WeChatMpApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpAddMaterialParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 微信公众号上传素材
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Component
public class WeChatMpAddMaterialPlugin extends AbstractPlugin<WeChatMpAddMaterialParams> {
  private final IFileStoreService fileStoreService;

  public WeChatMpAddMaterialPlugin(IFileStoreService fileStoreService) {
    super(WeChatMpAddMaterialParams.class);
    this.fileStoreService = fileStoreService;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WE_CHAT_MP_ADD_MATERIAL;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("fileId", "文件ID", AttrDataType.STRING),
      ParameterSpec.newProperty("description", "文件描述", AttrDataType.OBJECT),
      ParameterSpec.newProperty("accessToken", "公众号access_token", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("mediaId", "公众号素材库中素材Id", AttrDataType.STRING),
      ParameterSpec.newProperty("mediaUrl", "公众号素材库中素材url", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(WeChatMpAddMaterialParams params) {
    Assert.notNull(params.getFileId(), "fileId不能为空");
    Assert.notNull(params.getAccessToken(), "accessToken不能为空");
  }

  @Override
  public Object doRun(WeChatMpAddMaterialParams pluginParams) {
    FileInfoVO fileInfoVO = fileStoreService.getFileInfoById(pluginParams.getFileId());
    if (fileInfoVO == null || StringUtils.isEmpty(fileInfoVO.getFileName())) {
      throw new BssException("获取到的文件为空");
    }
    String fileType = fileInfoVO.getFileName().substring(fileInfoVO.getFileName().lastIndexOf(".") + 1);
    String materialType = getMaterialType(fileType, fileInfoVO.getFileSize());
    if (StringUtils.isEmpty(materialType)) {
      throw new BssException("不支持的文件类型或文件大小超出限制");
    }
    byte[] fileBytes = fileStoreService.downloadFile(fileInfoVO.getFileId());
    if (fileBytes == null) {
      throw new BssException("获取文件内容异常");
    }
    return WeChatMpApiUtil.addMaterial(pluginParams.getAccessToken(), fileBytes, materialType,
      pluginParams.getDescription(), fileInfoVO.getFileName());
  }

  private static String getMaterialType(String fileType, Long fileSize) {
    /*
      图片（image）: 10M，支持bmp/png/jpeg/jpg/gif格式
      语音（voice）：2M，播放长度不超过60s，mp3/wma/wav/amr格式
      视频（video）：10MB，支持MP4格式
      缩略图（thumb）：64KB，支持JPG格式
    */
    switch (fileType.toLowerCase()) {
      case "png":
      case "bmp":
      case "jpg":
      case "jpeg":
      case "gif":
        if (fileSize >= 10 * 1024 * 1024) {
          return null;
        }
        return "image";
      case "mp3":
      case "wma":
      case "wav":
      case "amr":
        if (fileSize >= 2 * 1024 * 1024) {
          return null;
        }
        return "voice";
      case "mp4":
        if (fileSize >= 10 * 1024 * 1024) {
          return null;
        }
        return "video";
      default:
        return null;
    }
  }

}
