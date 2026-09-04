<#-- @ftlvariable name="isChatflow" type="java.lang.Boolean" -->
<#if isChatflow>
## 流程图约束

1. 流程的输入有 2 种:
    * 用户消息: 用户在聊天窗口中发送的消息，内容是自然语言，通常需要使用大模型判断其意图，或者提取其中的参数
    * 流程入参: 结构化的参数，伴随用户发送的消息隐式传递，每次执行流程时可能不同

2. 流程的输出有 3 种，对应 3 种节点类型:
    * 回复(reply): 文本回复。其它节点比如大模型、知识问答返回的结果并不会直接输出，必须通过回复节点引用才会返回给用户
    * 页面(page): 使用卡片展示复杂的信息、表单
    * 页面函数(pageFunc): 用于触发业务系统的页面动作

3. 流程变量的值会持久化存储，流程执行前会把流程变量还原为上次执行结束时的状态

4. 流程可以处理用户的多轮会话:
  * 需要先分析整个会话有几种状态，必要时设计一个流程变量维护会话状态（初始状态为空值）
  * 每一轮会话中，流程都会从头开始执行，需要在流程开头判断会话状态（根据流程变量判断，或者对用户消息做意图识别），对不同状态执行不同的分支

</#if>
## 流程图格式

流程图使用 JSON 格式，结构如下：

```json
{
  // 流程名称，使用中文，不超过 20 个字
  "name": "示例",
  // 流程编码，遵循 Java 变量命名规则
  "code": "demo",
  // 流程的入参，可选，需要以一个虚拟的 root 对象开始，实际的入参配置为 root 对象的子节点
  "request": {
    "name": "root",
    "type": "object",
    "children": []
  },
<#if !isChatflow>
  // 流程的出参，可选，需要以一个虚拟的 root 对象开始，实际的出参配置为 root 对象的子节点
  "response": {
    "name": "root",
    "type": "object",
    "children": []
  },
</#if>
  // 流程变量，可选，类似 Java 中的局部变量，可用于存储不同条件分支中的节点出参
  "variables": [],
   // 节点列表，第一个节点必须是开始节点(type=start)
  "nodes": [
    {
      // 节点名称，使用中文，不超过 20 个字
      "name": "示例",
      // 节点编码，遵循 Java 变量命名规则。节点编码必须唯一
      "code": "demo",
      // 节点类型
      "type": "reply",
      // 节点的配置数据，具体结构取决于节点类型
      "data": {
      }
    }
  ],
  // 线条列表，线条的方向表示执行的顺序
  "edges": [
    {
      // 起始节点编码
      "from": "reply",
      // 目标节点编码。不能是开始节点
      "to": "reply2",
      // 线条标签，可选，同一个起始节点连接多个目标节点时用来区分不同线条的含义
      "label": ""
    }
  ]
}
```

## 参数结构

流程入参、流程变量、节点入参、部分节点出参都需要描述参数结构，结构如下:

```json
  {
    // 参数名称，遵循 Java 变量命名规则
    "name": "customerName",
    // 参数的中文描述
    "description": "客户名称",
    // 参数类型
    "type": "string",
    // 参数值，可以是固定的参数值，也可以是引用表达式。仅在赋值时使用，如果只是描述结构不需要指定
    "value": "",
    // 子节点列表，仅在参数类型为 object 或 array 时使用
    "children": []
  }
```

多数地方表示参数结构时会有一个虚拟的 root 根节点，用来表示整体参数是个对象，在生成引用表达式时不能包含 root。

参数类型:

* object: 对象，对应 Java 中的 Map。children 表示对象的属性，为空时表示可以包含任意属性
* array: 数组，对应 Java 中的 List。children 最多只能有一个元素，表示数组的元素类型，为空时表示列表元素可以是任意类型
* string: 字符串，对应 Java 中的 String
* integer: 整数，对应 Java 中的 Long
* number: 浮点数，对应 Java 中的 BigDecimal
* boolean: 布尔，对应 Java 中的 Boolean
* date: 日期，不含时间，对应 Java 中的 java.time.LocalDate, 字符串形式为 yyyy-MM-dd
* datetime: 日期时间，对应 Java 中的 java.util.Date, 字符串形式为 yyyy-MM-dd HH:mm:ss
* any: 任意类型，不确定类型或者可能是多种类型时使用，对应 Java 中的 Object

## 参数赋值

节点入参、节点的部分配置属性需要赋值，赋值时既可以指定常量值，也可以引用表达式。

表达式格式为 $.expression, 用于引用流程入参、流程变量、节点出参、系统变量、登录信息等：

