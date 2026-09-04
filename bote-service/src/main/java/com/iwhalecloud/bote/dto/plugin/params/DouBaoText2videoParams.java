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
public class DouBaoText2videoParams extends AbstractPluginParams {

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

    /**
     * 比例
     */
    private String ratio;

    public DouBaoText2videoParams() {
        super(PluginConsts.PLUGIN_CODE_DOUBAO_TEXT_2_VIDEO);
    }
}
