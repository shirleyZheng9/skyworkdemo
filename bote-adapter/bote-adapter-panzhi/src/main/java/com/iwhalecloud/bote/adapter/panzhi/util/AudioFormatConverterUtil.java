package com.iwhalecloud.bote.adapter.panzhi.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;

/**
 * 音频格式转换工具类 支持WAV格式转换为RAW格式
 *
 * @author qian.sisheng
 * @since 2025-01-14
 */
public final class AudioFormatConverterUtil {
  private static final Logger logger = LoggerFactory.getLogger(AudioFormatConverterUtil.class);

  /** 支持的音频格式 */
  private static final List<String> SUPPORTED_FORMATS = Arrays.asList("mp3", "mp4", "wmv", "wav", "avi");
  /** 默认采样率 */
  private static final int DEFAULT_SAMPLE_RATE = 16000;
  /** 默认声道数（单声道） */
  private static final int DEFAULT_CHANNELS = 1;

  private AudioFormatConverterUtil() {

  }

  /**
   * 将音频文件转换为RAW格式字节数组
   * 使用默认参数：16kHz采样率，单声道
   *
   * @param audioFile 音频文件
   * @return RAW格式字节数组
   * @throws Exception 转换异常
   */
  public static byte[] convertToRawBytes(File audioFile) throws Exception {
    Assert.notNull(audioFile, "音频文件不能为空");
    Assert.isTrue(audioFile.exists(), "音频文件不存在: " + audioFile.getPath());
    String fileName = audioFile.getName();
    String fileExtension = StringUtils.substringAfterLast(fileName, ".");
    return convertToRawBytes(audioFile, fileExtension);
  }

  /**
   * 将音频文件转换为RAW格式字节数组
   *
   * @param audioFile 音频文件
   * @param fileExtension 文件扩展名（如"mp3", "wav"等）
   * @return RAW格式字节数组
   * @throws Exception 转换异常
   */
  private static byte[] convertToRawBytes(File audioFile, String fileExtension) throws Exception {
    // 参数验证
    Assert.notNull(audioFile, "音频文件不能为空");
    Assert.hasText(fileExtension, "文件扩展名不能为空");

    // 检查文件格式
    if (!isSupportedFormat(fileExtension)) {
      throw new IllegalArgumentException("不支持的文件格式: " + fileExtension);
    }

    logger.debug("Starting audio file conversion, format: {}, sample rate: {}Hz, channels: {}", fileExtension, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNELS);

    File tempWavFile = null;

    try {
      // 统一转换成wav格式
      tempWavFile = File.createTempFile("panzhi_audio_convert_", ".wav");
      convertToWav(audioFile, tempWavFile);
      // 从WAV文件提取RAW字节数组
      byte[] rawBytes = extractRawBytesFromWav(tempWavFile);
      logger.debug("Audio file conversion completed, RAW data size: {} bytes", rawBytes.length);
      return rawBytes;
    }
    finally {
      // 清理临时文件
      if (tempWavFile != null && tempWavFile.exists()) {
        FileUtils.delete(tempWavFile);
      }
    }
  }

  /**
   * 转换为WAV格式
   */
  private static void convertToWav(File inputFile, File outputWavFile) throws EncoderException {
    // 配置音频属性
    AudioAttributes audioAttributes = new AudioAttributes();
    audioAttributes.setCodec("pcm_s16le");
    audioAttributes.setSamplingRate(DEFAULT_SAMPLE_RATE);
    audioAttributes.setChannels(DEFAULT_CHANNELS);
    audioAttributes.setBitRate(DEFAULT_SAMPLE_RATE * DEFAULT_CHANNELS * 16);
    // 配置编码属性
    EncodingAttributes encodingAttributes = new EncodingAttributes();
    encodingAttributes.setOutputFormat("wav");
    encodingAttributes.setAudioAttributes(audioAttributes);
    // 执行转换
    Encoder encoder = new Encoder();
    MultimediaObject source = new MultimediaObject(inputFile);
    encoder.encode(source, outputWavFile, encodingAttributes);
  }

  /**
   * 从WAV文件提取RAW字节数组
   */
  private static byte[] extractRawBytesFromWav(File wavFile) throws IOException {
    byte[] allBytes = FileUtils.readFileToByteArray(wavFile);
    // 验证WAV文件头
    if (allBytes.length < 44 || !isValidWavHeader(Arrays.copyOf(allBytes, 44))) {
      throw new IOException("不是有效的WAV文件");
    }
    // 返回跳过44字节头部后的数据
    return Arrays.copyOfRange(allBytes, 44, allBytes.length);
  }

  /**
   * 验证WAV文件头
   */
  private static boolean isValidWavHeader(byte[] header) {
    // 检查RIFF标识
    return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' &&
      header[8] == 'W' && header[9] == 'A' && header[10] == 'V' && header[11] == 'E';
  }

  /**
   * 检查文件格式是否支持
   */
  public static boolean isSupportedFormat(String fileExtension) {
    return StringUtils.isNotBlank(fileExtension) &&
           SUPPORTED_FORMATS.contains(StringUtils.lowerCase(fileExtension));
  }

}
