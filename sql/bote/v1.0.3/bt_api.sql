UPDATE bote.bt_api
SET api_name='开启对话', api_desc='发起一次对话，支持添加上下文和流式响应', relative_path='/bote/chat/completions', req_method='POST', path_json=NULL, query_json=NULL, body_json='{"key":-1,"description":"根节点","name":"root","type":"object","require":false,"value":"","children":[{"description":"租户ID，可选","name":"tenantId","type":"integer","require":false,"value":"","key":"adcb3439-2fde-4e56-b9d7-9fa27f5deb67","parentKey":-1},{"description":"机器人ID","name":"botId","type":"integer","require":true,"value":"","key":"fc440006-34fa-4713-88bc-645febe57d40","parentKey":-1},{"description":"机器人ID列表","name":"botIds","type":"array","require":false,"value":"","key":"041e3342-4d1a-a4d4-8cf6-b9198456abe3","parentKey":-1},{"description":"客户端 ID，可选","name":"clientId","type":"string","require":false,"value":"","key":"e8c2024a-11a9-81d8-0d42-ba67ad65731b","parentKey":-1},{"description":"会话 ID，可选","name":"sessionId","type":"integer","require":false,"value":"","key":"52309665-aa68-7cd5-bf62-edf0c4d8c706","parentKey":-1},{"description":"当前上下文 ID, 可选","name":"contextId","type":"integer","require":false,"value":"","key":"a50c0627-6db0-b61e-b00d-10aa27af8bcd","parentKey":-1},{"description":"当前场景 ID","name":"sceneId","type":"integer","require":true,"value":"","key":"d6957081-2135-28e5-b883-c5ee90162d75","parentKey":-1},{"description":"当前场景名称","name":"sceneName","type":"string","require":true,"value":"","key":"47bf2ea2-fb38-5b52-cba6-3d7db7332454","parentKey":-1},{"description":"上一个场景 ID, 退出场景时使用","name":"lastSceneId","type":"integer","require":false,"value":"","key":"47bf2ea2-fb38-5b52-cba6-3d7db7332455","parentKey":-1},{"description":"上一个上下文 ID, 退出场景时使用","name":"lastContextId","type":"string","require":false,"value":"","key":"0c74d1c4-7c00-a377-4c14-5c14d55fdd90","parentKey":-1},{"description":"消息","name":"message","type":"object","require":false,"value":"","key":"fea2186e-365f-43eb-9d97-d8a42b755425","parentKey":-1,"children":[{"description":"消息类型","name":"type","type":"string","require":false,"value":"","key":"8578933e-2004-c7a3-dd96-36b26cecc589","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"},{"description":"消息角色","name":"role","type":"string","require":false,"value":"","key":"8d2c9f97-150e-b260-6b9f-01b03322975b","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"},{"description":"消息内容","name":"content","type":"string","require":false,"value":"","key":"5b26ea1c-1410-bb11-91ff-52c31a92c928","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"},{"description":"文件 ID 列表","name":"fileIds","type":"array","require":false,"value":"","key":"77d3b639-5071-5c49-f006-5f63e872b2d8","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"},{"description":"参数","name":"params","type":"object","require":false,"value":"","key":"a4dc204a-b96d-2d9d-cc44-c5854a0dc2ff","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"},{"description":"关联消息 ID，可选","name":"refId","type":"integer","require":false,"value":"","key":"74f27918-d4bf-701c-524e-367640881552","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"},{"description":"工具调用 ID","name":"toolCallId","type":"integer","require":false,"value":"","key":"46b564e8-6965-40bc-d1f9-60c6b5a51579","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"}]},{"description":"上下文参数","name":"contextParams","type":"object","require":false,"value":"","key":"9362c79f-a3b7-d7a9-db0e-2a5617d722e9","parentKey":-1},{"description":"计划参数","name":"planParams","type":"object","require":false,"value":"","key":"9362c79f-a3b7-d7a9-db0e-2a5617d72219","parentKey":-1,"children":[{"description":"标识是否通过应用的规划智策略生成计划","name":"planable","type":"boolean","require":false,"value":"","key":"8578933e-2004-c7a3-dd96-36b26cecc511","parentKey":"9362c79f-a3b7-d7a9-db0e-2a5617d72219"},{"description":"通过应用的规划智策略生成计划，初始化的智能体列表","name":"busiInfos","type":"array","require":false,"value":"","key":"8578933e-2004-c7a3-dd96-36b26cecc512","parentKey":"9362c79f-a3b7-d7a9-db0e-2a5617d72219","children":[{"description":"智能体ID","name":"agentId","type":"integer","require":false,"value":"","key":"8578933e-2004-c7a3-dd96-36b26cecc531","parentKey":"8578933e-2004-c7a3-dd96-36b26cecc512"},{"description":"智能体名称","name":"agentName","type":"string","require":false,"value":"","key":"8578933e-2004-c7a3-dd96-36b26cecc532","parentKey":"8578933e-2004-c7a3-dd96-36b26cecc512"},{"description":"智能体文本输入","name":"agentRequest","type":"string","require":false,"value":"","key":"8578933e-2004-c7a3-dd96-36b26cecc533","parentKey":"8578933e-2004-c7a3-dd96-36b26cecc512"},{"description":"智能体入参","name":"agentParam","type":"object","require":false,"value":"","key":"8578933e-2004-c7a3-dd96-36b26cecc534","parentKey":"8578933e-2004-c7a3-dd96-36b26cecc512"}]}]},{"description":"是否启用联网搜索","name":"webSearchEnabled","type":"boolean","require":false,"value":"","key":"9362c79f-a3b7-d7a9-db0e-2a5617d72229","parentKey":-1}]}', response_json='{"key":-1,"description":"根节点","name":"root","type":"object","require":false,"value":"","children":[{"description":"事件ID","name":"id","type":"string","require":false,"value":"","key":"adcb3439-2fde-4e56-b9d7-9fa27f5deb67","parentKey":-1},{"description":"事件名称","name":"envent","type":"string","require":false,"value":"","key":"fc440006-34fa-4713-88bc-645febe57d40","parentKey":-1},{"description":"事件数据","name":"data","type":"any","require":false,"value":"","key":"fea2186e-365f-43eb-9d97-d8a42b755425","parentKey":-1}]}', example_json='{
	"input": "curl --location --request POST ''http:\\/\\/10.10.176.135:9500\\/bote\\/api\\/bote\\/chat\\/completions'' \\\\\\r\\n--header ''Authorization: Bearer abcd***1234'' \\\\\\r\\n--header ''Content-Type: application\\/json'' \\\\\\r\\n--header ''Accept: *\\/*'' \\\\\\r\\n--header ''Connection: keep-alive'' \\\\\\r\\n--data-raw ''{\\r\\n    \\"botId\\": \\"1224246182091034624\\",\\r\\n    \\"sessionId\\": \\"1224247868020887552\\",\\r\\n    \\"message\\": {\\r\\n        \\"role\\": \\"user\\",\\r\\n        \\"content\\": \\"中国有几个省\\",\\r\\n        \\"type\\": \\"input\\"\\r\\n    },\\r\\n    \\"sceneId\\": \\"1224246482281566208\\",\\r\\n    \\"sceneName\\": \\"简单对话\\",\\r\\n    \\"tenantId\\": \\"0\\",\\r\\n    \\"clientId\\": \\"a46997d4-4af9-4494-828f-d8c65e4b0fff\\",\\r\\n    \\"webSearchEnabled\\": false\\r\\n}''",
	"output": "Content-Type: text\\/event-stream\\r\\nTransfer-Encoding: chunked\\r\\n\\r\\nevent:sessionId\\r\\ndata:1414098641555787776\\r\\n\\r\\nevent:contextId\\r\\ndata:06649fd5-55ef-42bd-af32-3a843d6c183c\\r\\n\\r\\nevent:scene\\r\\ndata:{\\"sceneName\\":\\"复杂对话\\",\\"sceneId\\":\\"1224249955618263040\\"}\\r\\n\\r\\nid:1224252173260034048\\r\\nevent:text\\r\\ndata:\\"中国的省级行政区共有34个，包括23个省，5个自治区，4个直辖市，3个特别行政区。\\"\\r\\n\\r\\nid:1224252173260034048\\r\\nevent:done\\r\\ndata:[DONE]"
}', catalog_item_id=2001, created_time='2025-02-26 18:14:50', status_cd='00A', updated_time='2025-02-26 18:14:50', creator_id=1, updator_id=1, remark=NULL, header_json='{"key":-1,"description":"根节点","name":"root","type":"object","require":false,"value":"","children":[{"description":"Cookie 请求头","name":"Cookie","type":"string","require":false,"value":"","key":"axef8ad2a-csa1-48a8-bss1-ba5155acg4da","parentKey":-1},{"description":"API 密钥, 值为 \\"Bearer 密钥\\"","name":"Authorization","type":"string","require":false,"value":"","key":"245e2b31-bff6-2c1a-ec7c-3cf340480a98","parentKey":-1}]}', api_detail='对话接口用于聊天窗口与后端的交互，主要用于博特平台前端，如果业务系统需要使用自己的聊天窗口，也可通过此接口与博特平台后端交付。



