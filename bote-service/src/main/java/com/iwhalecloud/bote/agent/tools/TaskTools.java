package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.annotation.ToolRequest;
import com.iwhalecloud.bote.agent.memory.helper.ReminderGenerator;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.common.consts.GeneralAgentConsts;
import com.iwhalecloud.bote.common.enums.SystemReminderType;
import com.iwhalecloud.bote.dto.SystemReminder;
import com.iwhalecloud.bote.dto.agent.SessionTaskDTO;
import com.iwhalecloud.bote.dto.agent.MemoryMessage;
import com.iwhalecloud.bote.dto.agent.task.CreateTaskRequest;
import com.iwhalecloud.bote.dto.agent.task.UpdateTaskRequest;
import com.iwhalecloud.bote.entity.agent.SessionTaskEntity;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.service.agent.ISessionTaskService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 任务管理工具：供通用智能体创建、查询、更新、删除任务
 *
 * @author bianjp
 * @since 2026-04-10
 */
public final class TaskTools {
  private static final ISessionTaskService agentTaskService = SpringUtil.getBean(ISessionTaskService.class);

  /** 发送任务提醒阈值: 距离上次使用任务管理工具至少间隔的对话轮次 */
  private static final int TASK_REMINDER_TURNS_SINCE_WRITE = 10;
  /** 发送任务提醒阈值: 两次任务提醒之间至少间隔的对话轮次 */
  private static final int TASK_REMINDER_TURNS_BETWEEN_REMINDERS = 10;
  /** 任务管理工具名称列表 */
  private static final List<String> TASK_TOOL_NAMES = List.of("task_create", "task_update", "task_list", "task_get");
  /** 任务管理工具提示语 */
  private static final String TASK_REMINDER_INTRO = "The task tools haven't been used recently. " +
    "If you're working on tasks that would benefit from tracking progress, consider using `task_create` to add new tasks and `task_update` to update task status (set to in_progress when starting, completed when done). " +
    "Also consider cleaning up the task list if it has become stale. " +
    "Only use these if relevant to the current work. " +
    "This is just a gentle reminder - ignore if not applicable. " +
    "Make sure that you NEVER mention this reminder to the user";

  private TaskTools() {
  }

  @Tool(
    name = "task_create",
    description = """
      Use this tool to create a structured task list for your current coding session. This helps you track progress, organize complex tasks, and demonstrate thoroughness to the user.
      It also helps the user understand the progress of the task and overall progress of their requests.
      ## When to Use This Tool
      Use this tool proactively in these scenarios:
      - Complex multi-step tasks - When a task requires 3 or more distinct steps or actions
      - Non-trivial and complex tasks - Tasks that require careful planning or multiple operations
      - Plan mode - When using plan mode, create a task list to track the work
      - User explicitly requests todo list - When the user directly asks you to use the todo list
      - User provides multiple tasks - When users provide a list of things to be done (numbered or comma-separated)
      - After receiving new instructions - Immediately capture user requirements as tasks
      - When you start working on a task - Mark it as in_progress BEFORE beginning work
      - After completing a task - Mark it as completed and add any new follow-up tasks discovered during implementation
      ## When NOT to Use This Tool
      Skip using this tool when:
      - There is only a single, straightforward task
      - The task is trivial and tracking it provides no organizational benefit
      - The task can be completed in less than 3 trivial steps
      - The task is purely conversational or informational
      NOTE that you should not use this tool if there is only one trivial task to do. In this case you are better off just doing the task directly.
      ## Task Fields
      - **subject**: A brief, actionable title in imperative form (e.g., \\"Fix authentication bug in login flow\\")
        - **description**: What needs to be done
      - **activeForm** (optional): Present continuous form shown in the spinner when the task is in_progress (e.g., \\"Fixing authentication bug\\"). If omitted, the spinner shows the subject instead.
        All tasks are created with status `pending`.
      ## Tips
      - Create tasks with clear, specific subjects that describe the outcome
      - After creating tasks, use task_update to set up dependencies (blocks/blockedBy) if needed
      - Check task_list first to avoid creating duplicate tasks
      """
  )
  public static String taskCreate(@ToolRequest CreateTaskRequest request, ToolContext toolContext) {
    String subject = StringUtils.trimToNull(request.getSubject());
    String description = StringUtils.trimToNull(request.getDescription());
    Assert.notNull(subject, "Error: subject is required");
    Assert.notNull(description, "Error: description is required");

    SessionTaskEntity task = new SessionTaskEntity();
    task.setSessionId(toolContext.sessionId());
    task.setSubject(subject.trim());
    task.setDescription(description.trim());
    task.setActiveForm(request.getActiveForm());

    Integer taskId = agentTaskService.createTask(task);
    return "Task #%s created successfully: %s".formatted(taskId, subject);
  }

