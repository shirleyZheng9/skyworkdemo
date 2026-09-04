package com.iwhalecloud.bote.service.asr.offline.adpter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.PathUtil;
import com.iwhalecloud.bote.common.util.VideoSplitUtil;
import com.iwhalecloud.bote.common.util.VideoSplitUtil.SplitResult;
import com.iwhalecloud.bote.service.asr.offline.AbstractAsrRecognitionAdapter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 默认语音识别适配器(wct-audio)
 *
 * @author qian.sisheng
 * @since 2025-10-27
 */
@Component
public class DefaultAsrRecognitionAdapter extends AbstractAsrRecognitionAdapter {

  @Override
  protected String doRecognize(File audioFile) {
    // 语音识别能力，限制文件不可超过 3M，需要切分处理
    SplitResult splitResult = VideoSplitUtil.splitVideoBySize(audioFile, VideoSplitUtil.DEFAULT_CHUNK_SIZE_MB);
    StringBuilder content = new StringBuilder();

    try {
      for (String chunk : splitResult.getChunkFiles()) {
        FileSystemResource resource = new FileSystemResource(new File(chunk));
        MultiValueMap<String, Object> args = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        args.add("files", resource);
        args.add("keys", "string");
        args.add("lang", "auto");

        Map<String, Object> result = HttpUtil.post(getRequiredVideoRecognizeUrl(), args, new ParameterizedTypeReference<>() {
        }, headers);

        if (result != null && result.containsKey("result")) {
          List<Map<String, Object>> resultList = JsonUtil.parseJson(JsonUtil.toJsonString(result.get("result")), new TypeReference<>() {
          });
          content.append(
            CollectionUtils.emptyIfNull(resultList).stream().map(p -> MapUtils.getString(p, "clean_text", MapUtils.getString(p, "text", "")))
              .collect(Collectors.joining("")));
        }
        else {
          throw BaseErrorConstant.OCR_IDENTIFY_WORDS_ERROR.toException(result == null ? "未知错误" : JsonUtil.toJsonString(result));
        }
      }
    }
    finally {
      String outputDir = splitResult.getOutputDir();
      if (StringUtils.isNotEmpty(outputDir)) {
        Path dir = PathUtil.resolvePath(outputDir);
        FileUtils.deleteQuietly(dir.toFile());
      }
    }

    return content.toString();
  }

  @Override
  public String getVideoRecognizeType() {
    return "default";
  }

}