* 流程入参:  $.input.PROPERTY_PATH, 其中 PROPERTY_PATH 是流程入参的属性路径. 比如 $.input.name 表示引用流程入参中的 name 属性, $.input.customer.name 表示引用流程入参中的 customer 对象的 name 属性
* 流程变量:  $.variable.PROPERTY_PATH, 其中 PROPERTY_PATH 是流程变量的属性路径
* 节点出参: $.step.STEP_CODE.PROPERTY_PATH, 其中 STEP_CODE 是节点编码, PROPERTY_PATH 是节点出参中的属性路径. 比如 $.step.queryCustomer.name 表示引用节点编码为 queryCustomer 的节点的出参中的 name 属性。不指定 PROPERTY_PATH 时表示引用该节点的所有出参，比如 $.step.queryCustomer
  * 只能引用从开始节点到当前节点之间的节点的出参，不能引用之后的节点的出参，在条件节点后面也能引用某一个分支中的节点出参（确实需要引用时，应该定义一个流程变量传递节点出参）
  * 如果要引用列表的第一个元素的 name 属性，格式为 $.step.STEP_CODE.list.[0].name
* $.system.now: 当前时间(yyyy-MM-dd HH:mm:ss)
* $.system.today: 当前日期(yyyy-MM-dd)
* $.system.uuid: 随机生成的 UUID 字符串
<#if isChatflow>
* $.system.query: 用户消息内容（用户在聊天窗口中发送的消息）
* $.system.fileIds: 用户上传的文件 ID 列表，对应的 Java 类型为 `List<Long>`
* $.system.replied: 是否已生成回复
</#if>
* $.session.userId: 当前登录用户 ID
* $.session.userName: 当前登录用户的用户名
* $.session.realName: 当前登录用户的姓名
* $.session.sessionId: 当前登录信息的 session ID
* $.system.cookie: 当前 HTTP 请求的 Cookies

注意: 表达式只支持简单地引用其它参数，不支持做任何计算，如果需要计算(比如字符串拼接、获取列表大小、列表查找、&& || 等逻辑运算)，应该使用脚本节点实现。

<#noparse>部分节点配置属性的赋值支持字符串模板，即在文本中嵌入表达式，但表达式需要使用 ${expression} 形式，比如 `你好, ${session.realName}`</#noparse>

## 节点类型

以下是各个节点的说明，包括类型编码、节点配置数据的结构、用途、使用限制等。

### 开始(type=start)

表示流程的开始节点，不包含配置数据

### 结束(type=end)

<#if isChatflow>
表示业务场景执行结束，需要退出场景。

只在用户明确要求时添加，一般不需要，执行到没有线条的节点时就会自动停止执行
<#else>
表示流程执行结束，并返回执行结果（即流程出参）

```json
{
  // 流程出参的赋值
  "parameters": {
    "name": "root",
    "type": "object",
    "children": [
      {
        "name": "customer",
        "type": "object",
        "value": "$.variable.customer"
      }
    ]
  }
}
```
</#if>

### 条件(type=if)

用于根据不同条件执行不同的分支，类似 Java 中的 if-elseif-else 结构。

```json
{
  // 条件分支列表，至少包含一个分支。不包含 else 分支，else 分支没有条件，不需要显式配置
  "branches": [
    {
      // 分支编码，用于标识分支，必须唯一。分支与后面的节点连线时，需要将分支编码设置为线条的 label, 表示该分支的条件满足时执行该线条连接的节点
      "branchCode": "idNotEqual",
      // 分支名称，用于描述分支，使用中文，不超过 20 个字
      "branchName": "ID 不相等",
      // 条件表达式，支持嵌套
      "condition": {
        // 比较操作符
        "operator": "!=",
        // 左操作数，常量值或引用表达式
        "left": "$.input.id",
        // 由操作数，常量值或引用表达式。有些单元操作符不需要右操作数
        "right": "$.input.id",
        // 子条件列表，仅用于 operator=and/or
        "children": []
      }
    }
  ]
}
```

支持的比较操作符:

* and, or: 特殊的操作符，对应 Java 中的 &&, ||, 用于将多个子条件(children)连接起来
* =, !=, >, >=, <, <=: 相等、大小比较，支持数值、字符串、日期、日期时间
* isNull, isNotNull: 检查左操作数是否为 null （对应 Java 中的 null）
* isEmpty, isNotEmpty: 检查左操作数是否为空（null、空对象、空数组、空字符串都当作空）
* between: 检查左操作数是否在两个数值之间（包含起始值、结束值），右操作数为 "~" 分隔的起始值、结束值
* enumIn: 检查左操作数是否在枚举值列表中，右操作数为 "," 分隔的枚举值列表
* 字符串比较
    * contains: 包含右操作数
    * notContains: 不包含右操作数
    * startsWith: 以右操作数开头
    * notStartsWith: 不以右操作数开头
    * endsWith: 以右操作数结尾
    * notEndsWith: 不以右操作数结尾

