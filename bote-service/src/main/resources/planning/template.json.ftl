<#-- @ftlvariable name="steps" type="java.util.List" -->
{
  "steps": [
    {
      "type": "start",
      "name": "开始",
      "code": "start",
      "next": "scene_1"
    },
<#list steps as step>
    {
      "type": "scene",
      "name": "${step.agentName!}",
      "code": "scene_${step_index + 1}",
      "sceneId": ${step.agentId!},
      "messageContent": "${step.agentRequest!?j_string}",
      "stepId": ${step.stepId!},
    <#if step.getStepParams()?has_content>
      "contextParams": ${step.getStepParams()},
    </#if>
    <#if !step_has_next>
      "next": "end"
    <#else>
      "next": "scene_${step_index + 2}"
    </#if>
    },
</#list>
    {
      "type": "end",
      "name": "结束",
      "code": "end"
    }
  ]
}