  @Tool(
    name = "task_update",
    description = """
      Use this tool to update a task in the task list.

      ## When to Use This Tool

      **Mark tasks as resolved:**
      - When you have completed the work described in a task
      - When a task is no longer needed or has been superseded
      - IMPORTANT: Always mark your assigned tasks as resolved when you finish them
      - After resolving, call TaskList to find your next task

      - ONLY mark a task as completed when you have FULLY accomplished it
      - If you encounter errors, blockers, or cannot finish, keep the task as in_progress
      - When blocked, create a new task describing what needs to be resolved
      - Never mark a task as completed if:
        - Tests are failing
        - Implementation is partial
        - You encountered unresolved errors
        - You couldn't find necessary files or dependencies

      **Delete tasks:**
      - When a task is no longer relevant or was created in error
      - Setting status to `deleted` permanently removes the task

      **Update task details:**
      - When requirements change or become clearer
      - When establishing dependencies between tasks

      ## Fields You Can Update

      - **status**: The task status (see Status Workflow below)
      - **subject**: Change the task title (imperative form, e.g., "Run tests")
      - **description**: Change the task description
      - **activeForm**: Present continuous form shown in spinner when in_progress (e.g., "Running tests")
      - **owner**: Change the task owner (agent name)
      - **addBlocks**: Mark tasks that cannot start until this one completes
      - **addBlockedBy**: Mark tasks that must complete before this one can start

      ## Status Workflow

      Status progresses: `pending` → `in_progress` → `completed`

      Use `deleted` to permanently remove a task.

      ## Staleness

      Make sure to read a task's latest state using `TaskGet` before updating it.

      ## Examples

      Mark task as in progress when starting work:
      ```json
      {"taskId": "1", "status": "in_progress"}
      ```

      Mark task as completed after finishing work:
      ```json
      {"taskId": "1", "status": "completed"}
      ```

      Delete a task:
      ```json
      {"taskId": "1", "status": "deleted"}
      ```

      Claim a task by setting owner:
      ```json
      {"taskId": "1", "owner": "my-name"}
      ```

      Set up task dependencies:
      ```json
      {"taskId": "2", "addBlockedBy": ["1"]}
      ```
      """
  )
  public static String taskUpdate(@ToolRequest UpdateTaskRequest request, ToolContext toolContext) {
    Assert.notNull(request.getTaskId(), "Error: taskId is required");
    Assert.isTrue(StringUtils.isEmpty(request.getStatus()) ||
        GeneralAgentConsts.AGENT_TASK_STATUS_LIST.contains(request.getStatus()) ||
        GeneralAgentConsts.AGENT_TASK_STATUS_DELETED.equals(request.getStatus()),
      () -> "Error: Invalid status, allowed values: " + String.join(", ", GeneralAgentConsts.AGENT_TASK_STATUS_LIST) + ", " + GeneralAgentConsts.AGENT_TASK_STATUS_DELETED);
    ResultVO<Void> result;
    if (GeneralAgentConsts.AGENT_TASK_STATUS_DELETED.equals(request.getStatus())) {
      result = agentTaskService.deleteTask(toolContext.sessionId(), request.getTaskId());
    }
    else {
      result = agentTaskService.updateTask(request, toolContext.sessionId());
    }
    if (!result.isSuccess()) {
      throw new ToolExecutionException("Error: " + result.getResultMsg());
    }
    return "Updated task #%s".formatted(request.getTaskId());
  }

