package com.iwhalecloud.bote.doc.module.crawl.step.transformer;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.doc.module.base.service.impl.FileUploadHelper;
import com.iwhalecloud.bote.doc.module.crawl.dto.CrawlResult;
import com.iwhalecloud.bote.doc.module.crawl.dto.DataUriPartsDTO;
import com.iwhalecloud.bote.doc.module.crawl.dto.ImageMatchDTO;
import com.iwhalecloud.bote.doc.module.crawl.dto.ImageProcessContextDTO;
import com.iwhalecloud.bote.doc.module.crawl.dto.PrefixMatchDTO;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * 转换器 - Image
 *
 * @author chen.linfa
 * @since 2026-02-21
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ImageTransformer {
  private static final Logger logger = LoggerFactory.getLogger(ImageTransformer.class);
  /** SVG 转 PNG 时的默认宽度（像素），用于未指定尺寸的 SVG */
  private static final float SVG_TO_PNG_DEFAULT_WIDTH = 800f;
  /** SVG 转 PNG 时的默认高度（像素），用于未指定尺寸的 SVG */
  private static final float SVG_TO_PNG_DEFAULT_HEIGHT = 600f;

  private final FileUploadHelper fileUploadHelper;

  /**
   * 调整 markdown 中的图片 URL
   */
  public String execute(String content, CrawlResult result) {
    return execute(content, result, null);
  }

  /**
   * 调整 markdown 中的图片 URL
   *
   * @param content Markdown 内容
   * @param result 爬取结果
   * @param sourceUrl 原始页面 URL（用于设置 Referer 请求头）
   * @return 处理后的 Markdown 内容
   */
  public String execute(String content, CrawlResult result, String sourceUrl) {
    if (isNothingToDo(content, result)) {
      return content;
    }

    try {
      ImageProcessContextDTO ctx = initProcessContext(result);
      processImages(ctx, sourceUrl);
      logProcessStats(ctx);
      return replaceIfNeeded(content, ctx.getUrlReplacementMap());
    }
    catch (Exception e) {
      logger.error("处理图片时发生异常", e);
      // 发生异常时返回原始内容
      return content;
    }
  }

  private boolean isNothingToDo(String content, CrawlResult result) {
    return StringUtils.isEmpty(content) || result == null || CollectionUtils.isEmpty(result.getImageUrls());
  }

  private ImageProcessContextDTO initProcessContext(CrawlResult result) {
    Map<String, String> urlReplacementMap = new HashMap<>();
    List<FileInfoVO> fileInfos = new ArrayList<>(result.getImageUrls().size());
    result.setFileInfos(fileInfos);
    ImageProcessContextDTO dto = new ImageProcessContextDTO();
    dto.setImageUrls(result.getImageUrls());
    dto.setUrlReplacementMap(urlReplacementMap);
    dto.setFileInfos(fileInfos);
    dto.setSuccess(0);
    dto.setFailed(0);
    dto.setSkipped(0);
    return dto;
  }

  private void processImages(ImageProcessContextDTO ctx, String sourceUrl) {
    int total = ctx.getImageUrls() == null ? 0 : ctx.getImageUrls().size();
    logger.info("开始处理图片下载，共 {} 张图片", total);
    if (ctx.getImageUrls() == null) {
      return;
    }
    for (String originalUrl : ctx.getImageUrls()) {
      if (shouldSkipUrl(originalUrl)) {
        ctx.setSkipped(ctx.getSkipped() + 1);
        continue;
      }
      boolean ok = processSingleImage(originalUrl, sourceUrl, ctx.getFileInfos(), ctx.getUrlReplacementMap());
      if (ok) {
        ctx.setSuccess(ctx.getSuccess() + 1);
      }
      else {
        ctx.setFailed(ctx.getFailed() + 1);
      }
    }
  }

  private boolean shouldSkipUrl(String originalUrl) {
    if (StringUtils.isBlank(originalUrl)) {
      logger.debug("跳过空图片URL");
      return true;
    }
    // 跳过 base64 编码的 data URI
    if (originalUrl.startsWith("data:") && originalUrl.contains(";base64")) {
      logger.debug("跳过 base64 编码的 data URI");
      return true;
    }
    return false;
  }

  private boolean processSingleImage(String originalUrl,
    String sourceUrl,
    List<FileInfoVO> fileInfos,
    Map<String, String> urlReplacementMap) {
    try {
      FileInfoVO fileInfo = downloadOrProcessImage(originalUrl, sourceUrl);
      if (fileInfo == null) {
        logger.atWarn().setMessage("图片下载失败，返回 null: imageUrl={}").addArgument(() -> abbreviate(originalUrl, 200)).log();
        return false;
      }
      fileInfos.add(fileInfo);
      String newUrl = buildImageUrl(fileInfo);
      if (StringUtils.isNotBlank(newUrl)) {
        urlReplacementMap.put(originalUrl, newUrl);
        logger.atDebug().setMessage("图片下载成功: originalUrl={}, newUrl={}").addArgument(() -> abbreviate(originalUrl, 100)).addArgument(() -> newUrl).log();
      }
      return true;
    }
    catch (Exception e) {
      logger.atWarn().setMessage("图片下载或上传失败，保留原始URL: imageUrl={}, error={}").addArgument(() -> abbreviate(originalUrl, 200)).addArgument(e::getMessage).log();
      return false;
    }
  }

  private FileInfoVO downloadOrProcessImage(String originalUrl, String sourceUrl) {
    if (originalUrl.startsWith("data:")) {
      logger.debug("处理 data URI 图片");
      return processDataUriImage(originalUrl);
    }
    logger.debug("下载图片: {}", originalUrl);
    return downloadAndUploadImage(originalUrl, sourceUrl);
  }

  private void logProcessStats(ImageProcessContextDTO ctx) {
    int total = ctx.getImageUrls() == null ? 0 : ctx.getImageUrls().size();
    logger.info("图片处理完成: 总数={}, 成功={}, 失败={}, 跳过={}", total, ctx.getSuccess(), ctx.getFailed(), ctx.getSkipped());
  }

  private String replaceIfNeeded(String content, Map<String, String> urlReplacementMap) {
    if (urlReplacementMap == null || urlReplacementMap.isEmpty()) {
      return content;
    }
    return replaceImageUrlsInMarkdown(content, urlReplacementMap);
  }

  private String abbreviate(String s, int max) {
    if (s == null) {
      return null;
    }
    if (s.length() <= max) {
      return s;
    }
    return s.substring(0, max) + "...";
  }

  /**
   * 处理非 base64 的 data URI 格式图片（如 SVG XML）。
   * 若为 image/svg+xml，会先转为 PNG 再上传；其他类型按原格式写入并上传。
   *
   * @param dataUri data URI 字符串，格式如：data:image/svg+xml,<?xml...>
   * @return 文件信息，失败返回 null
   */
  private FileInfoVO processDataUriImage(String dataUri) {
    Path tempFile = null;
    try {
      DataUriPartsDTO parts = parseDataUri(dataUri);
      if (parts == null) {
        logger.warn("无效的 data URI 格式: {}", dataUri);
        return null;
      }
      String mimeType = parts.getMimeType();
      boolean isSvg = StringUtils.defaultString(mimeType).toLowerCase().contains("svg");
      byte[] bytesToWrite;
      String extension;
      if (isSvg) {
        String svgContent = new String(decodeNonBase64Data(parts.getData()), StandardCharsets.UTF_8);
        bytesToWrite = convertSvgToPngBytes(svgContent);
        extension = "png";
      }
      else {
        bytesToWrite = decodeNonBase64Data(parts.getData());
        extension = extensionFromMime(mimeType);
      }
      tempFile = Files.createTempFile("bote-datauri-image-", "." + extension);
      Files.write(tempFile, bytesToWrite);
      return fileUploadHelper.uploadFile(tempFile.toFile());
    }
    catch (Exception e) {
      logger.atWarn()
        .setMessage("处理 data URI 图片失败: dataUri={}, error={}")
        .addArgument(() -> dataUri.length() > 100 ? dataUri.substring(0, 100) + "..." : dataUri)
        .addArgument(e::getMessage)
        .log();
      return null;
    }
    finally {
      deleteTempFileQuietly(tempFile);
    }
  }

  /**
   * 将 SVG 内容转为 PNG 字节数组
   *
   * @param svgContent SVG 文档字符串（XML）
   * @return PNG 字节数组
   * @throws TranscoderException 转换失败时抛出
   */
  private byte[] convertSvgToPngBytes(String svgContent) throws TranscoderException {
    String normalized = normalizeSvgContent(svgContent);
    PNGTranscoder transcoder = new PNGTranscoder();
    transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, SVG_TO_PNG_DEFAULT_WIDTH);
    transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, SVG_TO_PNG_DEFAULT_HEIGHT);
    TranscoderInput input = new TranscoderInput(new StringReader(normalized));
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    transcoder.transcode(input, new TranscoderOutput(outputStream));
    return outputStream.toByteArray();
  }

  /**
   * 规范化 SVG 字符串，避免 XML 解析报错（如「前言中不允许有内容」「根元素前面的标记必须格式正确」）。
   * 去除 BOM、控制字符、首尾空白、XML 声明，并保证从根元素 &lt;svg&gt; 开始。
   */
  private String normalizeSvgContent(String svgContent) {
    if (svgContent == null) {
      return "";
    }
    // 移除 XML 不允许的控制字符（易导致「根元素前面的标记必须格式正确」）
    String s = stripXmlInvalidControlChars(svgContent);
    s = s.trim();
    // 去除 UTF-8 BOM
    if (s.startsWith("\uFEFF")) {
      s = s.substring(1).trim();
    }
    // 若开头不是 '<'，从第一个 '<' 开始截取
    int firstLt = s.indexOf('<');
    if (firstLt > 0) {
      s = s.substring(firstLt);
    }
    // 去掉 <?xml ...?> 声明
    s = stripXmlDeclaration(s);
    s = s.trim();
    // 从根元素 <svg 开始截取
    int svgStart = findSvgRootStart(s);
    if (svgStart > 0) {
      s = s.substring(svgStart);
    }
    return s.trim();
  }

  /** 移除 XML 1.0 不允许的控制字符（保留 \t \n \r），避免解析报错 */
  private String stripXmlInvalidControlChars(String s) {
    if (s == null || s.isEmpty()) {
      return s;
    }
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c >= 0x20 || c == '\t' || c == '\n' || c == '\r') {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /** 返回根元素 &lt;svg 在字符串中的起始下标（不区分大小写），未找到返回 0 */
  private int findSvgRootStart(String s) {
    if (s == null || s.isEmpty()) {
      return 0;
    }
    int idx = s.toLowerCase().indexOf("<svg");
    return idx >= 0 ? idx : 0;
  }

  /** 移除字符串开头的 <?xml ...?> 处理指令（可有多条） */
  private String stripXmlDeclaration(String s) {
    if (s == null) {
      return "";
    }
    String rest = s.trim();
    while (rest.regionMatches(true, 0, "<?xml", 0, 5)) {
      int end = rest.indexOf("?>", 5);
      if (end < 0) {
        break;
      }
      rest = rest.substring(end + 2).trim();
    }
    return rest;
  }

  private DataUriPartsDTO parseDataUri(String dataUri) {
    if (StringUtils.isBlank(dataUri)) {
      return null;
    }
    int commaIndex = dataUri.indexOf(',');
    if (commaIndex < 0) {
      return null;
    }
    String header = dataUri.substring(0, commaIndex);
    String data = dataUri.substring(commaIndex + 1);
    String mimeType = extractMimeType(header);
    DataUriPartsDTO dto = new DataUriPartsDTO();
    dto.setMimeType(mimeType);
    dto.setData(data);
    return dto;
  }

  private String extractMimeType(String header) {
    if (StringUtils.isBlank(header) || !header.startsWith("data:")) {
      return "image/svg+xml";
    }
    String mimeType = header.substring(5);
    return StringUtils.defaultIfBlank(mimeType, "image/svg+xml");
  }

  private String extensionFromMime(String mimeType) {
    String mt = StringUtils.defaultString(mimeType).toLowerCase();
    if (mt.contains("svg")) {
      return "svg";
    }
    if (mt.contains("jpeg") || mt.contains("jpg")) {
      return "jpg";
    }
    if (mt.contains("png")) {
      return "png";
    }
    if (mt.contains("gif")) {
      return "gif";
    }
    if (mt.contains("webp")) {
      return "webp";
    }
    return "svg";
  }

  private byte[] decodeNonBase64Data(String data) {
    try {
      String decodedData = java.net.URLDecoder.decode(StringUtils.defaultString(data), "UTF-8");
      return decodedData.getBytes(StandardCharsets.UTF_8);
    }
    catch (Exception e) {
      logger.debug("URL 解码失败，使用原始数据: {}", e.getMessage());
      return StringUtils.defaultString(data).getBytes(StandardCharsets.UTF_8);
    }
  }

  private void deleteTempFileQuietly(Path tempFile) {
    if (tempFile == null) {
      return;
    }
    try {
      Files.deleteIfExists(tempFile);
    }
    catch (IOException e) {
      logger.warn("删除临时文件失败: {}", tempFile, e);
    }
  }

  /**
   * 下载图片并上传到文件系统。
   * 若 URL 指向 SVG（如 .svg），会先转为 PNG 再上传；转换失败则上传原 SVG。
   *
   * @param imageUrl 图片 URL
   * @param sourceUrl 原始页面 URL（用于设置 Referer 请求头）
   * @return 文件信息，失败返回 null
   */
  private FileInfoVO downloadAndUploadImage(String imageUrl, String sourceUrl) {
    Path tempFile = null;
    Path pngTempFile = null;
    HttpHeaders headers = null; // 在方法作用域中声明，以便在 catch 块中使用
    try {
      tempFile = createTempImageFile(imageUrl);
      headers = buildImageDownloadHeaders(imageUrl, sourceUrl);
      logDownloadHeaders(imageUrl, headers);
      downloadToFile(imageUrl, headers, tempFile);

      if (isSvgUrl(imageUrl)) {
        try {
          String svgContent = FileUtils.readFileToString(tempFile.toFile(), StandardCharsets.UTF_8);
          byte[] pngBytes = convertSvgToPngBytes(svgContent);
          pngTempFile = Files.createTempFile("bote-crawler-image-", ".png");
          Files.write(pngTempFile, pngBytes);
          return fileUploadHelper.uploadFile(pngTempFile.toFile());
        }
        catch (TranscoderException e) {
          logger.warn("SVG 转 PNG 失败，改为上传原 SVG: imageUrl={}, error={}", imageUrl, e.getMessage());
          return fileUploadHelper.uploadFile(tempFile.toFile());
        }
      }

      return fileUploadHelper.uploadFile(tempFile.toFile());
    }
    catch (HttpStatusCodeException e) {
      logHttpStatusError(imageUrl, headers, e);
      return null;
    }
    catch (Exception e) {
      logger.warn("下载或上传图片失败: imageUrl={}, error={}", imageUrl, e.getMessage(), e);
      return null;
    }
    finally {
      deleteTempFileQuietly(pngTempFile);
      deleteTempFileQuietly(tempFile);
    }
  }

  /** 根据 URL 判断是否为 SVG 图片（按路径扩展名 .svg） */
  private boolean isSvgUrl(String imageUrl) {
    return "svg".equalsIgnoreCase(getImageExtension(imageUrl));
  }

  private Path createTempImageFile(String imageUrl) throws IOException {
    String extension = getImageExtension(imageUrl);
    return Files.createTempFile("bote-crawler-image-", "." + extension);
  }

  private void logDownloadHeaders(String imageUrl, HttpHeaders headers) {
    if (!logger.isDebugEnabled() || headers == null) {
      return;
    }
    logger.debug("下载图片请求头: imageUrl={}, referer={}, origin={}",
      imageUrl,
      headers.getFirst("Referer"),
      headers.getFirst("Origin"));
  }

  private void downloadToFile(String imageUrl, HttpHeaders headers, Path targetFile) {
    // 自定义超时时间，默认的超时时间太长了
    OkHttpClient okHttpClient = ModelHttpClient.getClient().newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .readTimeout(Duration.ofSeconds(30))
      .callTimeout(Duration.ofSeconds(30))
      .retryOnConnectionFailure(false)
      .build();
    Request request = new Builder().url(imageUrl).headers(Headers.of(headers.toSingleValueMap())).build();
    Call call = okHttpClient.newCall(request);
    try (Response response = call.execute()) {
      if (response.code() != 200) {
        throw new BssException("下载图片失败，状态码: " + response.code());
      }
      ResponseBody body = response.body();
      if (body == null) {
        throw new BssException("下载图片失败，响应体为空");
      }
      try (InputStream inputStream = body.byteStream()) {
        FileUtils.copyInputStreamToFile(inputStream, targetFile.toFile());
      }
    }
    catch (SocketTimeoutException e) {
      throw new BssException("下载图片超时，请检查网络", e);
    }
    catch (Exception e) {
      throw new BssException("下载图片失败: " + ExpUtil.getMsg(e), e);
    }
  }

  private void logHttpStatusError(String imageUrl, HttpHeaders headers, HttpStatusCodeException e) {
    int statusCode = e.getStatusCode().value();
    String referer = headers != null ? headers.getFirst("Referer") : null;
    String origin = headers != null ? headers.getFirst("Origin") : null;
    logger.warn("下载图片失败: imageUrl={}, status={}, referer={}, origin={}", imageUrl, statusCode, referer, origin);
    if (statusCode == 403) {
      logger.warn("图片下载被拒绝（403），可能是防盗链保护: imageUrl={}, referer={}", imageUrl, referer);
    }
  }

  /**
   * 构造图片访问 URL
   */
  private String buildImageUrl(FileInfoVO fileInfoVO) {
    // TODO 参照 DocBaseConsts.DOCUMENT_ATTACHMENT_GET_PATH 构造链接
    return "file://" + fileInfoVO.getFileId();
  }

  /**
   * 提取图片匹配信息（改进版，能够处理长的 data URI）
   *
   * @param markdownContent Markdown 内容
   * @return 图片匹配信息列表
   */
  private List<ImageMatchDTO> extractImageMatches(String markdownContent) {
    List<ImageMatchDTO> matches = new ArrayList<>();

    // 使用手动解析方式，能够更好地处理长的 data URI
    int index = 0;
    while (index < markdownContent.length()) {
      // 查找图片语法开始标记 `![`
      int imageStart = markdownContent.indexOf("![", index);
      if (imageStart < 0) {
        break;
      }

      // 查找 `](` 标记
      int urlStart = markdownContent.indexOf("](", imageStart);
      if (urlStart < 0) {
        index = imageStart + 2;
        continue;
      }

      // 提取 alt 文本
      String altText = markdownContent.substring(imageStart + 2, urlStart);

      // 从 `](` 之后开始查找 URL
      int urlContentStart = urlStart + 2;

      // 查找 URL 的结束位置（下一个 `)`）
      // 对于 data URI，需要找到真正的结束括号
      String imageUrl = extractImageUrl(markdownContent, urlContentStart);

      if (StringUtils.isNotBlank(imageUrl)) {
        // 计算图片语法的结束位置
        int imageEnd = urlContentStart + imageUrl.length() + 1; // +1 是 `)`
        ImageMatchDTO m = new ImageMatchDTO();
        m.setAltText(altText);
        m.setUrl(imageUrl);
        m.setStart(imageStart);
        m.setEnd(imageEnd);
        matches.add(m);
        // 移动到 URL 结束位置之后
        index = imageEnd;
      }
      else {
        // 如果提取失败，移动到下一个位置继续查找
        index = imageStart + 2;
      }
    }

    return matches;
  }

  /**
   * 提取图片 URL（能够处理长的 data URI）
   *
   * @param content Markdown 内容
   * @param urlStart URL 开始位置（`](` 之后）
   * @return 图片 URL，如果提取失败返回 null
   */
  private String extractImageUrl(String content, int urlStart) {
    if (urlStart >= content.length()) {
      return null;
    }

    if (startsWithDataUri(content, urlStart)) {
      return extractDataUriUrl(content, urlStart);
    }
    return extractNormalUrl(content, urlStart);
  }

  private boolean startsWithDataUri(String content, int urlStart) {
    return content.substring(urlStart).startsWith("data:");
  }

  private String extractNormalUrl(String content, int urlStart) {
    int urlEnd = content.indexOf(")", urlStart);
    if (urlEnd > urlStart) {
      return content.substring(urlStart, urlEnd);
    }
    return null;
  }

  private String extractDataUriUrl(String content, int urlStart) {
    int searchEnd = findDataUriSearchEnd(content, urlStart);
    String candidate = findDataUriByBackwardParenScan(content, urlStart, searchEnd);
    if (StringUtils.isNotBlank(candidate)) {
      return candidate;
    }
    return fallbackDataUriSlice(content, urlStart, searchEnd);
  }

  private int findDataUriSearchEnd(String content, int urlStart) {
    int nextImageStart = content.indexOf("![", urlStart);
    return nextImageStart > 0 ? nextImageStart : content.length();
  }

  private String findDataUriByBackwardParenScan(String content, int urlStart, int searchEnd) {
    int end = Math.min(searchEnd - 1, content.length() - 1);
    for (int i = end; i >= urlStart; i--) {
      if (content.charAt(i) != ')') {
        continue;
      }
      String potentialUrl = content.substring(urlStart, i);
      if (isValidDataUriCandidate(potentialUrl)) {
        return potentialUrl;
      }
    }
    return null;
  }

  private boolean isValidDataUriCandidate(String potentialUrl) {
    return StringUtils.isNotBlank(potentialUrl)
      && potentialUrl.startsWith("data:")
      && potentialUrl.length() > 10
      && !potentialUrl.contains("![")
      && !potentialUrl.contains("](");
  }

  private String fallbackDataUriSlice(String content, int urlStart, int searchEnd) {
    if (searchEnd <= urlStart) {
      return null;
    }
    String potentialUrl = content.substring(urlStart, searchEnd);
    if (potentialUrl.startsWith("data:") && potentialUrl.length() > 10) {
      logger.debug("提取 data URI（未找到结束括号）: length={}", potentialUrl.length());
      return potentialUrl;
    }
    return null;
  }


  /**
   * 替换 Markdown 中的图片 URL
   *
   * @param content Markdown 内容
   * @param urlReplacementMap URL 替换映射（原始URL -> 新URL）
   * @return 替换后的 Markdown 内容
   */
  public String replaceImageUrlsInMarkdown(String content, Map<String, String> urlReplacementMap) {
    if (urlReplacementMap == null || urlReplacementMap.isEmpty() || StringUtils.isEmpty(content)) {
      return content;
    }

    // 使用改进的提取方法，能够准确提取完整的 URL（包括长的 data URI）
    List<ImageMatchDTO> matches = extractImageMatches(content);
    if (matches.isEmpty()) {
      return content;
    }

    // 使用提取的匹配信息进行替换
    StringBuilder result = new StringBuilder();
    int lastIndex = 0;

    for (ImageMatchDTO match : matches) {
      String originalUrl = match.getUrl();
      String altText = match.getAltText();
      int imageStart = match.getStart();
      int imageEnd = match.getEnd();

      // 添加匹配之前的内容
      result.append(content, lastIndex, imageStart);

      String newUrl = resolveReplacementUrl(originalUrl, urlReplacementMap);
      appendReplacement(result, content, altText, newUrl, imageStart, imageEnd);

      lastIndex = imageEnd;
    }

    // 添加剩余内容
    if (lastIndex < content.length()) {
      result.append(content.substring(lastIndex));
    }

    return result.toString();
  }

  private String resolveReplacementUrl(String originalUrl, Map<String, String> urlReplacementMap) {
    String newUrl = urlReplacementMap.get(originalUrl);
    if (StringUtils.isNotBlank(newUrl)) {
      return newUrl;
    }
    PrefixMatchDTO pm = findBestPrefixMatch(originalUrl, urlReplacementMap);
    if (pm == null) {
      logger.debug("未找到匹配的 URL 替换: originalUrlLength={}", originalUrl != null ? originalUrl.length() : 0);
      return null;
    }
    logger.debug("使用前缀匹配替换 URL: originalUrlLength={}, matchedKeyLength={}",
      originalUrl != null ? originalUrl.length() : 0,
      pm.getMatchLength());
    return pm.getReplacement();
  }

  private PrefixMatchDTO findBestPrefixMatch(String originalUrl, Map<String, String> urlReplacementMap) {
    if (StringUtils.isBlank(originalUrl) || urlReplacementMap == null || urlReplacementMap.isEmpty()) {
      return null;
    }
    String best = null;
    int bestLen = 0;
    for (Map.Entry<String, String> entry : urlReplacementMap.entrySet()) {
      String key = entry.getKey();
      if (StringUtils.isBlank(key)) {
        continue;
      }
      if (originalUrl.startsWith(key) || key.startsWith(originalUrl)) {
        int matchLength = Math.min(originalUrl.length(), key.length());
        if (matchLength > bestLen) {
          best = entry.getValue();
          bestLen = matchLength;
        }
      }
    }
    if (best == null) {
      return null;
    }
    PrefixMatchDTO dto = new PrefixMatchDTO();
    dto.setReplacement(best);
    dto.setMatchLength(bestLen);
    return dto;
  }

  private void appendReplacement(StringBuilder out,
    String content,
    String altText,
    String newUrl,
    int imageStart,
    int imageEnd) {
    if (StringUtils.isNotBlank(newUrl)) {
      out.append("![").append(altText).append("](").append(newUrl).append(")");
      return;
    }
    out.append(content, imageStart, imageEnd);
  }


  /**
   * 构建图片下载请求头（用于绕过防盗链）
   *
   * @param imageUrl 图片 URL
   * @param sourceUrl 原始页面 URL
   * @return 请求头
   */
  private HttpHeaders buildImageDownloadHeaders(String imageUrl, String sourceUrl) {
    HttpHeaders headers = new HttpHeaders();
    applyUserAgent(headers);
    applyRefererAndOrigin(headers, imageUrl, sourceUrl);
    applyAcceptHeaders(headers);
    applyFetchHeaders(headers, computeSecFetchSite(imageUrl, sourceUrl));
    return headers;
  }

  private void applyUserAgent(HttpHeaders headers) {
    headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
  }

  private void applyRefererAndOrigin(HttpHeaders headers, String imageUrl, String sourceUrl) {
    String referer = determineReferer(imageUrl, sourceUrl);
    if (StringUtils.isBlank(referer)) {
      return;
    }
    headers.set("Referer", referer);
    String origin = computeOrigin(referer);
    if (StringUtils.isNotBlank(origin)) {
      headers.set("Origin", origin);
    }
  }

  private String computeOrigin(String referer) {
    try {
      java.net.URI refererUri = new java.net.URI(referer);
      String origin = refererUri.getScheme() + "://" + refererUri.getHost();
      if (refererUri.getPort() != -1) {
        origin += ":" + refererUri.getPort();
      }
      return origin;
    }
    catch (Exception e) {
      logger.debug("解析 Origin 失败: referer={}", referer, e);
      return null;
    }
  }

  private void applyAcceptHeaders(HttpHeaders headers) {
    headers.set("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
    headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
    headers.set("Accept-Encoding", "gzip, deflate, br");
  }

  private void applyFetchHeaders(HttpHeaders headers, String secFetchSite) {
    headers.set("Sec-Fetch-Dest", "image");
    headers.set("Sec-Fetch-Mode", "no-cors");
    headers.set("Sec-Fetch-Site", StringUtils.defaultIfBlank(secFetchSite, "cross-site"));
    headers.set("Cache-Control", "no-cache");
  }

  private String computeSecFetchSite(String imageUrl, String sourceUrl) {
    try {
      if (StringUtils.isBlank(imageUrl) || StringUtils.isBlank(sourceUrl)) {
        return "cross-site";
      }
      java.net.URI imageUri = new java.net.URI(imageUrl);
      java.net.URI sourceUri = new java.net.URI(sourceUrl);
      String imageHost = imageUri.getHost();
      String sourceHost = sourceUri.getHost();
      if (imageHost == null || sourceHost == null) {
        return "cross-site";
      }
      if (imageHost.equals(sourceHost)) {
        return "same-origin";
      }
      if (isRelatedDomain(imageHost, sourceHost)) {
        return "same-site";
      }
      return "cross-site";
    }
    catch (Exception e) {
      logger.debug("判断域名关系失败: imageUrl={}, sourceUrl={}", imageUrl, sourceUrl, e);
      return "cross-site";
    }
  }

  /**
   * 判断两个域名是否是相关域（如 toutiaoimg.com 和 toutiao.com）
   *
   * @param domain1 域名1
   * @param domain2 域名2
   * @return 是否是相关域
   */
  private boolean isRelatedDomain(String domain1, String domain2) {
    if (StringUtils.isBlank(domain1) || StringUtils.isBlank(domain2)) {
      return false;
    }

    // 提取根域名
    String root1 = extractRootDomain(domain1);
    String root2 = extractRootDomain(domain2);

    // 检查根域名是否相同或相关
    if (root1.equals(root2)) {
      return true;
    }

    // 检查是否是头条相关域名
    if ((domain1.contains("toutiao") && domain2.contains("toutiao")) ||
      (domain1.contains("bytedance") && domain2.contains("bytedance")) ||
      (domain1.contains("douyin") && domain2.contains("douyin"))) {
      return true;
    }

    return false;
  }

  /**
   * 确定 Referer 值
   *
   * @param imageUrl 图片 URL
   * @param sourceUrl 原始页面 URL
   * @return Referer 值
   */
  private String determineReferer(String imageUrl, String sourceUrl) {
    String fromSource = refererFromSourceUrl(sourceUrl);
    if (StringUtils.isNotBlank(fromSource)) {
      return fromSource;
    }
    String imageHost = safeGetHost(imageUrl);
    if (StringUtils.isBlank(imageHost)) {
      return null;
    }
    String known = inferKnownReferer(imageHost);
    if (StringUtils.isNotBlank(known)) {
      return known;
    }
    String rootDomain = extractRootDomain(imageHost);
    return StringUtils.isNotBlank(rootDomain) ? "https://" + rootDomain + "/" : null;
  }

  private String refererFromSourceUrl(String sourceUrl) {
    if (StringUtils.isBlank(sourceUrl)) {
      return null;
    }
    try {
      java.net.URI uri = new java.net.URI(sourceUrl);
      String scheme = uri.getScheme();
      String host = uri.getHost();
      if (StringUtils.isNotBlank(scheme) && StringUtils.isNotBlank(host)) {
        return sourceUrl;
      }
    }
    catch (Exception e) {
      logger.debug("解析原始页面 URL 失败: {}", sourceUrl, e);
    }
    return null;
  }

  private String safeGetHost(String url) {
    try {
      java.net.URI uri = new java.net.URI(url);
      return uri.getHost();
    }
    catch (Exception e) {
      logger.debug("解析图片 URL 失败: {}", url, e);
      return null;
    }
  }

  private String inferKnownReferer(String imageHost) {
    if (StringUtils.isBlank(imageHost)) {
      return null;
    }
    String h = imageHost.toLowerCase();
    if (isDouyinHost(h)) {
      return "https://www.douyin.com/";
    }
    if (isToutiaoHost(h)) {
      return "https://www.toutiao.com/";
    }
    if (h.contains("sinaimg.cn")) {
      return "https://www.sina.com.cn/";
    }
    if (h.contains("qq.com")) {
      return "https://www.qq.com/";
    }
    if (h.contains("163.com")) {
      return "https://www.163.com/";
    }
    if (h.contains("chinanews.com")) {
      return "https://www.chinanews.com.cn/";
    }
    return null;
  }

  private boolean isDouyinHost(String host) {
    return host.contains("douyinpic.com")
      || host.contains("douyin.com")
      || host.contains("pstatp.com")
      || host.contains("bytedance.com");
  }

  private boolean isToutiaoHost(String host) {
    return host.contains("toutiaoimg.com") || host.contains("toutiao.com");
  }

  /**
   * 提取根域名
   *
   * @param host 主机名
   * @return 根域名
   */
  private String extractRootDomain(String host) {
    if (StringUtils.isBlank(host)) {
      return null;
    }

    // 移除 www. 前缀
    if (host.startsWith("www.")) {
      host = host.substring(4);
    }

    // 对于常见的二级域名，提取主域名
    // 例如：i2.chinanews.com.cn -> chinanews.com.cn
    // p9-sign.toutiaoimg.com -> toutiaoimg.com
    String[] parts = host.split("\\.");
    if (parts.length >= 2) {
      // 取最后两个部分作为根域名
      return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }

    return host;
  }

  /**
   * 从 URL 中提取图片扩展名
   *
   * @param imageUrl 图片 URL
   * @return 扩展名（默认返回 png）
   */
  private String getImageExtension(String imageUrl) {
    try {
      String path = new java.net.URI(imageUrl).getPath();
      if (StringUtils.isNotBlank(path)) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0 && lastDot < path.length() - 1) {
          String ext = path.substring(lastDot + 1).toLowerCase();
          // 验证是否为有效的图片扩展名
          String[] validExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"};
          for (String validExt : validExtensions) {
            if (validExt.equals(ext)) {
              return ext;
            }
          }
        }
      }
    }
    catch (Exception e) {
      logger.error("提取图片扩展名失败: imageUrl={}, error={}", imageUrl, e.getMessage(), e);
    }
    return "png";
  }
}
