---
name: cron
description: 通过 cron_* 工具管理通用智能体定时任务 - 创建、查询、禁用、启用、删除、立即执行
metadata: { "emoji": "⏰" }
---

# 定时任务管理

使用 `cron_*` 系列工具管理通用智能体定时任务。定时任务可按 cron 表达式在指定时间触发，向指定渠道发送固定文本或调用通用智能体并推送回复。

## 常用工具

- **cron_list**：分页列出当前用户在当前空间下的定时任务。
- **cron_get**：根据任务 ID 查询单个定时任务详情。
- **cron_create**：创建一条定时任务（需提供名称、cron 表达式、渠道与提问内容等）。
- **cron_delete**：根据任务 ID 删除定时任务。
- **cron_disable**：禁用定时任务（暂停调度）。
- **cron_enable**：启用已禁用的定时任务。
- **cron_trigger**：立即执行一次指定定时任务（不改变调度计划）。

## 创建任务

创建任务时需提供：

- **name**：任务名称（必填，**请使用中文**，如「每日早安」「每周汇总」）。
- **cron**：cron 执行频率，6 段格式（秒 分 时 日 月 周），例如 `0 0 9 * * ?` 表示每天 9:00。
- **channelId**：渠道 ID（必填）。
- **channelType**：渠道类型，如 DingTalk（钉钉）、Feishu（飞书）、console 等。
- **taskType**：任务类型，`text` 为固定文本，`agent` 为调用通用智能体。
- **requestInput**：内容。taskType 为 text 时为发送的文本；为 agent 时为向智能体提问的内容。
- **webhook**：渠道回调地址。对接钉钉或飞书时，按以下优先级解析：1）调用时传入的 webhook；2）环境变量（钉钉：`DINGTALK_WEBHOOK`，飞书：`FEISHU_WEBHOOK`，可在智能体「环境变量」或系统环境中配置）；3）若仍无，则引导用户配置对应环境变量或在此处手工输入 Webhook URL 后再次创建。

创建后任务会以当前登录用户为创建者、当前空间为归属空间。

## Cron 表达式示例（6 段：秒 分 时 日 月 周）

```
0 0 9 * * ?       每天 9:00
0 0 0/2 * * ?     每 2 小时
0 30 8 * * ? 1-5  工作日 8:30
0 0/15 * * * ?    每 15 分钟
```

## 使用建议

- **任务名称**：创建定时任务时 name 一律使用中文（如「每日 9 点提醒」「每周一汇报」）。
- **钉钉/飞书**：推送钉钉或飞书时，Webhook 优先从环境变量读取（钉钉：`DINGTALK_WEBHOOK`，飞书：`FEISHU_WEBHOOK`）。若环境变量未配置且用户未传入 webhook，则引导用户二选一：在智能体「环境变量」中配置上述变量，或直接提供 Webhook URL 后再次创建。
- 需要先列出任务时，使用 **cron_list** 查 jobId，再进行 get/delete/disable/enable/trigger。
- 创建前若缺少渠道等信息，先向用户确认 channelId、channelType、requestInput 等再调用 **cron_create**。
- 给用户的说明或命令要完整、可直接理解或复制使用。