  @Tool(
    name = "task_list",
    description = """
      Use this tool to list all tasks in the task list.

      ## When to Use This Tool

      - To see what tasks are available to work on (status: 'pending', no owner, not blocked)
      - To check overall progress on the project
      - To find tasks that are blocked and need dependencies resolved
      - After completing a task, to check for newly unblocked work or claim the next available task
      - **Prefer working on tasks in ID order** (lowest ID first) when multiple tasks are available, as earlier tasks often set up context for later ones

      ## Output

      Returns a summary of each task:
      - **id**: Task identifier (use with task_get, task_update)
      - **subject**: Brief description of the task
      - **status**: 'pending', 'in_progress', or 'completed'
      - **owner**: Agent ID if assigned, empty if available
      - **blockedBy**: List of open task IDs that must be resolved first (tasks with blockedBy cannot be claimed until dependencies resolve)

      Use task_get with a specific task ID to view full details including description and comments.
      """
  )
  public static String taskList(ToolContext toolContext) {
    List<SessionTaskDTO> tasks = agentTaskService.listTasks(toolContext.sessionId());
    if (tasks.isEmpty()) {
      return "No tasks found";
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tasks.size(); i++) {
      SessionTaskDTO task = tasks.get(i);
      if (i > 0) {
        sb.append("\n");
      }
      sb.append("#").append(task.getTaskId());
      sb.append(" [").append(task.getTaskStatus()).append("] ");
      sb.append(task.getSubject());
      if (CollectionUtils.isNotEmpty(task.getBlockedBy())) {
        sb.append(" [blocked by ");
        sb.append(task.getBlockedBy().stream().map(t -> "#" + t).collect(Collectors.joining(", ")));
        sb.append("]");
      }
    }
    return sb.toString();
  }

