package com.iwhalecloud.bote.loop.data.domain.component.conf;

public interface IConfig {

  DatasetFeatureProperties getDatasetFeature();

  DatasetItemStorageProperties getDatasetItemStorage();

  DatasetSpecProperties getDatasetSpec();

  ProducerConfigProperties getProducerConfig();

  SnapshotRetryProperties getSnapshotRetry();

  ConsumerConfigProperties getConsumerConfigs();

  TagSpecConfProperties getTagSpec();

}