## 接口鉴权

两种方式:



* 使用 session 鉴权: 传递 `Cookie` 请求头，里面要包含 session 的 cookie（一般名为 `BOTE_SESSION`）

  * 适用于从业务系统前端触发的调用，此时业务系统后端可以从请求中获取到 cookie, 然后转发给博特平台后端

  * 博特平台不同环境使用的 session cookie 名称不同，为方便兼容所有环境，建议直接透传所有 cookie

  * 示例: `Cookie: BOTE_SESSION=xxx`

* API 密钥: 传递 `Authorization` 请求头，值为 "Bearer 密钥"

  * 密钥在平台的【设置 - API 开放 - 鉴权】中申请

  * 不依赖业务系统前端的 cookie, 适用于业务系统后端异步或者在定时任务中调用

  * 示例: `Authorization: Bearer xxx`



## 接口地址



* 请求方法: POST

* 请求地址: `/api/bote/chat/completions`（前面加上环境地址，比如生产环境完整地址为 `http://10.10.176.135:9500/bote/api/bote/chat/completions`）



## 接口入参

```json

{

  // 租户 ID，可选，也可通过请求头 Tenant-Id 或 URL 参数 tenantId 传递。使用 API 密钥鉴权方式时可不传（API 密钥与租户是绑定的，系统能自动查出来）

  "tenantId": "1",

  // 当前机器人 ID, 必填，意图识别时默认限定当前机器人下的场景，除非租户设置开启了多机器人意图识别

  "botId": "1",

  // 机器人 ID 列表，用于限定意图识别的范围，仅在租户设置开启了多机器人意图识别时生效

  "botIds": [],

  // 客户端 ID，可选，格式为 uuid，用于调用方主动中断请求（需要调用 /bote/chat/cancel 接口）

  "clientId": "xxx",

  // 会话 ID，可选，为空或 -1 时不记录会话消息

  "sessionId": "1",

  // 当前上下文 ID, 可选。上下文与场景绑定，用户在聊天窗口中每切换一次场景都会产生新的上下文

  "contextId": "",

  // 当前场景 ID。-1 表示用户手动退出当前场景；指定了场景 ID 但消息内容、文件列表都为空表示用户手动进入场景

  "sceneId": "1",

  // 当前场景名称

  "sceneName": "测试",

  // 上一个场景 ID, 退出场景时使用

  "lastSceneId": "",

  // 上一个上下文 ID, 退出场景时使用

  "lastContextId": "",

  // 消息

  "message": {

    // 消息类型，input: 用户输入，point: 副驾指令。默认为 input

    "type": "input",

    // 消息角色(user: 用户输入, tool: 页面交互)。默认为 user

    "role": "user",

    // 消息内容，用作系统变量中的用户查询($.system.query)。未指定 sceneId 或者开启了场景跳变时也会用于意图识别

    "content": "你好",

    // 文件 ID 列表

    "fileIds": [],

    // 参数（指令表单数据、副驾指令参数），绑定到场景入参（如果场景未配置入参，则会忽略）

    "params": {},

    // 关联消息 ID，可选，用户操作页面产生的消息需要传页面消息的 ID

    "refId": null,

    // 工具调用 ID, 仅简单场景中页面、页面函数回调使用，用于将回调结果作为大模型的工具调用结果

    "toolCallId": null

  },

  // 上下文参数，结构需要与租户功能设置中配置的对话上下文一致，绑定到场景/工作流中的上下文参数

  "contextParams": {}

}

```



