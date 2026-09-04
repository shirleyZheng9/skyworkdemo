package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * ClickHouse数据库配置
 */
public class CKDBConfig {

  private String exptTurnResultFilterDbName;
  private String datasetItemsSnapshotDbName;

  public CKDBConfig() {
  }

  public CKDBConfig(String exptTurnResultFilterDbName, String datasetItemsSnapshotDbName) {
    this.exptTurnResultFilterDbName = exptTurnResultFilterDbName;
    this.datasetItemsSnapshotDbName = datasetItemsSnapshotDbName;
  }

  // Getters and Setters
  public String getExptTurnResultFilterDbName() {
    return exptTurnResultFilterDbName;
  }

  public void setExptTurnResultFilterDbName(String exptTurnResultFilterDbName) {
    this.exptTurnResultFilterDbName = exptTurnResultFilterDbName;
  }

  public String getDatasetItemsSnapshotDbName() {
    return datasetItemsSnapshotDbName;
  }

  public void setDatasetItemsSnapshotDbName(String datasetItemsSnapshotDbName) {
    this.datasetItemsSnapshotDbName = datasetItemsSnapshotDbName;
  }
}
