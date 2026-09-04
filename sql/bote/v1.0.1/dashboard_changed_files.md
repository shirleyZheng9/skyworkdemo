# 首页数据看板改动文件清单

本文档用于汇总本次首页数据看板项目涉及的新增和修改文件，方便后续代码核对、提测、上线检查。

说明：

- 只统计源码、Mapper、测试、SQL 和说明文档。
- 不统计 `target/classes`、`target/test-classes`、`surefire-reports` 等编译或测试生成文件。
- 路径均以项目根目录 `aadp-Release-v1.0.1` 为基准。

## Controller

| 文件路径 | 说明 |
| --- | --- |
| `bote-service/src/main/java/com/iwhalecloud/bote/controller/dashboard/DataDashboardController.java` | 首页数据看板接口入口，包含概览、分布、今日动态、每日消息趋势等接口。 |

## Service 接口

| 文件路径 | 说明 |
| --- | --- |
| `bote-service/src/main/java/com/iwhalecloud/bote/service/dashboard/IDataDashboardService.java` | 首页数据看板查询服务接口。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/service/dashboard/IModelUsageAggregationService.java` | 模型调用量累计汇总服务接口。 |

## Service 实现与支撑类

| 文件路径 | 说明 |
| --- | --- |
| `bote-service/src/main/java/com/iwhalecloud/bote/service/dashboard/impl/DataDashboardServiceImpl.java` | 首页数据看板核心查询逻辑，包含今日动态、每日消息趋势、资源概览和分布统计。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/service/dashboard/impl/ConversationMessageTrendRefreshService.java` | 每日对话消息量趋势缓存刷新逻辑。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/service/dashboard/impl/TodayDynamicsRefreshService.java` | 今日动态缓存刷新逻辑。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/service/dashboard/impl/KnowledgeFileDistributionRefreshService.java` | 知识库文件类型分布缓存刷新逻辑。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/service/dashboard/impl/OverviewMetricsRefreshService.java` | 顶部资源概览缓存刷新逻辑。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/service/dashboard/impl/ModelUsageAggregationServiceImpl.java` | 模型调用量累计汇总逻辑，按检查点增量处理模型日志。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/service/dashboard/support/DashboardRefreshCoordinator.java` | 看板缓存刷新协调器，用于合并同一租户同一统计日的重复刷新请求。 |

## Cache

| 文件路径 | 说明 |
| --- | --- |
| `bote-service/src/main/java/com/iwhalecloud/bote/cache/DataDashboardCache.java` | 首页数据看板 Redis 缓存封装，包含概览、今日动态、每日消息趋势和知识库文件类型分布缓存。 |

## Job

| 文件路径 | 说明 |
| --- | --- |
| `bote-service/src/main/java/com/iwhalecloud/bote/job/AggregateModelUsageDashboardJob.java` | 模型调用量看板汇总定时任务入口。 |

## Mapper 接口

| 文件路径 | 说明 |
| --- | --- |
| `bote-service/src/main/java/com/iwhalecloud/bote/mapper/dashboard/DataDashboardMapper.java` | 首页数据看板实时查询 Mapper 接口。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/mapper/dashboard/ModelUsageAggregationMapper.java` | 模型调用量累计汇总 Mapper 接口。 |

## Mapper XML

| 文件路径 | 说明 |
| --- | --- |
| `bote-service/src/main/resources/com/iwhalecloud/bote/mapper/dashboard/DataDashboardMapper.xml` | 首页数据看板查询 SQL，包含概览、分布、今日动态、每日消息趋势。 |
| `bote-service/src/main/resources/com/iwhalecloud/bote/mapper/dashboard/ModelUsageAggregationMapper.xml` | 模型调用量累计汇总 SQL，包含检查点、增量日志查询、汇总写入和模型状态校准。 |

## DTO

