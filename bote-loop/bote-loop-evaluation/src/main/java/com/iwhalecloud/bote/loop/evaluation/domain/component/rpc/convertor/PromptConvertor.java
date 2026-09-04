package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.convertor;

import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.CommitInfoDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MessageDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptBasicDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.RoleDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ToolCallDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.VariableDefDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.VariableValDTO;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.CommitInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.LoopPrompt;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.PromptBasic;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.PromptCommit;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.PromptDetail;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.PromptTemplate;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.VariableDef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FunctionCall;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Message;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ToolCall;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ToolType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.VariableVal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.utils.Lists;

public final class PromptConvertor {
  private PromptConvertor() {
  }

  public static List<LoopPrompt> convertToLoopPrompts(List<PromptDTO> promptDTO) {
    return promptDTO.stream().map(PromptConvertor::convertToLoopPrompt).toList();
  }

  public static LoopPrompt convertToLoopPrompt(PromptDTO promptDTO) {
    LoopPrompt loopPrompt = new LoopPrompt();
    PromptBasic promptBasic = new PromptBasic();
    PromptCommit promptCommit = new PromptCommit();
    PromptDetail promptDetail = new PromptDetail();
    PromptTemplate promptTemplate = new PromptTemplate();
    List<VariableDef> variableDefs = Lists.newArrayList();
    CommitInfo commitInfo = new CommitInfo();

    loopPrompt.setId(promptDTO.getId());
    loopPrompt.setPromptKey(promptDTO.getPromptKey());
    loopPrompt.setPromptBasic(promptBasic);
    loopPrompt.setPromptCommit(promptCommit);
    promptCommit.setDetail(promptDetail);
    promptCommit.setCommitInfo(commitInfo);
    promptDetail.setPromptTemplate(promptTemplate);
    promptTemplate.setVariableDefs(variableDefs);

    PromptBasicDTO promptBasicDTO = promptDTO.getPromptBasic();
    promptBasic.setDisplayName(promptBasicDTO.getDisplayName());
    promptBasic.setDescription(promptBasicDTO.getDescription());
    promptBasic.setLatestVersion(promptBasicDTO.getLatestVersion());
    if (promptDTO.getPromptCommit() != null) {
      List<VariableDefDTO> variableDefDTOs = promptDTO.getPromptCommit().getDetail().getPromptTemplate().getVariableDefs();
      if (variableDefDTOs != null) {
        for (VariableDefDTO variableDefDTO : variableDefDTOs) {
          VariableDef variableDef = new VariableDef();
          variableDef.setKey(variableDefDTO.getKey());
          variableDefs.add(variableDef);
        }
      }
      CommitInfoDTO commitInfoDTO = promptDTO.getPromptCommit().getCommitInfo();
      commitInfo.setVersion(commitInfoDTO.getVersion());
      commitInfo.setBaseVersion(commitInfoDTO.getBaseVersion());
      commitInfo.setDescription(commitInfoDTO.getDescription());
      commitInfo.setCommittedAt(commitInfoDTO.getCommittedAt());
      commitInfo.setCommittedBy(commitInfoDTO.getCommittedBy());
    }
    return loopPrompt;
  }

  public static List<VariableValDTO> convertVariables2Prompt(List<VariableVal> variableVals) {
    return variableVals.stream().map(PromptConvertor::convertVariables2Prompt).toList();
  }

  public static VariableValDTO convertVariables2Prompt(VariableVal variableVal) {
    VariableValDTO variableValDTO = new VariableValDTO();
    variableValDTO.setKey(variableVal.getKey());
    variableValDTO.setValue(variableVal.getValue());
    variableValDTO.setPlaceholderMessages(convertMessages2Prompt(variableVal.getPlaceholderMessages()));
    return variableValDTO;
  }

  public static List<MessageDTO> convertMessages2Prompt(List<Message> messages) {
    if (messages == null) {
      return new ArrayList<>();
    }
    return messages.stream().map(o -> {
      MessageDTO messageDTO = new MessageDTO();
      messageDTO.setRole(RoleDTO.fromValue(o.getRole().getDescription()));
      messageDTO.setContent(o.getContent().getText());
      return messageDTO;
    }).toList();
  }

  public static List<ToolCall> convertPromptToolCalls2Eval(List<ToolCallDTO> toolCallDTOs) {
    if (toolCallDTOs == null) {
      return new ArrayList<>();
    }
    return toolCallDTOs.stream().map(PromptConvertor::convertPromptToolCalls2Eval).toList();
  }

  public static ToolCall convertPromptToolCalls2Eval(ToolCallDTO toolCallDTO) {
    ToolCall toolCall = new ToolCall();
    toolCall.setIndex(toolCall.getIndex());
    toolCall.setId(toolCall.getId());
    toolCall.setType(ToolType.FUNCTION);


    FunctionCall functionCall = new FunctionCall();
    toolCall.setFunctionCall(functionCall);

    functionCall.setName(toolCallDTO.getFunctionCall().getName());
    functionCall.setArguments(toolCallDTO.getFunctionCall().getArguments());

    return toolCall;
  }

}