## 接口出参



只支持流式输出，使用 Server-Sent Events 技术实现，响应头中会返回 `Content-Type: text/event-stream`。



流式输出的格式与大模型的会话补全接口不同，请勿直接套用大模型的流式接口处理逻辑。



服务器发送的每个事件可能包含以下属性:



* id: 事件 ID, 我们用来表示消息 ID（整数）。如果 id 跟上一个事件相同，表示是同一条回复（工作流中的相同节点产生），回复内容需要拼起来，否则就表示是一条新回复（工作流中的不同节点产生）

  * 仅表示回复的事件会返回，即 text, references, questions, page, pageFunc

* event: 事件名称，用于区分用途

  * `sessionId`: 用于返回会话 ID，入参传了sessionId就返回入参的sessionId，否则返回系统生成的sessionId

  * `contextId`: 用于返回上下文 ID，事件数据为纯文本的 uuid（非 JSON 格式）

  * `scene`: 用于返回意图识别的场景信息，事件数据为场景信息的 JSON 字符串形式，包含 sceneId, sceneName 两个属性

  * `selectScene`: 意图识别未能识别到场景时返回，用于返回多个场景供用户手动选择，事件数据为推荐信息的 JSON 字符串形式，包含 content, scenes 两个属性，content 表示提示信息，scenes 表示场景列表

  * `error`: 错误信息（执行失败，或其它系统错误），事件数据为错误信息（字符串）的 JSON 字符串形式

  * `exception`: 异常堆栈，执行失败时返回

  * `ready`: 表示已进入场景，没有回复。事件数据固定为纯文本 `[READY]`(非 JSON 格式)。两种情况: 1. 用户手动切换到场景; 2. 场景执行结束但没有产生任何回复

  * `exitScene`: 表示退出当前场景，事件数据固定为纯文本 `[EXIT]`(非 JSON 格式)。两种情况: 1. 用户手动退出场景; 2. 场景或场景调用的对话型工作流里执行到了场景结束节点

  * `done`: 表示响应结束，事件数据固定为纯文本 `[DONE]`(非 JSON 格式)

    * 不论执行是否成功，结束时都会发送 `done` 事件

  * `text`: 文本回复，事件数据为文本片段的 JSON 字符串形式（SSE 的响应格式不允许文本内有换行符，因此使用 JSON 形式做个转义）

  * `references`: 参考文档，只在工作流中的知识问答节点开启了参考文档时返回，事件数据为参考文档列表的 JSON 字符串形式

  * `questions`: 追问问题，只在工作流中的知识问答节点开启了追问时返回，事件数据为追问问题列表的 JSON 字符串形式

  * `page`: 页面，事件数据为页面配置的 JSON 字符串形式

  * `pageFunc`: 页面函数，事件数据为页面函数配置的 JSON 字符串形式