| 文件路径 | 说明 |
| --- | --- |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/AgentModeCountDTO.java` | 智能体模式分布数据库查询结果。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/ConversationMessageTrendCacheDTO.java` | 每日对话消息量趋势 Redis 缓存数据结构。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/ConversationMessageTrendCountDTO.java` | 每日对话消息量趋势数据库聚合结果。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/ConversationMessageTrendItemVO.java` | 每日对话消息量趋势接口返回项。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/DistributionItemVO.java` | 通用分布类接口返回项。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/KnowledgeFileTypeCountDTO.java` | 知识库文件类型分布数据库查询结果。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/ModelUsageAggregationCheckpointDTO.java` | 模型调用量汇总检查点 DTO。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/ModelUsageDistributionItemVO.java` | 模型接入使用分布接口返回项。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/ModelUsageLogIncrementDTO.java` | 模型使用日志增量查询结果。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/ModelUsageStatDTO.java` | 模型累计调用量统计查询结果。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/ModelUsageStatusCandidateDTO.java` | 模型状态校准候选数据。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/OverviewMetricCountDTO.java` | 顶部资源概览数据库统计结果。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/OverviewMetricVO.java` | 顶部资源概览接口返回项。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/OverviewMetricsCacheDTO.java` | 顶部资源概览缓存数据结构。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/TodayDynamicsCacheDTO.java` | 今日动态 Redis 缓存数据结构。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/TodayDynamicsVO.java` | 今日动态接口返回对象。 |
| `bote-service/src/main/java/com/iwhalecloud/bote/dto/dashboard/query/DashboardTenantQuery.java` | 看板租户查询参数。 |

## SQL 脚本

| 文件路径 | 说明 |
| --- | --- |
| `sql/bote/v1.0.1/ddl_20260830_dashboard_model_usage_total.sql` | 模型累计使用量汇总表、检查点表和增量扫描索引。 |
| `sql/bote/v1.0.1/ddl_20260831_dashboard_model_usage_product_type.sql` | 旧环境模型汇总表补充 `product_type` 字段的兼容脚本。 |
| `sql/bote/v1.0.1/dml_20260830_dashboard_model_usage_total.sql` | 初始化模型汇总检查点和定时任务。 |
| `sql/bote/v1.0.1/ddl_20260831_dashboard_overview_indexes.sql` | 顶部资源概览、智能体模式分布、知识库文件类型分布查询索引。 |
| `sql/bote/v1.0.1/ddl_20260831_dashboard_today_trend_indexes.sql` | 今日动态和每日对话消息趋势查询索引。 |
| `sql/bote/v1.0.1/dashboard_all_in_one.sql` | 新环境可直接执行的看板 SQL 合并脚本。 |

## 说明文档

| 文件路径 | 说明 |
| --- | --- |
| `sql/bote/v1.0.1/dashboard_sql_execution_order.md` | 看板数据库脚本执行顺序说明文档。 |
| `sql/bote/v1.0.1/dashboard_changed_files.md` | 本文档，本次看板项目改动文件清单。 |

## 测试文件

| 文件路径 | 说明 |
| --- | --- |
| `bote-service/src/test/java/com/iwhalecloud/bote/cache/DataDashboardCacheTest.java` | 看板缓存逻辑测试。 |
| `bote-service/src/test/java/com/iwhalecloud/bote/service/dashboard/impl/DataDashboardServiceImplTest.java` | 看板查询服务逻辑测试。 |
| `bote-service/src/test/java/com/iwhalecloud/bote/service/dashboard/impl/KnowledgeFileDistributionRefreshServiceTest.java` | 知识库文件类型分布刷新逻辑测试。 |
| `bote-service/src/test/java/com/iwhalecloud/bote/service/dashboard/impl/ModelUsageAggregationServiceImplTest.java` | 模型调用量累计汇总逻辑测试。 |
| `bote-service/src/test/java/com/iwhalecloud/bote/service/dashboard/support/DashboardRefreshCoordinatorTest.java` | 看板刷新协调器测试。 |

## 当前特别说明

- `queryTodayDynamics` 已移除今日动态中的插件/MCP调用次数，只保留新增智能体、新增知识库、大模型调用次数、知识构建文档。
- `queryTodayDynamics` 已改用专用今日资源新增 SQL，不再复用包含模型、插件/MCP总量的顶部概览 SQL。
- `queryTodayDynamics` 已增加 Redis 逻辑缓存，冷缓存查库，热缓存直接返回并异步刷新。
- `queryConversationMessageTrend` 已增加 Redis 逻辑缓存，冷缓存查库，热缓存直接返回并异步刷新。
- 今日大模型调用次数只查 `bt_model_usage_log` 当前表，不查历史表。
- `dashboard_all_in_one.sql` 面向新环境执行，已将 `product_type` 直接放入新增表结构。
