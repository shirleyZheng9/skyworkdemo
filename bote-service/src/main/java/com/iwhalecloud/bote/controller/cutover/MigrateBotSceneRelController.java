package com.iwhalecloud.bote.controller.cutover;

import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneRelDTO;
import com.iwhalecloud.bote.mapper.base.ResourceElementMapper;
import com.iwhalecloud.bote.mapper.bot.BotRelaManageMapper;
import com.iwhalecloud.bote.mapper.cutover.CutOverMapper;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 迁移 BOT 关联的场景
 *
 * @author chen.linfa
 * @since 2025-04-24
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cutOver/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Hidden
@IgnoreSign
public class MigrateBotSceneRelController {
  private final CutOverMapper cutOverMapper;

  private final BotRelaManageMapper botRelaManageMapper;

  private final ResourceElementMapper resourceElementMapper;

  @GetMapping(path = "migrateBotSceneRel", produces = MediaType.TEXT_PLAIN_VALUE)
  public void migrateBotSceneRel(HttpServletResponse response) throws IOException {
    response.setContentType("text/plain;charset=UTF-8");
    PrintWriter writer = response.getWriter(); //NOPMD - suppressed CloseResource - HTTP 输出流不需要手动关闭
    long startTime = System.currentTimeMillis();
    addLog(writer, "开始迁移 BOT 关联的智能体\n");
    List<BotDTO> bots = cutOverMapper.selectBot();
    long total = bots.size();
    if (total == 0) {
      addLog(writer, "没有需要处理的 BOT\n");
      return;
    }
    addLog(writer, "总数: %d\n", total);
    for (BotDTO bot : bots) {
      List<BotSceneDTO> scenes = cutOverMapper.selectBotScene(bot.getTenantId(), bot.getBotId());
      List<BotSceneRelDTO> rels = new ArrayList<>(scenes.size());
      List<ResourceElementDTO> elements = new ArrayList<>(scenes.size());
      for (BotSceneDTO scene : scenes) {
        BotSceneRelDTO rel = new BotSceneRelDTO();
        rel.setRelId(Sequences.BOT_SCENE_REL_ID.next());
        rel.setTenantId(bot.getTenantId());
        rel.setBotId(bot.getBotId());
        rel.setSceneId(scene.getSceneId());
        rel.setIsDefault(BaseConsts.FALSE);
        rel.setStatusCd(BaseConsts.STATUS_CD_VALID);
        rel.setCreatorId(1L);
        rels.add(rel);

        ResourceElementDTO element = new ResourceElementDTO();
        element.setResourceElementId(Sequences.RESOURCE_ELEMENT_ID.next());
        element.setTenantId(bot.getTenantId());
        element.setResourceId(bot.getBotId());
        element.setResourceType(DataSyncCodeEnum.BOT.getCode());
        element.setElementId(scene.getSceneId());
        element.setElementType(DataSyncCodeEnum.SCENE.getCode());
        element.setStatusCd(BaseConsts.STATUS_CD_VALID);
        element.setCreatorId(1L);
        element.setUpdatorId(1L);
        elements.add(element);
      }
      TransactionUtil.executeNew(() -> {
        botRelaManageMapper.batchInsertBotSceneRel(rels);
        resourceElementMapper.batchInsertResourceElement(elements);
      });
    }
    addLog(writer, "迁移完成。耗时: %sms, 总数: %s\n", System.currentTimeMillis() - startTime, total);
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