* data: 事件数据，具体内容、结构跟事件名称相关



注意:



* 只有 `text` 事件有流式输出的效果（对同一个回复 ID 可能会发送一次或多次事件），其它事件都是一次性返回完整数据（同一个回复 ID 只会发送一次）



正常情况下返回文本回复时的事件顺序: `sessionId` -> `contextId` -> `scene` -> `text`(多次) -> `done`



示例(返回 3 条文本回复，中间那条有流式输出效果):

```

Content-Type: text/event-stream

Transfer-Encoding: chunked

event:sessionId

data:1414098641555787776



event:contextId

data:c9977ecf-d4ed-4a3e-aa10-305a7b7f740b



event:scene

data:{"sceneName":"多回复测试","sceneId":"1215305791857074176"}



id:1216997928345038848

event:text

data:"调用大模型开始"



id:1216997928357621760

event:text

data:"中国的"



id:1216997928357621760

event:text

data:"的"



id:1216997928357621760

event:text

data:"首都"



id:1216997928357621760

event:text

data:"是"



id:1216997928357621760

event:text

data:"北京"



id:1216997940328165376

event:text

data:"调用大模型结束"



event:done

data:[DONE]

```', is_sse='T'
WHERE api_id=1001 and api_name='开启对话';










UPDATE bote.bt_api
SET api_name='BoteClaw 对话', api_desc='发起一次 BoteClaw 对话', relative_path='/bote/chat/completions', req_method='POST', path_json=NULL, query_json=NULL, body_json='{"key":-1,"description":"根节点","name":"root","type":"object","require":false,"value":"","children":[{"description":"当前空间ID","name":"spaceId","type":"integer","require":false,"value":"","key":"adcb3439-2fde-4e56-b9d7-9fa27f5de345","parentKey":-1},{"description":"应用对应的空间ID","name":"tenantId","type":"integer","require":false,"value":"","key":"adcb3439-2fde-4e56-b9d7-9fa27f5deb67","parentKey":-1},{"description":"应用ID，默认-1","name":"botId","type":"integer","require":true,"value":"","key":"fc440006-34fa-4713-88bc-645febe57d40","parentKey":-1},{"description":"客户端 ID，可选","name":"clientId","type":"string","require":false,"value":"","key":"e8c2024a-11a9-81d8-0d42-ba67ad65731b","parentKey":-1},{"description":"会话 ID","name":"sessionId","type":"integer","require":false,"value":"","key":"52309665-aa68-7cd5-bf62-edf0c4d8c706","parentKey":-1},{"description":"消息","name":"message","type":"object","require":false,"value":"","key":"fea2186e-365f-43eb-9d97-d8a42b755425","parentKey":-1,"children":[{"description":"消息类型","name":"type","type":"string","require":false,"value":"","key":"8578933e-2004-c7a3-dd96-36b26cecc589","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"},{"description":"消息角色","name":"role","type":"string","require":false,"value":"","key":"8d2c9f97-150e-b260-6b9f-01b03322975b","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"},{"description":"消息内容","name":"content","type":"string","require":false,"value":"","key":"5b26ea1c-1410-bb11-91ff-52c31a92c928","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"},{"description":"文件 ID 列表","name":"fileIds","type":"array","require":false,"value":"","key":"77d3b639-5071-5c49-f006-5f63e872b2d8","parentKey":"fea2186e-365f-43eb-9d97-d8a42b755425"}]}]}', response_json='{"key":-1,"description":"根节点","name":"root","type":"object","require":false,"value":"","children":[{"description":"事件ID","name":"id","type":"string","require":false,"value":"","key":"adcb3439-2fde-4e56-b9d7-9fa27f5deb67","parentKey":-1},{"description":"事件名称","name":"envent","type":"string","require":false,"value":"","key":"fc440006-34fa-4713-88bc-645febe57d40","parentKey":-1},{"description":"事件数据","name":"data","type":"any","require":false,"value":"","key":"fea2186e-365f-43eb-9d97-d8a42b755425","parentKey":-1}]}', example_json='{
  "input": "curl --location --request POST ''http://10.10.176.135:9500/bote/api/bote/chat/completions'' \\\\\\n--header ''Authorization: Bearer abcd***1234'' \\\\\\n--header ''Content-Type: application/json'' \\\\\\n--header ''Accept: */*'' \\\\\\n--header ''Connection: keep-alive'' \\\\\\n--data-raw ''{\\n    \\"botId\\": \\"-1\\",\\n    \\"tenantId\\": \\"0\\",\\n    \\"spaceId\\": \\"0\\",\\n    \\"sessionId\\": \\"1355102195176443904\\",\\n    \\"message\\": {\\n        \\"role\\": \\"user\\",\\n        \\"content\\": \\"输出当前时间\\",\\n        \\"type\\": \\"input\\"\\n    },\\n    \\"clientId\\": \\"a46997d4-4af9-4494-828f-d8c65e4b0fff\\"\\n}''",
  "output": "Content-Type: text/event-stream\\nTransfer-Encoding: chunked\\n\\nevent:sessionId\\ndata:1414098641555787776\\n\\nid:1774687977332\\nevent:toolCall\\ndata:{\\"toolCallId\\":\\"call_c755ec64631d44819196af\\",\\"toolName\\":\\"get_current_time\\",\\"input\\":{}}\\n\\nid:1774687977332\\nevent:toolCallResult\\ndata:{\\"toolCallId\\":\\"call_c755ec64631d44819196af\\",\\"toolName\\":\\"get_current_time\\",\\"output\\":\\"2026-03-28T16:53:06+0800\\",\\"success\\":true,\\"spentTime\\":\\"6\\"}\\n\\nid:1774687977332\\nevent:text\\ndata:\\"当前时间是 2026年3月28日 16:53:06 (东八区时间)。\\"\\n\\nid:1774687977332\\nevent:done\\ndata:[DONE]"
}', catalog_item_id=2001, created_time='2026-03-28 16:24:14', status_cd='00A', updated_time='2026-03-28 16:24:14', creator_id=1, updator_id=1, remark=NULL, header_json='{
	"key": -1,
	"description": "根节点",
	"name": "root",
	"type": "object",
	"require": false,
	"value": "",
	"children": [
		{
			"description": "Cookie 请求头",
			"name": "Cookie",
			"type": "string",
			"require": false,
			"value": "",
			"key": "axef8ad2a-csa1-48a8-bss1-ba5155acg4da",
			"parentKey": -1
		},
		{
			"description": "API 密钥, 值为 \\"Bearer 密钥\\"",
			"name": "Authorization",
			"type": "string",
			"require": false,
			"value": "",
			"key": "245e2b31-bff6-2c1a-ec7c-3cf340480a98",
			"parentKey": -1
		}
	]
}', api_detail='对话接口用于聊天窗口与后端的交互，主要用于博特平台前端，如果业务系统需要使用自己的聊天窗口，也可通过此接口与博特平台后端交付。

