package com.iwhalecloud.bote.loop.evaluation.infra.pdf.handle;

import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ScoreDistributionInfo;
import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(of = "infoMap", callSuper = true)
public class ScoreLabelGenerator extends StandardCategoryItemLabelGenerator {
  @Serial
  private static final long serialVersionUID = 1L;

  private final Map<String, ScoreDistributionInfo> infoMap;

  public ScoreLabelGenerator(List<ScoreDistributionInfo> dataList) {
    this.infoMap = dataList.stream().collect(Collectors.toMap(
        ScoreDistributionInfo::getScore,
        info -> info
      ));
  }

  @Override
  public String generateLabel(CategoryDataset dataset, int row, int column) {
    try {
      String score = (String) dataset.getColumnKey(column);
      ScoreDistributionInfo info = infoMap.get(score);
      double percentage = info.getPercentage();
      long count = info.getCount();
      return String.format("%.2f%%/%d项", percentage, count);
    }
    catch (Exception e) {
      return "";
    }
  }

  @Override
  public boolean canEqual(Object other) {
    return other instanceof ScoreLabelGenerator;
  }
}
