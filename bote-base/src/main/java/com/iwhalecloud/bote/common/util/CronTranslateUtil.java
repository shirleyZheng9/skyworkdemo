package com.iwhalecloud.bote.common.util;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Cron表达式翻译工具类 - 提供 cron表达式的中文描述转换
 * 基于 cron-utils 开源库实现
 *
 * @author wangtingyun
 * @since 2026-03-23
 */
public final class CronTranslateUtil {

    private static final Logger logger = LoggerFactory.getLogger(CronTranslateUtil.class);

    private static final CronParser QUARTZ_PARSER = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    private static final CronDescriptor DESCRIPTOR = CronDescriptor.instance(Locale.SIMPLIFIED_CHINESE);

    private CronTranslateUtil() {
    }

    /**
     * 将 cron表达式转换为中文描述
     *
     * @param cron cron表达式（6 段格式：秒 分 时 日 月 星期）
     * @return 中文描述
     */
    public static String toChineseDescription(String cron) {
        if (StringUtils.isBlank(cron)) {
            return "";
        }

        try {
            // 解析 cron 表达式
            Cron parsedCron = QUARTZ_PARSER.parse(cron.trim());
            // cron 表达式翻译为中文
            String describe = DESCRIPTOR.describe(parsedCron);
            // 美化中文翻译结果
            describe = describe.replaceFirst("^在", "每天");
            describe = describe.replaceAll("每\\s+(秒|分|小时|天|周|年)", "每$1");
            return describe;
        }
        catch (Exception e) {
            // 解析失败时不抛出异常，避免影响主业务
            logger.error("cron表达式解析失败, cron: {}", cron, e);
            return cron;
        }
    }
}