## 接口鉴权
两种方式:

* 使用 session 鉴权: 传递 `Cookie` 请求头，里面要包含 session 的 cookie（一般名为 `BOTE_SESSION`）
  * 适用于从业务系统前端触发的调用，此时业务系统后端可以从请求中获取到 cookie, 然后转发给博特平台后端
  * 博特平台不同环境使用的 session cookie 名称不同，为方便兼容所有环境，建议直接透传所有 cookie
  * 示例: `Cookie: BOTE_SESSION=xxx`
* API 密钥: 传递 `Authorization` 请求头，值为 "Bearer 密钥"
  * 密钥在平台的【设置 - API 开放 - 鉴权】中申请
  * 不依赖业务系统前端的 cookie, 适用于业务系统后端异步或者在定时任务中调用
  * 示例: `Authorization: Bearer xxx`

## 接口地址

* 请求方法: POST
* 请求地址: `/api/bote/chat/completions`（前面加上环境地址，比如生产环境完整地址为 `http://10.10.176.135:9500/bote/api/bote/chat/completions`）

## 接口入参
```json
{
	"botId": "-1",
	"tenantId": "0",
	"spaceId": "0",
	"sessionId": "1355102195176443904",
	"message": {
		"role": "user",
		"content": "输出当前时间",
		"type": "input"
	},
	"clientId": "a46997d4-4af9-4494-828f-d8c65e4b0fff"
}
```