条件节点后面可以连接多个下级节点，线条的 label 设置为分支编码，表示该分支的条件满足时执行该线条连接的节点。else 分支使用特殊的 label "else"。

### 设置变量(type=setVariable)

用于给流程变量赋值

```json
{
  // 变量列表，可以给一个或多个变量赋值，结构与流程变量的结构一致，只是增加 value 属性表示赋值
  "variables": [
    {
      // 变量名称，必须先在流程变量中定义
      "name": "customer",
      "description": "客户",
      "type": "object",
      // 赋值，支持常量值、引用表达式。对于对象、数组，如果要赋值常量值需要使用 JSON 字符串
      "value": "$.input.customer",
      // 如果类型为对象，可以同时给对象的子节点赋值，子节点的赋值会覆盖对象赋值带过来的属性值。如果类型为数组，只能给数组赋值，不能给子节点赋值
      "children": [
        {
          "name": "name",
          "type": "string",
          "value": "$.step.queryCustomer.name"
        }
      ]
    }
  ]
}
```

<#if isChatflow>
### 回复(type=reply)

用于向用户回复消息，可以是固定的文本，也可以引用其它参数（比如大模型节点的出参）。

```json
{
<#noparse>
  // 消息内容，字符串模板
  "messageContent": "你好，${step.queryCustomer.custName}"
</#noparse>
}
```

### 页面(type=page)

用于向用户展示页面，页面可以展示信息，也可以包含表单，让用户输入信息。

```json
{
  // 页面 ID
  "pageId": 1,
  // 是否用作会话记忆，默认为 false, 可省略
  "memorized": false,
  // 是否使用自定义的记忆内容作为会话记忆，默认为 false, 可省略，只在 memorized=true 时生效。未开启时表示使用入参
  "customMemorized": false,
  // 自定义记忆内容（常量值或引用表达式），只有 memorized, customMemorized 都为 true 时才生效
  "memoryContent": "$.step.queryCustomers.list",
  // 页面入参的赋值，没有入参时可省略
  "parameters": {
    "name": "root",
    "type": "object",
    "children": [
      {
        "name": "customers",
        "description": "客户列表",
        "type": "list",
        "value": "$.step.queryCustomers.list"
      }
    ]
  }
}
```

### 页面函数(type=pageFunc)

用于触发前端页面的 JavaScript 函数

```json
{
  // 页面函数 ID
  "pageFuncId": 1,
  // 页面函数入参的赋值，没有入参时可省略
  "parameters": {
    "name": "root",
    "type": "object",
    "children": [
      {
        "name": "customers",
        "description": "客户列表",
        "type": "list",
        "value": "$.step.queryCustomers.list"
      }
    ]
  }
}
```
</#if>

### API 服务(type=service)

用于调用 API 服务，返回值作为节点出参

```json
{
  // 服务 ID
  "serviceId": 1,
  // API 服务入参的赋值，没有入参时可省略
  "parameters": {
    "name": "root",
    "type": "object",
    "children": [
      {
        "name": "body",
        "description": "请求体",
        "type": "object",
        "children": [
          {
            "name": "custName",
            "description": "客户名称",
            "type": "string",
            "value": "$.input.custName"
          }
        ]
      }
    ]
  }
}
```

### SQL 服务(type=sql)

用于调用 SQL 服务（和 API 服务基本相同），返回值作为节点出参

```json
{
  // SQL 服务 ID
  "sqlId": 1,
  // SQL 服务入参的赋值，没有入参时可省略
  "parameters": {
    "name": "root",
    "type": "object",
    "children": [
      {
        "name": "custName",
        "description": "客户名称",
        "type": "string",
        "value": "$.input.custName"
      }
    ]
  }
}
```

### 流程(type=workflow)

用于调用子流程（另一个流程），子流程的流程变量作为节点出参

为避免单个流程过于复杂、难以维护，可以把复杂任务拆解为多个子流程。

```json
{
  // 流程 ID
  "flowId": 1,
  // 流程入参的赋值，没有入参时可省略
  "parameters": {
    "name": "root",
    "type": "object",
    "children": [
      {
        "name": "custName",
        "description": "客户名称",
        "type": "string",
        "value": "$.input.custName"
      }
    ]
  }
}
```

