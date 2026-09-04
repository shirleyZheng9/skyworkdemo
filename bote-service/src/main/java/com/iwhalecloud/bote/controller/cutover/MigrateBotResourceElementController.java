package com.iwhalecloud.bote.controller.cutover;

import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.bot.BotUserExperienceDTO;
import com.iwhalecloud.bote.mapper.base.ResourceElementMapper;
import com.iwhalecloud.bote.mapper.cutover.CutOverMapper;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 迁移 BOT 遗留的血缘关系
 * <p>常见问题、常见指令</p>
 *
 * @author chen.linfa
 * @since 2025-09-01
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cutOver/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Hidden
@IgnoreSign
public class MigrateBotResourceElementController {
  private final CutOverMapper cutOverMapper;

  private final ResourceElementMapper resourceElementMapper;

  @GetMapping(path = "migrateBotResourceElement", produces = MediaType.TEXT_PLAIN_VALUE)
  public void migrateBotSceneRel(HttpServletResponse response) throws IOException {
    response.setContentType("text/plain;charset=UTF-8");
    try (PrintWriter writer = response.getWriter()) {
      long startTime = System.currentTimeMillis();
      addLog(writer, "开始迁移 BOT 遗留的血缘关系\n");
      List<BotUserExperienceDTO> list = cutOverMapper.selectBotUserExperience();
      long total = list.size();
      if (total == 0) {
        addLog(writer, "没有需要处理的 BOT\n");
        return;
      }
      addLog(writer, "总数: %d\n", total);
      List<ResourceElementDTO> elements = new ArrayList<>(list.size());
      for (BotUserExperienceDTO dto : list) {
        ResourceElementDTO element = new ResourceElementDTO();
        element.setResourceElementId(Sequences.RESOURCE_ELEMENT_ID.next());
        element.setTenantId(dto.getTenantId());
        element.setResourceId(dto.getBotId());
        element.setResourceType(DataSyncCodeEnum.BOT.getCode());
        element.setElementId(dto.getExperienceId());
        element.setElementType(DataSyncCodeEnum.USER_EXPERIENCE.getCode());
        element.setStatusCd(BaseConsts.STATUS_CD_VALID);
        element.setCreatorId(1L);
        element.setUpdatorId(1L);
        elements.add(element);
      }
      TransactionUtil.executeNew(() -> {
        resourceElementMapper.batchInsertResourceElement(elements);
      });
      addLog(writer, "迁移完成。耗时: %sms, 总数: %s\n", System.currentTimeMillis() - startTime, total);
    }
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