## 接口出参

只支持流式输出，使用 Server-Sent Events 技术实现，响应头中会返回 `Content-Type: text/event-stream`。

流式输出的格式与大模型的会话补全接口不同，请勿直接套用大模型的流式接口处理逻辑。

服务器发送的每个事件可能包含以下属性:

* id: 事件 ID, 我们用来表示消息 ID（整数）。如果 id 跟上一个事件相同，表示是同一条回复（工作流中的相同节点产生），回复内容需要拼起来，否则就表示是一条新回复（工作流中的不同节点产生）
  * 仅表示回复的事件会返回，即 text, references, questions, page, pageFunc
* event: 事件名称，用于区分用途
  * `sessionId`: 用于返回会话 ID，入参传了sessionId就返回入参的sessionId，否则返回系统生成的sessionId
  * `toolCall`: 用于记录触发的工具
  * `toolCallResult`: 用于记录工具的输出
  * `error`: 错误信息（执行失败，或其它系统错误），事件数据为错误信息（字符串）的 JSON 字符串形式
  * `exception`: 异常堆栈，执行失败时返回
  * `done`: 表示响应结束，事件数据固定为纯文本 `[DONE]`(非 JSON 格式)
    * 不论执行是否成功，结束时都会发送 `done` 事件
  * `text`: 文本回复，事件数据为文本片段的 JSON 字符串形式（SSE 的响应格式不允许文本内有换行符，因此使用 JSON 形式做个转义）
