package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.VideoToAudioPluginParams;
import com.iwhalecloud.bote.service.plugin.IPluginRemoteService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频转音频插件
 */
@Component
public class VideoToAudioPlugin extends AbstractPlugin<VideoToAudioPluginParams> {

    private final IPluginRemoteService pluginRemoteService;

    public VideoToAudioPlugin(IPluginRemoteService pluginRemoteService) {
        super(VideoToAudioPluginParams.class);
        this.pluginRemoteService = pluginRemoteService;
    }

    @Override
    public String getPluginCode() {
        return PluginConsts.PLUGIN_CODE_VIDEO_TO_AUDIO;
    }

    @Override
    public ParameterSpec createRequestParameter() {
        List<ParameterSpec> children = new ArrayList<>();
        children.add(ParameterSpec.newProperty("fileId", "视频文件ID", AttrDataType.NUMBER));
        children.add(ParameterSpec.newProperty("audioFormat", "音频个格式", AttrDataType.STRING));

        return ParameterSpec.newRoot(children);
    }

    @Override
    public ParameterSpec createResponseParameter() {
        List<ParameterSpec> children = new ArrayList<>();
        children.add(ParameterSpec.newProperty("fileId", "音频文件ID", AttrDataType.STRING));
        return ParameterSpec.newRoot(children);
    }

    @Override
    public void validateParams(VideoToAudioPluginParams params) {
        Assert.notNull(params.getFileId(), "视频文件ID不能为空");
    }

    @Override
    @SuppressFBWarnings("SECSQLISPRJDBC")
    public Object doRun(VideoToAudioPluginParams pluginParams) {
        ResultVO<Object> resultVO = pluginRemoteService.videoToAudio(pluginParams.getFileId(), pluginParams.getAudioFormat());
        if (resultVO.isSuccess()) {
            return resultVO.getResultObject();
        } else {
            throw new BssException("视频转音频插件异常：" + resultVO.getResultMsg());
        }
    }
}
