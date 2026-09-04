package com.iwhalecloud.bote.controller.cutover;

import com.google.common.collect.Lists;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentContributorEntity;
import com.iwhalecloud.bote.dto.convert.BtDcDocumentDTO;
import com.iwhalecloud.bote.mapper.cutover.CutOverMapper;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 迁移文档贡献者
 *
 * @author qian.sisheng
 * @since 2026/03/07
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cutOver/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Hidden
@IgnoreSign
public class MigrateDocumentContributorController {
  private static final Logger logger = LoggerFactory.getLogger(MigrateDocumentContributorController.class);
  private final CutOverMapper cutOverMapper;

  @GetMapping(path = "migrateDocumentContributor", produces = MediaType.TEXT_PLAIN_VALUE)
  public void migrateDocumentContributor(HttpServletResponse response) throws IOException {
    response.setContentType("text/plain;charset=UTF-8");
    PrintWriter writer = response.getWriter(); //NOPMD - suppressed CloseResource - HTTP 输出流不需要手动关闭
    try {
      // 1. 查询所有迁移的文档 ID
      List<String> allDocumentIds = cutOverMapper.selectDocumentIds();
      long total = allDocumentIds.size();

      if (total == 0) {
        addLog(writer, "没有需要处理的文档数据\n");
        return;
      }
      addLog(writer, "开始迁移文档贡献者数据，共 %d 个文档\n", total);

      int batchSize = 500;
      long migratedCount = 0;

      // 2. 分批处理
      for (List<String> batchIds : Lists.partition(allDocumentIds, batchSize)) {
        // 查询完整文档信息
        List<BtDcDocumentDTO> docs = cutOverMapper.selectDocumentByIds(batchIds);

        // 构建实体
        List<DocumentContributorEntity> contributors = CollectionUtils.emptyIfNull(docs).stream()
          .map(this::createDocumentContributorEntity).collect(Collectors.toList());

        // 批量插入
        if (CollectionUtils.isNotEmpty(contributors)) {
          TransactionUtil.executeNew(() -> cutOverMapper.batchInsertDocumentContributor(contributors));
          migratedCount += contributors.size();
        }

        addLog(writer, "已迁移 %d / %d\n", migratedCount, total);
      }

      addLog(writer, "迁移完成！实际迁移 %d 条记录\n", migratedCount);
    }
    catch (Exception e) {
      logger.error("迁移失败", e);
      addLog(writer, "迁移失败: %s\n", e.getMessage());
    }
  }

  /**
   * 创建文档贡献者实体
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private DocumentContributorEntity createDocumentContributorEntity(BtDcDocumentDTO btDcDocument) {
    DocumentContributorEntity documentContributorEntity = new DocumentContributorEntity();
    documentContributorEntity.setId(IDUtils.nextId());
    documentContributorEntity.setDocumentId(btDcDocument.getDocumentId());
    documentContributorEntity.setUserId(btDcDocument.getCreatorId());
    documentContributorEntity.setTenantId(btDcDocument.getTenantId());
    documentContributorEntity.setContributorScore(BigDecimal.ZERO);
    documentContributorEntity.setCreatorId(btDcDocument.getCreatorId());
    documentContributorEntity.setUpdatorId(btDcDocument.getCreatorId());
    documentContributorEntity.setStatusCd(BaseConsts.STATUS_CD_VALID);
    documentContributorEntity.setCreatedTime(new Date());
    documentContributorEntity.setUpdatedTime(new Date());
    return documentContributorEntity;
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
