package com.iwhalecloud.bote.controller.cutover;

import com.github.pagehelper.PageHelper;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.TokenUtil;
import com.iwhalecloud.bote.dto.base.AppPublishDTO;
import com.iwhalecloud.bote.mapper.cutover.CutOverMapper;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 应用发布免登录链接数据割接服务
 *
 * @author tingyun.wang
 * @since 2026-02-10
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cutOver/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Hidden
@IgnoreSign
public class MigrateAppPublishUrlController {

  private final CutOverMapper cutOverMapper;

  /**
   * 割接存量免登录链接数据
   */
  @GetMapping(path = "migrateAppPublishUrl", produces = MediaType.TEXT_PLAIN_VALUE)
  public void migrateAppPublishUrl(HttpServletResponse response) throws IOException {
    response.setContentType("text/plain;charset=UTF-8");
    PrintWriter writer = response.getWriter(); //NOPMD - suppressed CloseResource - HTTP 输出流不需要手动关闭
    long startTime = System.currentTimeMillis();
    addLog(writer, "开始割接存量应用发布免登录链接数据\n");

    // 统计需要处理的数据总量
    long totalCount = PageHelper.count(cutOverMapper::selectAppPublishUrls);
    if (totalCount == 0) {
      addLog(writer, "没有需要处理的应用发布免登录链接数据\n");
      return;
    }

    // 获取博特api地址
    String boteApiUrl = SystemParameter.BOTE_API_URL.getValueFromEnv();
    Assert.hasText(boteApiUrl, "博特平台接口地址不能为空，请联系系统管理员");

    // 分页处理数据
    int limit = 100;
    int successCount = 0;
    for (int offset = 0; offset < totalCount; offset += limit) {
      // noinspection resource
      PageHelper.offsetPage(offset, limit, false);
      List<AppPublishDTO> publishList = cutOverMapper.selectAppPublishUrls();
      for (AppPublishDTO publishDTO : publishList) {
        // 执行数据割接并统计成功数量
        if (execConvertAppPublishUrl(publishDTO, boteApiUrl)) {
          successCount++;
        }
      }
    }

    addLog(writer, "存量应用发布免登录链接数据割接完成，耗时: %sms, 总数: %s, 更新: %s\n", System.currentTimeMillis() - startTime, totalCount, successCount);
  }

  /**
   * 执行应用发布免登录链接数据割接
   */
  private boolean execConvertAppPublishUrl(AppPublishDTO publish, String boteApiUrl) {
    // 解析url地址
    UriComponents components = UriComponentsBuilder.fromUriString(publish.getUrl()).build();
    String fragment = components.getFragment();
    if (StringUtils.isNotBlank(fragment)) {
      int queryIndex = fragment.indexOf('?');
      if (queryIndex != -1) {
        // 提取路径地址和查询参数
        String fragmentPath = fragment.substring(0, queryIndex);
        String queryPart = fragment.substring(queryIndex + 1);
        // url路径地址、片段路径地址、查询参数都不能为空
        if (StringUtils.isBlank(components.getPath()) || StringUtils.isBlank(fragmentPath) || StringUtils.isBlank(queryPart)) {
          return false;
        }
        // 解析查询参数
        UriComponents queryUri = UriComponentsBuilder.fromUriString("?" + queryPart).build();
        Map<String, String> queryParams = queryUri.getQueryParams().toSingleValueMap();
        List<String> queryList = new ArrayList<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
          if ("token".equals(entry.getKey())) {
            // 将参数中的 token 加密为 accessToken
            String accessToken = TokenUtil.buildAccessToken(entry.getValue());
            if (accessToken == null) {
              return false;
            }
            queryList.add("accessToken" + "=" + accessToken);
          }
          else {
            // 其它参数保持不变
            queryList.add(entry.getKey() + "=" + entry.getValue());
          }
        }
        // 补充重定向参数
        queryList.add("redirect=" + URLEncoder.encode(components.getPath() + "#" + fragmentPath, StandardCharsets.UTF_8));
        // 构造新的url
        String newUrl = StringUtils.stripEnd(boteApiUrl, "/") + "/bote/single" + "?" + String.join("&", queryList);
        // 更新数据
        TransactionUtil.executeNew(() -> cutOverMapper.updateAppPublishUrl(publish.getPublishId(), newUrl));
        return true;
      }
    }
    return false;
  }

  /**
   * 打印日志到 HTTP 响应
   */
  @SuppressFBWarnings("XSS_SERVLET")
  private void addLog(PrintWriter writer, String msg, Object... args) {
    writer.printf(msg, args);
    writer.flush();
  }

}
