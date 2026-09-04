package com.iwhalecloud.bote.service.chat.helper;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.handler.context.RowWriteHandlerContext;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.iwhalecloud.bote.dto.chat.export.SessionMessageDTO;
import com.iwhalecloud.bote.dto.chat.export.SessionSceneDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatMessageQueryParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseType;
import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 会话消息导出辅助类
 *
 * @author qian.sisheng
 * @since 2025-11-12
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class SessionMessageExportHelper {

  private final Logger logger = LoggerFactory.getLogger(SessionMessageExportHelper.class);
  private final JdbcTemplate jdbcTemplate;

  /** 批量写入大小 */
  private static final int BATCH_SIZE = 2000;
  /** 数据库游标抓取大小 */
  private static final int FETCH_SIZE = 2000;
  /** 默认点踩状态 */
  private static final String DEFAULT_LIKE_TYPE = "普通";
  /** 点赞状态 */
  private static final String LIKE_TYPE_LIKE = "赞";
  /** 点踩状态 */
  private static final String LIKE_TYPE_DISLIKE = "踩";

  /**
   * 导出会话列表
   */
  public void exportSessionList(ExcelWriter excelWriter, ChatMessageQueryParams params, List<Object> queryParams) {
    WriteSheet sceneSheet = EasyExcel.writerSheet("对话列表").head(SessionSceneDTO.class)
      .registerWriteHandler(new StyleWriteHandler(buildSessionSheetWidths(), false)).build();
    logger.info("开始导出会话列表数据, tenantId: {}, botId: {}, sceneId: {}", params.getTenantId(), params.getBotId(), params.getSceneId());
    List<SessionSceneDTO> sceneBuffer = new ArrayList<>(BATCH_SIZE);
    long startTime = System.currentTimeMillis();
    AtomicLong count = new AtomicLong();
    // 构建会话列表查询SQL
    String sql = buildSessionExportSql(params);
    jdbcTemplate.query(conn -> createPreparedStatement(conn, queryParams, sql), rs -> {
      sceneBuffer.add(buildSession(rs));
      count.getAndIncrement();
      if (sceneBuffer.size() >= BATCH_SIZE) {
        excelWriter.write(sceneBuffer, sceneSheet);
        sceneBuffer.clear();
      }
    });
    if (CollectionUtils.isNotEmpty(sceneBuffer)) {
      excelWriter.write(sceneBuffer, sceneSheet);
      sceneBuffer.clear();
    }
    else if (count.get() == 0) {
      // 即使没有数据，确保下载生成表头
      excelWriter.write(sceneBuffer, sceneSheet);
    }
    logger.info("完成会话列表数据导出, 总计 {} 条, 耗时 {} ms", count, System.currentTimeMillis() - startTime);
  }

  /**
   * 导出会话详情列表
   */
  public void exportSessionDetailList(ExcelWriter excelWriter, ChatMessageQueryParams params, List<Object> queryParams) {
    SessionMergeHandler mergeHandler = new SessionMergeHandler();
    WriteSheet messageSheet = EasyExcel.writerSheet("对话详情列表").head(SessionMessageDTO.class)
      .registerWriteHandler(new StyleWriteHandler(buildSessionDetailSheetWidths(), true))
      .registerWriteHandler(mergeHandler)
      .build();
    List<SessionMessageDTO> msgBuffer = new ArrayList<>(BATCH_SIZE);
    logger.info("开始导出会话详情列表数据, tenantId: {}, botId: {}, sceneId: {}", params.getTenantId(), params.getBotId(), params.getSceneId());
    long startTime = System.currentTimeMillis();
    AtomicLong count = new AtomicLong();
    // 构建会话详情查询SQL
    String sql = buildSessionDetailExportSql(params);
    long starttime = System.currentTimeMillis();
    jdbcTemplate.query(conn -> createPreparedStatement(conn, queryParams, sql), rs -> {
      msgBuffer.add(buildSessionDetail(rs));
      count.getAndIncrement();
      if (msgBuffer.size() >= BATCH_SIZE) {
        long time = System.currentTimeMillis();
        excelWriter.write(msgBuffer, messageSheet);
        msgBuffer.clear();
        logger.info("完成写入数据" + (System.currentTimeMillis() - time));
      }
    });
    if (CollectionUtils.isNotEmpty(msgBuffer)) {
      logger.info("完成查询， {}ms ,开始写入数据", System.currentTimeMillis() - starttime);
      excelWriter.write(msgBuffer, messageSheet);
      msgBuffer.clear();
    }
    else if (count.get() == 0) {
      // 即使没有数据，确保下载生成表头
      excelWriter.write(msgBuffer, messageSheet);
    }
    // 合并会话详情
    mergeHandler.finish();
    logger.info("完成会话详情列表数据导出, 总计 {} 条, 耗时 {} ms", count, System.currentTimeMillis() - startTime);
  }

  /**
   * 构建会话列表数据
   */
  private SessionSceneDTO buildSession(ResultSet rs) throws SQLException {
    SessionSceneDTO row = new SessionSceneDTO();
    row.setSessionId(rs.getLong("session_id"));
    row.setSessionTitle(rs.getString("session_title"));
    row.setCreatedTime(rs.getTimestamp("created_time"));
    row.setBotName(rs.getString("bot_name"));
    row.setSceneName(rs.getString("scene_name"));
    row.setCreatorName(rs.getString("creator_name"));
    return row;
  }

  /**
   * 构建会话详情
   */
  private SessionMessageDTO buildSessionDetail(ResultSet rs) throws SQLException {
    SessionMessageDTO row = new SessionMessageDTO();
    row.setSessionId(rs.getLong("session_id"));
    row.setSessionTitle(rs.getString("session_title"));
    row.setMsgId(rs.getLong("msg_id"));
    row.setMsgText(rs.getString("msg_text"));
    row.setLikeType(convertLikeType(rs.getString("like_type")));
    row.setCreatorName(rs.getString("creator_name"));
    row.setCreatedTime(rs.getTimestamp("created_time"));
    row.setSceneName(rs.getString("scene_name"));
    row.setBotName(rs.getString("bot_name"));
    row.setFeedback(rs.getString("feedback_reason"));
    return row;
  }

  /**
   * 预编译的SQL语句
   */
  @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE")
  private PreparedStatement createPreparedStatement(Connection connection, List<Object> parameters, String sql)
    throws SQLException {
    String dataSourceType = DbUtil.getDatabaseType().name();
    PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    // 组装动态参数
    setParameters(preparedStatement, parameters);
    // 设置通用配置项，适配不同数据库类型
    preparedStatement.setFetchDirection(ResultSet.FETCH_FORWARD);
    // 限制查询结果集数量，避免数据量过大导致 OOM
    preparedStatement.setFetchSize(FETCH_SIZE);
    if (DatabaseType.POSTGRESQL.name().equalsIgnoreCase(dataSourceType)) {
      connection.setAutoCommit(false);
    }
    // MYSQL 需要设置fetchSize为Integer.MIN_VALUE，才可以流式
    else if (DatabaseType.MYSQL.name().equalsIgnoreCase(dataSourceType)) {
      preparedStatement.setFetchSize(Integer.MIN_VALUE);
    }
    return preparedStatement;
  }

  /**
   * 设置SQL参数
   */
  public void setParameters(PreparedStatement preparedStatement, List<Object> parameters) {
    if (CollectionUtils.isEmpty(parameters)) {
      return;
    }
    try {
      for (int i = 0; i < parameters.size(); i++) {
        Object parameter = parameters.get(i);
        StatementCreatorUtils.setParameterValue(preparedStatement, i + 1, SqlTypeValue.TYPE_UNKNOWN, parameter);
      }
    }
    catch (Exception e) {
      logger.error("设置SQL查询参数异常， parameters={}, error={}", parameters, e.getMessage());
      throw new BssException("设置SQL查询参数异常", e);
    }
  }

  /**
   * 点踩状态转换
   */
  private String convertLikeType(String likeType) {
    if (StringUtils.isEmpty(likeType)) {
      return DEFAULT_LIKE_TYPE;
    }
    return switch (likeType) {
      case "1" -> LIKE_TYPE_LIKE;
      case "2" -> LIKE_TYPE_DISLIKE;
      default -> DEFAULT_LIKE_TYPE;
    };
  }

  /**
   * 构建会话列表sheet的列宽配置。
   */
  private Map<Integer, Integer> buildSessionSheetWidths() {
    Map<Integer, Integer> widths = new HashMap<>();
    // 会话ID
    widths.put(0, 20 * 256);
    // 会话标题
    widths.put(1, 30 * 256);
    // 智能应用名称
    widths.put(2, 20 * 256);
    // 智能体名称
    widths.put(3, 20 * 256);
    // 创建人
    widths.put(4, 12 * 256);
    // 创建时间
    widths.put(5, 20 * 256);
    return widths;
  }

  /**
   * 构建会话详情sheet的列宽配置。
   */
  private Map<Integer, Integer> buildSessionDetailSheetWidths() {
    Map<Integer, Integer> widths = new HashMap<>();
    // 会话ID
    widths.put(0, 20 * 256);
    // 会话标题
    widths.put(1, 30 * 256);
    // 绘画详情ID
    widths.put(2, 20 * 256);
    // 会话内容
    widths.put(3, 70 * 256);
    // 智能体应用名称
    widths.put(4, 20 * 256);
    // 智能体名称
    widths.put(5, 20 * 256);
    // 点赞状态
    widths.put(6, 12 * 256);
    // 点踩反馈信息
    widths.put(7, 20 * 256);
    // 创建人
    widths.put(8, 12 * 256);
    // 创建时间
    widths.put(9, 20 * 256);
    return widths;
  }

  /**
   * 构查询会话列表的SQL
   */
  private String buildSessionExportSql(ChatMessageQueryParams queryParams) {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT a.session_id, a.session_title, a.created_time, ");
    sb.append("b.bot_name, d.scene_name, e.real_name AS creator_name ");
    sb.append("FROM bt_bot_session a ");
    sb.append("LEFT JOIN bt_bot b ON a.bot_id = b.bot_id ");
    sb.append("LEFT JOIN bt_bot_session_msg c ON a.session_id = c.session_id ");
    sb.append("LEFT JOIN bt_bot_session_msg_text f ON c.msg_id = f.msg_id ");
    sb.append("LEFT JOIN bt_bot_scene d ON c.scene_id = d.scene_id ");
    sb.append("LEFT JOIN bt_user e ON a.creator_id = e.user_id ");
    sb.append("WHERE a.status_cd = '00A' AND a.tenant_id = ? ");
    if (queryParams.getBotId() != null) {
      sb.append("AND a.bot_id = ? ");
    }
    if (queryParams.getSceneId() != null) {
      sb.append("AND c.scene_id = ? ");
    }
    if (queryParams.getMinBeginTime() != null) {
      sb.append("AND a.created_time >= ? ");
    }
    if (queryParams.getMaxBeginTime() != null) {
      sb.append("AND a.created_time <= ? ");
    }
    sb.append("GROUP BY a.session_id, a.session_title, a.created_time, b.bot_name, d.scene_name, e.real_name ");
    sb.append("ORDER BY a.created_time DESC");
    return sb.toString();
  }

  /**
   * 构建查询会话详情列表的SQL
   */
  private String buildSessionDetailExportSql(ChatMessageQueryParams queryParams) {
    StringBuilder sb = new StringBuilder();
    // excel单元格最大长度32767个字符，进行截断
    sb.append("SELECT b.session_id, b.session_title, a.msg_id, SUBSTR(c.msg_text, 1, 32767) AS msg_text, a.like_type, ");
    sb.append("u.real_name AS creator_name, a.created_time, d.scene_name, e.bot_name, a.feedback_reason ");
    sb.append("FROM bt_bot_session_msg a ");
    sb.append("LEFT JOIN bt_bot_session b ON b.session_id = a.session_id ");
    sb.append("LEFT JOIN bt_bot_session_msg_text c ON c.msg_id = a.msg_id ");
    sb.append("LEFT JOIN bt_user u ON u.user_id = a.creator_id ");
    sb.append("LEFT JOIN bt_bot_scene d ON d.scene_id = a.scene_id AND d.tenant_id = b.bot_tenant_id ");
    sb.append("LEFT JOIN bt_bot e ON e.bot_id = b.bot_id AND e.tenant_id = b.bot_tenant_id ");
    sb.append("WHERE b.status_cd = '00A' ");
    sb.append("AND a.msg_type NOT IN ('point', 'pageFunc', 'exception') ");
    sb.append("AND a.ref_msg_id IS NULL AND (a.plan_id IS NULL OR a.msg_type = 'confirmPlan') ");
    sb.append("AND b.tenant_id = ? ");
    if (queryParams.getBotId() != null) {
      sb.append("AND b.bot_id = ? ");
    }
    if (queryParams.getSceneId() != null) {
      sb.append("AND a.scene_id = ? ");
    }
    if (queryParams.getMinBeginTime() != null) {
      sb.append("AND b.created_time >= ? ");
    }
    if (queryParams.getMaxBeginTime() != null) {
      sb.append("AND b.created_time <= ? ");
    }
    if (queryParams.getSessionId() != null) {
      sb.append("AND b.session_id = ? ");
    }
    if (StringUtils.isNotEmpty(queryParams.getLikeType())) {
      sb.append("AND a.like_type = ? ");
    }
    sb.append("ORDER BY b.created_time DESC, a.sort");
    return sb.toString();
  }

  /**
   * 构建查询参数
   */
  public List<Object> buildQueryParams(ChatMessageQueryParams queryParams) {
    List<Object> params = new ArrayList<>();
    params.add(queryParams.getTenantId());
    if (queryParams.getBotId() != null) {
      params.add(queryParams.getBotId());
    }
    if (queryParams.getSceneId() != null) {
      params.add(queryParams.getSceneId());
    }
    if (queryParams.getMinBeginTime() != null) {
      params.add(queryParams.getMinBeginTime());
    }
    if (queryParams.getMaxBeginTime() != null) {
      params.add(queryParams.getMaxBeginTime());
    }
    if (queryParams.getSessionId() != null) {
      params.add(queryParams.getSessionId());
    }
    if (StringUtils.isNotEmpty(queryParams.getLikeType())) {
      params.add(queryParams.getLikeType());
    }
    return params;
  }

  /**
   * 在表头行设置列宽, 会话详情列表会话ID和会话标题居中显示
   */
  private static class StyleWriteHandler implements RowWriteHandler, SheetWriteHandler {
    private final Map<Integer, Integer> colWidths;
    /** 居中对齐的样式 */
    private CellStyle centerStyle;
    /** 行对象 */
    private Sheet sheetRef;
    /** 是否应用居中对齐 */
    private final boolean applyCenterAlignment;

    StyleWriteHandler(Map<Integer, Integer> colWidths, boolean applyCenterAlignment) {
      this.colWidths = colWidths;
      this.applyCenterAlignment = applyCenterAlignment;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
      if (applyCenterAlignment) {
        // 创建居中对齐的样式
        centerStyle = writeWorkbookHolder.getWorkbook().createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
      }
    }

    @Override
    public void afterRowDispose(RowWriteHandlerContext context) {
      Row row = context.getRow();
      if (row == null) {
        return;
      }
      int rowIndex = context.getRowIndex();
      Sheet sheet = context.getWriteSheetHolder().getSheet();
      if (sheetRef == null) {
        sheetRef = sheet;
      }
      // 设置表头列宽
      if (rowIndex == 0) {
        for (Map.Entry<Integer, Integer> e : colWidths.entrySet()) {
          sheet.setColumnWidth(e.getKey(), e.getValue());
        }
      }
      // 为数据行设置居中对齐样式
      else if (applyCenterAlignment && centerStyle != null) {
        // 为第1列和第2列（索引为0和1）设置居中对齐
        if (row.getCell(0) != null) {
          row.getCell(0).setCellStyle(centerStyle);
        }
        if (row.getCell(1) != null) {
          row.getCell(1).setCellStyle(centerStyle);
        }
      }
    }
  }

  /**
   * 按会话动态合并前两列（会话ID、会话标题）。
   * <p>以 SessionMessageDTO.sessionId 为分组依据，组内合并第0列与第1列。</p>
   */
  @SuppressWarnings("PMD.GuardLogStatement")
  private static final class SessionMergeHandler implements RowWriteHandler {
    /** 日志对象 */
    private final Logger logger = LoggerFactory.getLogger(SessionMergeHandler.class);
    /** 当前分组的会话ID */
    private Long currentSessionId;
    /** 当前分组开始的数据行索引（含表头情况下，数据从索引1开始） */
    private int groupStartRowIndex = -1;
    /** 最后一个已写数据行索引 */
    private int lastDataRowIndex = -1;
    /** Sheet 引用 */
    private Sheet sheetRef;
    /** 缓存待合并的区域，最后统一处理 */
    private final List<CellRangeAddress> pendingMerges = new ArrayList<>();

    @Override
    public void afterRowDispose(RowWriteHandlerContext context) {
      Row row = context.getRow();
      if (row == null) {
        return;
      }
      int rowIndex = context.getRowIndex();
      Sheet sheet = context.getWriteSheetHolder().getSheet();
      if (sheetRef == null) {
        sheetRef = sheet;
      }
      // 跳过表头
      if (rowIndex == 0) {
        return;
      }

      Long sessionId = extractSessionId(row.getCell(0));
      lastDataRowIndex = rowIndex;
      // 初始化第一个分组
      if (currentSessionId == null) {
        currentSessionId = sessionId;
        groupStartRowIndex = rowIndex;
        return;
      }
      boolean isNewGroup = sessionId == null || !sessionId.equals(currentSessionId);
      if (isNewGroup) {
        // 缓存待合并的区域而不是立即合并
        cacheMergeRange(groupStartRowIndex, rowIndex - 1);
        // 开启新组
        currentSessionId = sessionId;
        groupStartRowIndex = rowIndex;
      }
    }

    /**
     * 解析单元格中的会话ID。
     *
     * @param cell 单元格对象
     * @return 解析得到的会话ID
     */
    @Nullable
    private Long extractSessionId(Cell cell) {
      if (cell == null) {
        return null;
      }

      try {
        CellType type = cell.getCellType();
        if (type == CellType.NUMERIC) {
          return (long) cell.getNumericCellValue();
        }
        if (type == CellType.STRING) {
          String s = cell.getStringCellValue();
          if (s == null || s.trim().isEmpty()) {
            return null;
          }
          return Long.valueOf(s.trim());
        }
      }
      catch (NumberFormatException e) {
        logger.warn("Failed to parse sessionId as Long: {}", cell);
        return null;
      }
      catch (Exception e) {
        logger.error("Parse sessionId failed: message = {}", e.getMessage(), e);
        return null;
      }
      return null;
    }

    /**
     * 合并补齐：写入结束时补最后一组的合并范围。
     */
    public void finish() {
      // 处理最后一个分组
      if (groupStartRowIndex >= 0 && lastDataRowIndex >= groupStartRowIndex) {
        cacheMergeRange(groupStartRowIndex, lastDataRowIndex);
      }

      // 统一执行所有合并操作
      if (sheetRef != null && !pendingMerges.isEmpty()) {
        for (CellRangeAddress range : pendingMerges) {
          sheetRef.addMergedRegion(range);
        }
      }
    }

    /**
     * 缓存待合并的区域
     *
     * @param startRow 开始行（包含）
     * @param endRow 结束行（包含）
     */
    private void cacheMergeRange(int startRow, int endRow) {
      if (endRow <= startRow) {
        return;
      }
      // 缓存第一列(会话ID)的合并区域
      pendingMerges.add(new CellRangeAddress(startRow, endRow, 0, 0));
      // 缓存第二列(会话标题)的合并区域
      pendingMerges.add(new CellRangeAddress(startRow, endRow, 1, 1));
    }
  }

}
