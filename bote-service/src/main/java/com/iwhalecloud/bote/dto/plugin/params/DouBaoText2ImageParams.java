package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import java.util.List;
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
public class DouBaoText2ImageParams extends AbstractPluginParams {

    /**
     * 文本提示
     */
    private String prompt;

    /**
     * 响应格式
     */
    private String responseFormat;

    /**
     * 图片大小
     */
    private String size;

    /**
     * 文件ID列表
     */
    private List<Long> fileIds;

    /**
     * 最大图片张数
     */
    private Integer maxImages;

    public DouBaoText2ImageParams() {
        super(PluginConsts.PLUGIN_CODE_DOUBAO_TEXT_2_IMAGE);
    }
}
