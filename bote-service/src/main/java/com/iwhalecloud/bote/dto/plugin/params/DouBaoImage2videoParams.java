package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图生视频参数
 *
 * @author fan.cong
 * @since 2025-08-06
 */
@Getter
@Setter
@ToString
public class DouBaoImage2videoParams extends AbstractPluginParams {

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 时长
     */
    private String duration;

    /**
     * 提示语
     */
    private String prompt;

    public DouBaoImage2videoParams() {
        super(PluginConsts.PLUGIN_CODE_DOUBAO_IMAGE_2_VIDEO);
    }
}