* data: 事件数据，具体内容、结构跟事件名称相关

注意:

* 只有 `text` 事件有流式输出的效果（对同一个回复 ID 可能会发送一次或多次事件），其它事件都是一次性返回完整数据（同一个回复 ID 只会发送一次）

正常情况下返回文本回复时的事件顺序: `text`(多次) -> `done`

示例(返回 3 条文本回复，中间那条有流式输出效果):
```
Content-Type: text/event-stream
Transfer-Encoding: chunked

event:sessionId
data:1414098641555787776

id:1774687977332
event:toolCall
data:{"toolCallId":"call_c755ec64631d44819196af","toolName":"get_current_time","input":{}}

id:1774687977332
event:toolCallResult
data:{"toolCallId":"call_c755ec64631d44819196af","toolName":"get_current_time","output":"2026-03-28T16:53:06+0800","success":true,"spentTime":"6"}

id:1774687977332
event:text
data:"当前时间是 2026年3月28日 16:53:06 (东八区时间)。"

id:1774687977332
event:done
data:[DONE]
```', is_sse='T'
WHERE api_id=1003 and api_name='BoteClaw 对话';








INSERT INTO bote.bt_api
(api_id, api_name, api_desc, relative_path, req_method, path_json, query_json, body_json, response_json, example_json, catalog_item_id, created_time, status_cd, updated_time, creator_id, updator_id, remark, header_json, api_detail, is_sse)
VALUES(2026091701, '对话文件上传', '对话文件上传，返回文件信息，fileId 可用于对话接口的 message.fileIds', '/bote/file/uploadSingleFile', 'POST', NULL, '', '{"key":-1,"description":"根节点","name":"root","type":"object","require":false,"value":"","parentKeyPath":[],"children":[{"description":"机器人 ID，可选，作为文件的归属应用(appId)","name":"botId","type":"integer","require":false,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f01","parentKey":-1,"parentKeyPath":[-1]},{"description":"要上传的文件，字段名任意，服务端取请求中的第一个文件","name":"file","type":"file","require":true,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f02","parentKey":-1,"parentKeyPath":[-1]}]}', '{"key":-1,"description":"根节点","name":"root","type":"object","require":false,"value":"","parentKeyPath":[],"children":[{"description":"结果码，0 表示成功","name":"resultCode","type":"string","require":true,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f03","parentKey":-1,"parentKeyPath":[-1]},{"description":"结果描述","name":"resultMsg","type":"string","require":true,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f04","parentKey":-1,"parentKeyPath":[-1]},{"description":"文件信息","name":"resultObject","type":"object","require":true,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05","parentKey":-1,"parentKeyPath":[-1],"children":[{"description":"文件 ID","name":"fileId","type":"string","require":true,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f06","parentKey":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05","parentKeyPath":[-1,"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05"]},{"description":"文件名称（服务端存储名）","name":"fileName","type":"string","require":true,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f07","parentKey":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05","parentKeyPath":[-1,"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05"]},{"description":"原始文件名","name":"originalFileName","type":"string","require":false,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f08","parentKey":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05","parentKeyPath":[-1,"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05"]},{"description":"文件大小，单位字节","name":"fileSize","type":"integer","require":false,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f09","parentKey":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05","parentKeyPath":[-1,"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05"]},{"description":"文件类型，如 png、xlsx","name":"fileType","type":"string","require":false,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f0a","parentKey":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05","parentKeyPath":[-1,"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05"]},{"description":"存储类型","name":"storeType","type":"string","require":false,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f0b","parentKey":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05","parentKeyPath":[-1,"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05"]},{"description":"是否图片","name":"isPicture","type":"boolean","require":false,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f0c","parentKey":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05","parentKeyPath":[-1,"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05"]},{"description":"状态，00A 有效","name":"statusCd","type":"string","require":false,"value":"","key":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f0d","parentKey":"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05","parentKeyPath":[-1,"9b1c2d3e-9c4a-4f18-9a7e-2d5c6a8e1f05"]}]}]}', '{
  "input": "curl --location --request POST ''http://10.10.176.135:9500/bote/api/bote/file/uploadSingleFile'' --header ''Authorization: Bearer abcd***1234'' --header ''Accept: */*'' --header ''Connection: keep-alive'' --form ''file=@\\"/path/to/Knife4j (23).txt\\"'' --form ''botId=1224246182091034624''",
  "output": "{\\n  \\"resultCode\\": \\"0\\",\\n  \\"resultMsg\\": \\"success\\",\\n  \\"resultObject\\": {\\n    \\"fileId\\": \\"1417712312959143936\\",\\n    \\"storeType\\": \\"MINIO\\",\\n    \\"filePathInServer\\": \\"bote/593f4b0b-7003-4665-9f7f-df061e7925d3.txt\\",\\n    \\"fileName\\": \\"Knife4j (23).txt\\",\\n    \\"fileDesc\\": null,\\n    \\"createDate\\": \\"2026-09-17 11:22:35\\",\\n    \\"statusCd\\": \\"00A\\",\\n    \\"statusDate\\": \\"2026-09-17 11:22:35\\",\\n    \\"appId\\": \\"1407991337311903744\\",\\n    \\"fileSize\\": \\"44771\\",\\n    \\"fileType\\": \\"txt\\",\\n    \\"isPicture\\": false\\n  },\\n  \\"stack\\": \\"\\"\\n}"
}
', 2003, '2026-09-17 13:17:34', '00A', '2026-09-17 13:17:34', 1, 1, NULL, '{"key":-1,"description":"根节点","name":"root","type":"object","require":false,"value":"","children":[{"description":"Cookie 请求头","name":"Cookie","type":"string","require":false,"value":"","key":"axef8ad2a-csa1-48a8-bss1-ba5155acg4da","parentKey":-1},{"description":"API 密钥, 值为 Bearer 密钥","name":"Authorization","type":"string","require":false,"value":"","key":"245e2b31-bff6-2c1a-ec7c-3cf340480a98","parentKey":-1}]}', '上传单个文件，返回文件信息。上传成功后返回的 fileId 可用于对话接口 `/api/bote/chat/completions` 的 `message.fileIds`。

