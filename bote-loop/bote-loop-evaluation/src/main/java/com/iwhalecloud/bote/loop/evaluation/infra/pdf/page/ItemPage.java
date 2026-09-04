package com.iwhalecloud.bote.loop.evaluation.infra.pdf.page;

import com.iwhalecloud.bote.loop.evaluation.infra.pdf.OpenPdfContext;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ItemInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemPage extends AbstractPage {
  private static final int MAX_SCORE_COL = 5;
  private List<ItemInfo> itemInfos;
  private List<String> evaluatorKeys;
  private TableSplitProps tableSplitProps;

  public ItemPage(Document document, OpenPdfContext openPdfContext) {
    super(document, openPdfContext);
  }

  public ItemPage() {
  }

  @Override
  public void render() {
    newPage();
    addTitle();

    if (!validateData()) {
      addEmptyDataParagraph();
      return;
    }

    initTableSplitInfo();
    renderAllTables();
  }

  private void addTitle() {
    Paragraph title = new Paragraph("数据明细", openPdfContext.getFontTitle());
    title.setSpacingBefore(5f);
    title.setSpacingAfter(5f);
    addElement(title);
  }


  private boolean validateData() {
    itemInfos = openPdfContext.getOpenPdfData().getItemInfos();
    if (CollectionUtils.isEmpty(itemInfos) || itemInfos.getFirst() == null) {
      return false;
    }

    Map<String, Double> evaluatorScores = itemInfos.getFirst().getEvaluatorScores();
    evaluatorScores = Optional.ofNullable(evaluatorScores).orElse(Collections.emptyMap());
    evaluatorKeys = new ArrayList<>(evaluatorScores.keySet());
    return true;
  }

  private void initTableSplitInfo() {
    int totalCol = evaluatorKeys.size();
    tableSplitProps = new TableSplitProps(MAX_SCORE_COL, totalCol);
  }

  private void renderAllTables() {
    for (int tableIndex = 0; tableIndex < tableSplitProps.getTableCount(); tableIndex++) {
      PdfPTable table = createTable(tableIndex);
      addTableHeader(table, tableIndex);
      addTableRows(table, tableIndex);
      addElement(table);
    }
  }

  private PdfPTable createTable(int tableIndex) {
    int currentScoreCol = tableSplitProps.getCurrentTableScoreCol(tableIndex);
    PdfPTable table = new PdfPTable(currentScoreCol + 2);

    table.setWidthPercentage(100);
    table.setSpacingBefore(5f);
    table.setHeaderRows(1);
    return table;
  }

  private void addTableHeader(PdfPTable table, int tableIndex) {
    int currentScoreCol = tableSplitProps.getCurrentTableScoreCol(tableIndex);
    int start = tableIndex * MAX_SCORE_COL;

    table.addCell(warpCell(createCell(new Phrase("ID", openPdfContext.getFontHeading()))));

    for (int i = 0; i < currentScoreCol; i++) {
      int idx = start + i;
      String name = evaluatorKeys.get(idx);
      table.addCell(warpCell(createCell(new Phrase(name, openPdfContext.getFontHeading()))));
    }

    table.addCell(warpCell(createCell(new Phrase("耗时", openPdfContext.getFontHeading()))));
  }

  private void addTableRows(PdfPTable table, int tableIndex) {
    int currentScoreCol = tableSplitProps.getCurrentTableScoreCol(tableIndex);
    int start = tableIndex * MAX_SCORE_COL;

    for (ItemInfo itemInfo : itemInfos) {
      addItemIdCell(table, itemInfo);
      addScoreCells(table, itemInfo, currentScoreCol, start);
      addCostCell(table, itemInfo);
    }
  }

  private void addItemIdCell(PdfPTable table, ItemInfo itemInfo) {
    table.addCell(createCell(new Phrase(String.valueOf(itemInfo.getItemId()), openPdfContext.getFontNormal())));
  }

  private void addScoreCells(PdfPTable table, ItemInfo itemInfo, int currentScoreCol, int start) {
    Map<String, Double> evaluatorScores = itemInfo.getEvaluatorScores();
    evaluatorScores = Optional.ofNullable(evaluatorScores).orElse(Collections.emptyMap());
    for (int i = 0; i < currentScoreCol; i++) {
      int idx = start + i;
      String key = evaluatorKeys.get(idx);
      Double score = evaluatorScores.get(key);
      String text = score == null ? "-" : score.toString();
      table.addCell(createCell(new Phrase(text, openPdfContext.getFontNormal())));
    }
  }

  private void addCostCell(PdfPTable table, ItemInfo itemInfo) {
    table.addCell(createCell(new Phrase(itemInfo.getTotalCost(), openPdfContext.getFontNormal())));
  }

  private PdfPCell createCell(Phrase phrase) {
    PdfPCell cell = new PdfPCell(phrase);
    setCellStyle(cell);
    return cell;
  }

  @Getter
  @Setter
  private static class TableSplitProps {
    private final int maxScoreCol;
    private final int totalScoreCol;
    private final int tableCount;

    public TableSplitProps(int maxScoreCol, int totalScoreCol) {
      this.maxScoreCol = maxScoreCol;
      this.totalScoreCol = totalScoreCol;
      this.tableCount = (totalScoreCol + maxScoreCol - 1) / maxScoreCol;
    }

    public int getCurrentTableScoreCol(int tableIndex) {
      int start = tableIndex * maxScoreCol;
      int end = Math.min(start + maxScoreCol, totalScoreCol);
      return end - start;
    }
  }
}