### 插件(type=llmSkill)

用于调用封装好的大模型功能，使用方式上和 API 服务基本相同，只是节点出参固定只有一个 text 属性，表示大模型回复的文本。

```json
{
  // 插件 ID
  "apiId": 1,
  // 插件入参的赋值，没有入参时可省略
  "parameters": {
    "name": "root",
    "type": "object",
    "children": [
      {
        "name": "custName",
        "description": "客户名称",
        "type": "string",
        "value": "$.input.custName"
      }
    ]
  }
}
```

### 工具函数(type=toolbox)

用于调用工具函数（即预定义的 Groovy 函数），函数的返回值作为节点出参。

```json
{
  // 工具函数 ID
  "funcId": 1,
  // 工具函数入参的赋值，没有入参时可省略
  "parameters": {
    "name": "root",
    "type": "object",
    "children": [
      {
        "name": "custName",
        "description": "客户名称",
        "type": "string",
        "value": "$.input.custName"
      }
    ]
  }
}
```

### 脚本(type=script)

用于执行自定义的 Python 3 或 Groovy 函数，可以用于对参数做计算、转换。

Groovy 脚本允许使用的 groovy 模块: groovy-datetime, groovy-dateutil, 不允许使用 groovy-sql, groovy-json, groovy-xml, 可以使用 Java 库: spring-jdbc, Jackson

```json
{
  // 脚本类型: python3, groovy. 优先使用 groovy
  "scriptType": "groovy",
  // 脚本内容，必须定义一个 invoke 方法，方法参数是节点入参，返回值是节点出参
  "scriptContent": "def invoke(Long a, Long b) {\n  return ['sum': a + b];\n}",
  // 脚本入参的赋值，没有入参时可省略
  "parameters": {
    "name": "root",
    "type": "object",
    "children": [
      {
        "name": "a",
        "type": "integer",
        "value": "$.input.a"
      },
      {
        "name": "a",
        "type": "integer",
        "value": "$.input.b"
      }
    ]
  },
  // 节点出参，必须和 invoke 方法的返回值保持一致，没有返回值时可省略
  "outData": {
    "name": "root",
    "type": "object",
    "children": [
      {
        "name": "sum",
        "description": "和",
        "type": "integer"
      }
    ]
  }
}
```

Groovy 函数示例:

```groovy
def invoke(Long a, Long b) {
  return ['sum': a + b]
}
```

Python 3 函数示例:

```python
def invoke(a, b):
  return {'sum': a + b}
```

### 大模型(type=llm)

用于调用大模型的会话补全接口，节点出参只有一个 text 属性，表示大模型的回复内容

```json
{
  // 大模型 ID, -1 表示使用默认大模型
  "modelId": -1,
  // 消息列表，只有第一条可以是 system 消息，后面是 user 和 assistant 消息，用于给大模型提供会话历史，辅助大模型回答 userMessage
  "messages": [
    {
      // 消息角色
      "role": "system",
      // 消息内容，字符串模板
      "content": "你是一个智能助手"
    },
    {
      "role": "user",
      "content": "中国的首都是哪里？"
    },
    {
      "role": "assistant",
      "content": "北京"
    }
  ],
  // 用户消息，字符串模板。这是最后一条 user 消息，用于指定让大模型回答的问题
  <#noparse>"userMessage": "${system.query}",</#noparse>
<#if isChatflow>
  // 是否流式输出，默认为 false。当大模型的回复内容用于回复节点时，尽量开启流式输出以提升用户体验，如果用于其它节点则不应开启
  "stream": false,
</#if>
  // 视觉配置，用于指定图片，需要大模型支持视觉理解能力。默认不开启，可以省略
  "vision": {
    // 是否启用视觉
    "enabled": false,
    // 图片地址或文件 ID(取值表达式), 常量值只能是一个文件，引用变量时可以是多个文件
    "files": "$.system.fileIds"
  },
  // 记忆配置，开启时会自动将用户和流程的会话记录作为历史消息插入到最后一条 user 消息前，作为大模型的上下文。默认不开启，可以省略。只有当用户消息可能跟会话记录有关时才需要开启
  "memory": {
    // 是否开启记忆
    "enabled": false,
    // 是否开启记忆窗口，默认不开启。不开启时会传递所有会话记录，开启时只取最近的 windowSize 条会话记录
    "windowEnabled": false,
    // 记忆窗口大小
    "windowSize": 50
  }
}
```

### 问题分类(type=questionClassifier)

用于通过大模型对用户的问题进行分类，也可用作意图识别。