## 接口鉴权

两种方式:

* 使用 session 鉴权: 传递 `Cookie` 请求头，里面要包含 session 的 cookie（一般名为 `BOTE_SESSION`）
  * 示例: `Cookie: BOTE_SESSION=xxx`
* API 密钥: 传递 `Authorization` 请求头，值为 "Bearer 密钥"
  * 密钥在平台的【设置 - API 开放 - 鉴权】中申请
  * 示例: `Authorization: Bearer xxx`

## 接口地址

* 请求方法: POST
* 请求地址: `/api/bote/file/uploadSingleFile`（前面加上环境地址，比如生产环境完整地址为 `http://10.10.176.135:9500/bote/api/bote/file/uploadSingleFile`）
* 请求类型: `multipart/form-data`

## 接口入参

### 表单参数

* file: 要上传的文件，必填。字段名任意，服务端取请求中的第一个文件
* botId: 机器人 ID，可选，作为文件的归属应用(appId)

注意: 文件类型受平台【允许上传的文件类型】配置限制，不支持的类型会返回失败。

## 接口出参

```json
{
    "resultCode": "0",
    "resultMsg": "success",
    "resultObject": {
        "fileId": "1417712312959143936",
        "storeType": "MINIO",
        "filePathInServer": "bote/593f4b0b-7003-4665-9f7f-df061e7925d3.txt",
        "fileName": "Knife4j (23).txt",
        "fileDesc": null,
        "createDate": "2026-09-17 11:22:35",
        "statusCd": "00A",
        "statusDate": "2026-09-17 11:22:35",
        "appId": "1407991337311903744",
        "fileSize": "44771",
        "fileType": "txt",
        "isPicture": false
    },
    "stack": ""
}
```

说明:

* `resultCode` 为 `0` 表示成功，其它值表示失败，失败原因见 `resultMsg`
* 返回的 `fileId` 可直接作为对话接口/bote/chat/completions入参 `message.fileIds` 的元素', 'F');
