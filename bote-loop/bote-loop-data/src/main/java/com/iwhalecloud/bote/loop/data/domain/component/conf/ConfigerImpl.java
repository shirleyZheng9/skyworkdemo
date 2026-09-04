package com.iwhalecloud.bote.loop.data.domain.component.conf;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConfigerImpl implements IConfig {

  private final DatasetFeatureProperties datasetFeatureProperties;
  private final DatasetItemStorageProperties datasetItemStorageProperties;
  private final DatasetSpecProperties datasetSpecProperties;
  private final ProducerConfigProperties producerConfigProperties;
  private final SnapshotRetryProperties snapshotRetryProperties;
  private final ConsumerConfigProperties consumerConfigProperties;
  private final TagSpecConfProperties tagSpecConfProperties;

  @Override
  public DatasetFeatureProperties getDatasetFeature() {
    return datasetFeatureProperties;
  }

  @Override
  public DatasetItemStorageProperties getDatasetItemStorage() {
    return datasetItemStorageProperties;
  }

  @Override
  public DatasetSpecProperties getDatasetSpec() {
    return datasetSpecProperties;
  }

  @Override
  public ProducerConfigProperties getProducerConfig() {
    return producerConfigProperties;
  }

  @Override
  public SnapshotRetryProperties getSnapshotRetry() {
    return snapshotRetryProperties;
  }

  @Override
  public ConsumerConfigProperties getConsumerConfigs() {
    return consumerConfigProperties;
  }

  @Override
  public TagSpecConfProperties getTagSpec() {
    return tagSpecConfProperties;
  }
}