问题分类节点可以连接多个下级节点，线条的 label 设置为分类 ID, 表示命中该分类时执行该线条连接的节点。

节点出参只有一个 name 属性，表示分类名称，通常不需要使用（不同分类要执行的下级节点通过线条表示，不需要判断分类名称）。

```json
{
  // 大模型 ID, -1 表示使用默认大模型
  "modelId": -1,
  // 问题，字符串模板
  <#noparse>"question": "${system.query}",</#noparse>
  // 指令，字符串模板。可选，只有分类名称不足以让大模型正确分类时需要设置，用于指导大模型正确分类
  "instruction": null,
  // 分类列表，隐含一个 {"id": "else", "name": null} 分类，表示问题与用户配置的所有分类都匹配不上，通常不需要显式添加"其它"、"其他"分类
  "classifications": [
    {
      // 分类 ID，必须唯一，使用数字从 1 递增即可。问题分类与后面的节点连线时，需要将分类 ID 设置为线条的 label, 表示该命中该分类时执行该线条连接的节点
      "id": "1",
      // 分类名称，应该简短、直白，方便大模型理解
      "name": "是"
    },
    {
      "id": "2",
      "name": "否"
    }
  ],
  // 记忆配置，开启时会自动将用户和流程的会话记录传给大模型，辅助大模型更准确地理解用户问题。默认不开启，可以省略
  "memory": {
    // 是否开启记忆
    "enabled": false,
    // 是否开启记忆窗口，默认不开启。不开启时会传递所有会话记录，开启时只取最近的 windowSize 条会话记录
    "windowEnabled": false,
    // 记忆窗口大小
    "windowSize": 50
  }
}
```

### 参数提取(type=paramExtractor)

用于通过大模型从一段文本中提取结构化的参数，节点出参和要提取的参数一致。

```json
{
  // 大模型 ID, -1 表示使用默认大模型
  "modelId": -1,
  // 输入文本，字符串模板，用于提取参数的来源
  <#noparse>"input": "${system.query}",</#noparse>
  // 指令，字符串模板。可选，只有参数的名称、描述不足以让大模型正确提取时需要设置，用于指导大模型正确提取参数
  "instruction": null,
  // 要提取的参数
  "parameters": {
    "type": "object",
    "children": [
      {
        "name": "custName",
        "description": "客户名称",
        "type": "string"
      },
      {
        "name": "phone",
        "description": "手机号码",
        "type": "string"
      }
    ]
  },
  // 记忆配置，开启时会自动将用户和流程的会话记录传给大模型，辅助大模型更准确地理解输入内容。默认不开启，可以省略
  "memory": {
    // 是否开启记忆
    "enabled": false,
    // 是否开启记忆窗口，默认不开启。不开启时会传递所有会话记录，开启时只取最近的 windowSize 条会话记录
    "windowEnabled": false,
    // 记忆窗口大小
    "windowSize": 50
  }
}
```

### 知识问答(type=knowledgeChat)

用于根据问题从知识库中检索相关知识，并使用大模型总结相关知识生成最终回答。节点出参只有一个 text 属性，表示回答内容。

```json
{
  // 知识库 ID(常量值或引用表达式)
  "knowledgeId": "1",
  // 问题，字符串模板
  <#noparse>"question": "${system.query}",</#noparse>
  // 记忆配置，开启时会自动将用户和流程的会话记录传给大模型，辅助大模型更准确地理解输入内容。默认不开启，可以省略
  "memory": {
    // 是否开启记忆
    "enabled": false,
    // 是否开启记忆窗口，默认不开启。不开启时会传递所有会话记录，开启时只取最近的 windowSize 条会话记录
    "windowEnabled": false,
    // 记忆窗口大小
    "windowSize": 50
  },
  <#if isChatflow>
  // 是否流式输出，默认为 false。当回答内容用于回复节点时，尽量开启流式输出以提升用户体验，如果用于其它节点则不应开启
  "stream": false,
  </#if>
  // 是否返回参考文档，默认为 false，一般不需要开启
  "withReferences": false,
  // 是否生成几个相关问题展示给用户，默认为 false, 一般不需要开启
  "withQuestions": false
}
```

### 知识检索(type=knowledgeRetrieval)

用于根据问题从知识库中检索相关知识，节点出参表示检索到的原始知识，包含 text, image 2 个属性，text 表示文本列表，image 表示图片列表。

```json
{
  // 知识库 ID(常量值或引用表达式)
  "knowledgeId": "1",
  // 问题，字符串模板
  <#noparse>"question": "${system.query}",</#noparse>
  // 召回数量，默认为 10
  "topK": 10
}
```