  @Tool(
    name = "task_get",
    description = """
      Use this tool to retrieve a task by its ID from the task list.

      ## When to Use This Tool

      - When you need the full description and context before starting work on a task
      - To understand task dependencies (what it blocks, what blocks it)
      - After being assigned a task, to get complete requirements

      ## Output

      Returns full task details:
      - **subject**: Task title
      - **description**: Detailed requirements and context
      - **status**: 'pending', 'in_progress', or 'completed'
      - **blocks**: Tasks waiting on this one to complete
      - **blockedBy**: Tasks that must complete before this one can start

      ## Tips

      - After fetching a task, verify its blockedBy list is empty before beginning work.
      - Use task_list to see all tasks in summary form.
      """
  )
  public static String taskGet(@ToolParam Integer taskId, ToolContext toolContext) {
    Assert.notNull(taskId, "taskId is required");
    SessionTaskDTO task = agentTaskService.getTask(toolContext.sessionId(), taskId);
    if (task == null) {
      return "Task not found";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Task #").append(task.getTaskId()).append(": ").append(task.getSubject()).append("\n");
    sb.append("Status: ").append(task.getTaskStatus()).append("\n");
    sb.append("Description: ").append(task.getDescription()).append("\n");
    if (CollectionUtils.isNotEmpty(task.getBlockedBy())) {
      sb.append("Blocked By: ").append(task.getBlockedBy().stream().map(t -> "#" + t).collect(Collectors.joining(", "))).append("\n");
    }
    if (CollectionUtils.isNotEmpty(task.getBlocks())) {
      sb.append("Blocks: ").append(task.getBlocks().stream().map(t -> "#" + t).collect(Collectors.joining(", "))).append("\n");
    }
    return sb.toString();
  }

  /**
   * 获取任务提醒生成器
   */
  public static ReminderGenerator getTaskReminderGenerator(Long sessionId, @Nullable String contextId) {
    // contextId 过滤待与 SessionTask 服务对齐；当前先按 session 维度提醒，保证编译与主链路可用
    return new TaskReminderGenerator(sessionId);
  }

  /**
   * 任务提醒生成器
   *
   * <p>当大模型长时间未使用任务管理工具时，提醒大模型使用任务管理工具，并发送当前的任务列表</p>
   * <p>计数规则参考：task-reminder-implementation.md（仅统计非 thinking 的 assistant 轮次；task_create / task_update 与历史 task_reminder 为停止边界）</p>
   */
  @SuppressWarnings("ClassCanBeRecord")
  @RequiredArgsConstructor
  private static final class TaskReminderGenerator implements ReminderGenerator {
    private final Long sessionId;

    @Override
    @Nullable
    public SystemReminder generate(List<MemoryMessage> messages, Message message) {
      if (messages.isEmpty() || !shouldAddReminder(messages)) {
        return null;
      }
      List<SessionTaskDTO> tasks = agentTaskService.listTasks(sessionId);
      StringBuilder sb = new StringBuilder();
      sb.append("<system-reminder>\n");
      sb.append(TASK_REMINDER_INTRO);
      if (!tasks.isEmpty()) {
        sb.append("\n\n");
        sb.append("Here are the existing tasks:");
        for (SessionTaskDTO task : tasks) {
          sb.append('\n');
          sb.append("#").append(task.getTaskId());
          sb.append(". [").append(task.getTaskStatus()).append("] ");
          sb.append(task.getSubject());
        }
      }
      sb.append("\n</system-reminder>");
      return new SystemReminder(SystemReminderType.TASK_REMINDER, sb.toString());
    }

    /**
     * 检查是否需要添加任务提醒
     */
    private boolean shouldAddReminder(List<MemoryMessage> messages) {
      // 上次使用任务管理工具距今的对话轮数（只算 assistant 消息）
      int turnsSinceTaskManagement = 0;
      // 上次发送任务提醒距今的对话轮数（只算 assistant 消息）
      int turnsSinceReminder = 0;

      for (int i = messages.size() - 1; i >= 0; i--) {
        if (messages.get(i).message() instanceof AssistantMessage assistantMessage) {
          if (hasTaskManagementToolUsage(assistantMessage)) {
            break;
          }
          turnsSinceTaskManagement++;
        }
      }
      for (int i = messages.size() - 1; i >= 0; i--) {
        MemoryMessage memoryMessage = messages.get(i);
        List<SystemReminder> reminders = memoryMessage.reminders();
        if (CollectionUtils.isNotEmpty(reminders) && reminders.stream().anyMatch(r -> r.type() == SystemReminderType.TASK_REMINDER)) {
          break;
        }
        if (memoryMessage.message() instanceof AssistantMessage) {
          turnsSinceReminder++;
        }
      }
      return turnsSinceTaskManagement > TASK_REMINDER_TURNS_SINCE_WRITE && turnsSinceReminder > TASK_REMINDER_TURNS_BETWEEN_REMINDERS;
    }

    /**
     * 检查是否使用了任务管理工具
     */
    private boolean hasTaskManagementToolUsage(AssistantMessage message) {
      if (CollectionUtils.isNotEmpty(message.getToolCalls())) {
        for (ToolCall toolCall : message.getToolCalls()) {
          if (toolCall.getFunction() != null && TASK_TOOL_NAMES.contains(toolCall.getFunction().getName())) {
            return true;
          }
        }
      }
      else if (message.getFunctionCall() != null) {
        return TASK_TOOL_NAMES.contains(message.getFunctionCall().getName());
      }
      return false;
    }
  }
}
