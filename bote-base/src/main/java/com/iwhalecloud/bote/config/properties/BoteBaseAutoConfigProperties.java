package com.iwhalecloud.bote.config.properties;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 自动配置
 *
 * @author qian.sisheng
 * @since 2025-11-26
 */
@AutoConfiguration
@EnableConfigurationProperties(FilePreviewConfig.class)
public class BoteBaseAutoConfigProperties {
}
