# 洛神系统 Demo - 基于 AgentScope-Java 的多智能体协作演示

## 项目概述

本项目是基于 [AgentScope-Java](https://github.com/agentscope-ai/agentscope-java) 框架构建的洛神系统 AI 应用平台 Demo，展示了多智能体协作架构的实现方式。

### 架构设计

洛神系统采用三级智能体架构：

```
┌─────────────────────────────────────────────────────────────┐
│                    Leader/Master Agent                       │
│                      (总控智能体)                              │
│  - 接收用户请求                                               │
│  - 分析任务类型                                               │
│  - 分发给子智能体                                             │
│  - 汇总结果                                                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Core Agent                              │
│                      (设备智能体)                              │
│  - 设备状态查询                                               │
│  - 设备操作控制                                               │
│  - 设备故障诊断                                               │
│  - 协调 Sub Agent                                            │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
┌─────────────────────────┐   ┌─────────────────────────┐
│     Sub Agent           │   │     Sub Agent           │
│   (质量智能体)           │   │   (物料智能体)           │
│ - 质量检测               │   │ - 库存查询               │
│ - 问题分析               │   │ - 采购管理               │
│ - 质量报告               │   │ - 出入库记录             │
└─────────────────────────┘   └─────────────────────────┘
```

## 项目结构

```
luoshen-demo/
├── pom.xml                          # 父 POM，管理依赖版本
├── README.md                        # 项目文档
├── start.sh                         # 启动脚本
├── test.sh                          # 测试脚本
│
├── luoshen-admin/                   # 管理后台模块 ⭐ 新增
│   ├── pom.xml
│   └── src/main/
│       ├── java/io/luoshen/admin/
│       │   ├── LuoshenAdminApplication.java  # 主应用
│       │   ├── model/              # 数据模型
│       │   │   ├── AgentConfigEntity.java
│       │   │   ├── SkillConfigEntity.java
│       │   │   ├── McpConfigEntity.java
│       │   │   ├── SessionConfigEntity.java
│       │   │   └── MemoryConfigEntity.java
│       │   ├── repository/         # 数据访问层
│       │   ├── service/            # 业务服务层
│       │   │   ├── AgentDynamicService.java
│       │   │   ├── SkillDynamicService.java
│       │   │   ├── McpDynamicService.java
│       │   │   ├── SessionManagementService.java
│       │   │   └── MemoryManagementService.java
│       │   └── controller/         # REST API
│       │       ├── AgentManagementController.java
│       │       ├── SkillManagementController.java
│       │       ├── McpManagementController.java
│       │       ├── SessionManagementController.java
│       │       └── MemoryManagementController.java
│       └── resources/
│           ├── application.yml     # 配置文件
│           └── static/
│               └── index.html      # Web 管理界面
│
├── luoshen-core/                    # 核心模块
│   ├── pom.xml
│   └── src/main/java/io/luoshen/core/
│       ├── spec/
│       │   ├── AgentSpec.java       # Agent 规格定义
│       │   └── AgentFactory.java    # Agent 工厂类
│       └── config/
│           ├── LuoshenModelConfig.java    # 模型配置
│           ├── LuoshenMemoryConfig.java   # 记忆配置
│           └── LuoshenSessionConfig.java  # 会话配置
│
├── luoshen-leader-agent/            # Leader Agent 模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/io/luoshen/leader/
│       │   ├── LeaderAgentApplication.java  # 主应用
│       │   └── config/
│       │       └── LeaderAgentConfig.java   # Agent 配置
│       └ resources/agents/
│           └── leader-agent.md      # Agent 定义文件
│
├── luoshen-device-agent/            # Device Agent 模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/io/luoshen/device/
│       │   ├── DeviceAgentApplication.java  # 主应用
│       │   ├── config/
│       │   │   └── DeviceAgentConfig.java   # Agent 配置
│       │   └ tools/
│       │       └── DeviceTools.java         # 设备工具类
│       └ resources/agents/
│           └── device-agent.md      # Agent 定义文件
│
├── luoshen-quality-agent/           # Quality Agent 模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/io/luoshen/quality/
│       │   ├── QualityAgentApplication.java # 主应用
│       │   ├── config/
│       │   │   └── QualityAgentConfig.java  # Agent 配置
│       │   └ tools/
│       │       └── QualityTools.java        # 质量工具类
│       └ resources/agents/
│           └── quality-agent.md     # Agent 定义文件
│
└── luoshen-material-agent/          # Material Agent 模块
    ├── pom.xml
    └── src/main/
        ├── java/io/luoshen/material/
        │   ├── MaterialAgentApplication.java # 主应用
        │   ├── config/
        │   │   └── MaterialAgentConfig.java  # Agent 配置
        │   └ tools/
        │       └── MaterialTools.java        # 物料工具类
        └ resources/agents/
            └── material-agent.md    # Agent 定义文件
```

## 核心概念详解

### 1. ReActAgent - 智能体核心

`ReActAgent` 是 AgentScope-Java 的核心智能体类，采用 ReAct（推理-行动）范式：

```java
ReActAgent agent = ReActAgent.builder()
    .name("agent-name")              // 智能体名称
    .description("描述")              // 智能体描述
    .sysPrompt("系统提示")            // 系统提示词
    .model(model)                    // LLM 模型
    .toolkit(toolkit)                // 工具集
    .skillBox(skillBox)              // 技能箱
    .memory(memory)                  // 记忆管理
    .build();
```

**关键参数说明：**

| 参数 | 说明 | 必填 |
|------|------|------|
| `name` | 智能体唯一标识符 | ✓ |
| `sysPrompt` | 系统提示词，定义智能体行为 | ✓ |
| `model` | LLM 模型实例 | ✓ |
| `toolkit` | 工具集，包含可调用的工具 | - |
| `skillBox` | 技能箱，包含可用的技能 | - |
| `memory` | 记忆管理，存储对话历史 | - |

### 2. 动态配置 Agent

#### 方式一：通过 AgentSpec 动态创建

```java
// 定义 Agent 规格
AgentSpec spec = AgentSpec.of(
    "dynamic-agent",
    "动态创建的智能体",
    "你是一个动态创建的智能体...",
    List.of("tool1", "tool2"),       // 工具列表
    List.of("skill1")                 // 技能列表
);

// 使用 AgentFactory 创建
AgentFactory factory = AgentFactory.builder()
    .model(model)
    .defaultToolsByName(toolMap)
    .defaultSkillsByName(skillMap)
    .build();

ReActAgent agent = factory.create(spec);
```

#### 方式二：通过 Markdown 文件定义

```markdown
---
name: device-agent
description: 设备管理智能体
---

你是设备管理智能体...

## 可用工具
- query_device_status
- control_device
```

加载方式：

```java
FileSystemSkillRepository repository = 
    new FileSystemSkillRepository(Path.of("agents/"), false);
AgentSkill skill = repository.getSkill("device-agent");
```

### 3. Skill 系统

#### Skill 定义结构

```
skills/
└── my-skill/
    ├── SKILL.md              # 技能定义文件（必需）
    └── references/           # 参考文档（可选）
        ├── guide.md
        └── examples.md
```

#### SKILL.md 格式

```markdown
---
name: skill-name
description: 技能描述
---

# 技能说明

详细描述技能的功能和使用方式...

## 使用指南

1. 步骤一
2. 步骤二
```

#### 注册和使用 Skill

```java
// 创建 SkillBox
SkillBox skillBox = new SkillBox(toolkit);

// 注册技能
skillBox.registration()
    .skill(skill)
    .apply();

// 启用代码执行
skillBox.codeExecution()
    .workDir("/path/to/work")
    .withShell(shellTool)
    .withRead()
    .withWrite()
    .enable();

// 创建 Agent 时绑定 SkillBox
ReActAgent agent = ReActAgent.builder()
    .skillBox(skillBox)
    .build();
```

### 4. MCP 工具集成

MCP (Model Context Protocol) 是一种标准化的工具协议：

```java
// MCP 工具示例
@Tool(name = "mcp_tool", description = "MCP 工具描述")
public String mcpTool(
    @ToolParam(name = "param1", description = "参数描述") String param1
) {
    // 工具实现
    return "结果";
}

// 注册到 Toolkit
Toolkit toolkit = new Toolkit();
toolkit.registerTool(new McpTools());
```

### 5. Session 会话管理

#### JsonSession - JSON 文件存储

```java
// 创建 Session
Path sessionPath = Path.of(System.getProperty("user.home"), 
    ".luoshen", "sessions");
Session session = new JsonSession(sessionPath);

// 加载会话
if (session.exists(SimpleSessionKey.of(sessionId))) {
    agent.loadFrom(session, sessionId);
}

// 保存会话
agent.saveTo(session, sessionId);

// 删除会话
session.delete(SimpleSessionKey.of(sessionId));
```

#### 会话管理流程

```
用户请求 → 检查 sessionId → 加载已有会话 → 处理请求 → 保存会话 → 返回响应
```

### 6. Memory 记忆管理

#### InMemoryMemory - 内存记忆

```java
// 创建内存记忆
InMemoryMemory memory = new InMemoryMemory();

// 获取历史消息
List<Msg> messages = memory.getMessages();

// 清除记忆
memory.clear();
```

#### 长期记忆（RAG）

```java
// 配置长期记忆（需要 Embedding 服务）
// 参考 HayStackRAGExample 或 PgVectorRAGExample
```

### 7. 多智能体协作

#### SubAgentProvider - 子智能体提供者

```java
@Bean("qualityAgentProvider")
public SubAgentProvider<ReActAgent> qualityAgentProvider() {
    return () -> ReActAgent.builder()
        .name("quality-agent")
        .sysPrompt(QUALITY_SYSTEM_PROMPT)
        .model(model)
        .toolkit(qualityToolkit())
        .memory(new InMemoryMemory())
        .build();
}
```

#### Pipeline 模式

```java
// 顺序执行
SequentialPipeline pipeline = new SequentialPipeline(
    leaderAgent, deviceAgent, qualityAgent
);

// 并行执行
FanoutPipeline fanout = new FanoutPipeline(
    List.of(deviceAgent, qualityAgent, materialAgent)
);
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- DashScope API Key（或其他 LLM API Key）

### 2. 配置 API Key

```bash
# 设置环境变量
export DASHSCOPE_API_KEY=your-api-key
```

或在 `application.yml` 中配置：

```yaml
luoshen:
  api-key: your-api-key
  model-name: qwen-max
```

### 3. 编译项目

```bash
cd luoshen-demo
mvn clean install
```

### 4. 启动服务

```bash
# 启动 Leader Agent（端口 8080）
java -jar luoshen-leader-agent/target/luoshen-leader-agent-1.0.0-SNAPSHOT.jar

# 启动 Device Agent（端口 8081）
java -jar luoshen-device-agent/target/luoshen-device-agent-1.0.0-SNAPSHOT.jar \
  --server.port=8081

# 启动 Quality Agent（端口 8082）
java -jar luoshen-quality-agent/target/luoshen-quality-agent-1.0.0-SNAPSHOT.jar \
  --server.port=8082

# 启动 Material Agent（端口 8083）
java -jar luoshen-material-agent/target/luoshen-material-agent-1.0.0-SNAPSHOT.jar \
  --server.port=8083
```

### 5. 测试接口

```bash
# 测试 Leader Agent
curl -X POST http://localhost:8080/api/leader/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "查询所有设备状态"}' \
  -G --data-urlencode "sessionId=test-session"

# 测试 Device Agent
curl -X POST http://localhost:8081/api/device/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "查询设备 device-001 的状态"}'

# 测试 Quality Agent
curl -X POST http://localhost:8082/api/quality/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "执行产品质量检测"}'

# 测试 Material Agent
curl -X POST http://localhost:8083/api/material/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "查询所有物料库存"}'
```

## 工具详解

### DeviceTools - 设备管理工具

| 工具名称 | 功能 | 参数 |
|----------|------|------|
| `query_device_status` | 查询设备状态 | `device_id` |
| `query_all_devices` | 查询所有设备 | - |
| `control_device` | 控制设备 | `device_id`, `action`, `params` |
| `diagnose_device` | 故障诊断 | `device_id`, `symptom` |
| `generate_device_report` | 生成报告 | `device_id`（可选） |

### QualityTools - 质量管理工具

| 工具名称 | 功能 | 参数 |
|----------|------|------|
| `inspect_quality` | 执行质量检测 | `product_id`, `inspection_type` |
| `analyze_quality_issue` | 分析质量问题 | `issue_description`, `product_id` |
| `generate_quality_report` | 生成质量报告 | `period` |
| `query_quality_standards` | 查询质量标准 | `category`（可选） |

### MaterialTools - 物料管理工具

| 工具名称 | 功能 | 参数 |
|----------|------|------|
| `query_material_inventory` | 查询物料库存 | `material_id` |
| `query_all_materials` | 查询所有物料 | - |
| `request_material_purchase` | 申请采购 | `material_id`, `quantity`, `reason` |
| `query_purchase_requests` | 查询采购申请 | `status` |
| `record_material_transaction` | 记录出入库 | `material_id`, `type`, `quantity`, `reason` |
| `generate_material_report` | 生成物料报告 | `report_type` |

## 扩展指南

### 添加新的智能体

1. 创建新模块：

```bash
mkdir -p luoshen-new-agent/src/main/java/io/luoshen/new/{config,tools}
mkdir -p luoshen-new-agent/src/main/resources/agents
```

2. 创建工具类：

```java
public class NewTools {
    @Tool(name = "new_tool", description = "新工具")
    public String newTool(@ToolParam(name = "param") String param) {
        return "结果";
    }
}
```

3. 创建配置类：

```java
@Configuration
public class NewAgentConfig {
    @Bean
    public Toolkit newToolkit() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new NewTools());
        return toolkit;
    }
    
    @Bean("newAgent")
    public ReActAgent newAgent(Toolkit newToolkit) {
        return ReActAgent.builder()
            .name("new-agent")
            .sysPrompt("...")
            .model(luoshenModel)
            .toolkit(newToolkit)
            .memory(new InMemoryMemory())
            .build();
    }
}
```

4. 创建主应用类和 Markdown 定义文件

5. 在父 POM 中添加模块：

```xml
<modules>
    <module>luoshen-new-agent</module>
</modules>
```

### 添加新的工具

1. 在工具类中添加方法：

```java
@Tool(name = "new_tool", description = "工具描述")
public String newTool(
    @ToolParam(name = "param1", description = "参数1") String param1,
    @ToolParam(name = "param2", description = "参数2", required = false) String param2
) {
    // 实现逻辑
    return "结果";
}
```

2. 工具会自动注册到 Toolkit

### 添加新的技能

1. 创建技能目录：

```bash
mkdir -p skills/new-skill/references
```

2. 创建 SKILL.md：

```markdown
---
name: new-skill
description: 新技能描述
---

# 技能说明

详细描述...
```

3. 加载技能：

```java
FileSystemSkillRepository repository = 
    new FileSystemSkillRepository(Path.of("skills/"), false);
AgentSkill skill = repository.getSkill("new-skill");
```

## 管理后台（luoshen-admin）

### 功能概述

管理后台提供**动态配置管理**能力，所有配置存储在数据库中，修改后**即时生效**，无需重启服务。

### 核心能力

| 管理项 | 功能 | API 路径 |
|--------|------|----------|
| **Agent 管理** | 创建/编辑/删除/启用禁用 Agent | `/api/admin/agents` |
| **Skill 管理** | 创建/编辑/删除 Skill，热加载 | `/api/admin/skills` |
| **MCP 管理** | 注册/配置/启用禁用 MCP 工具 | `/api/admin/mcp` |
| **Session 管理** | 查看/删除会话，清理过期会话 | `/api/admin/sessions` |
| **Memory 管理** | 查看/搜索/删除记忆 | `/api/admin/memories` |

### 启动管理后台

```bash
# 设置 API Key
export DASHSCOPE_API_KEY=your-api-key

# 启动管理后台（端口 9090）
java -jar luoshen-admin/target/luoshen-admin-1.0.0-SNAPSHOT.jar

# 访问管理界面
open http://localhost:9090
```

### API 示例

#### 创建 Agent

```bash
curl -X POST http://localhost:9090/api/admin/agents \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "my-agent",
    "name": "我的智能体",
    "type": "sub",
    "description": "自定义智能体",
    "systemPrompt": "你是一个智能助手...",
    "enabled": true
  }'
```

#### 更新 Agent（即时生效）

```bash
curl -X PUT http://localhost:9090/api/admin/agents/my-agent \
  -H "Content-Type: application/json" \
  -d '{
    "name": "更新后的智能体",
    "systemPrompt": "新的系统提示词..."
  }'
```

#### 刷新所有 Agent

```bash
curl -X POST http://localhost:9090/api/admin/agents/refresh
```

### 数据库配置

默认使用 H2 嵌入式数据库，可切换为 MySQL/PostgreSQL：

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/luoshen
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Web 管理界面

访问 `http://localhost:9090` 即可使用可视化界面管理：

- 📊 统计面板：显示 Agent/Skill/MCP/Session 数量
- 🤖 Agent 管理：创建、编辑、删除、启用/禁用
- 📝 Skill 管理：创建、编辑、删除技能
- 🔧 MCP 管理：注册、配置 MCP 工具
- 💬 Session 管理：查看、删除会话
- 🧠 Memory 管理：查看、搜索、删除记忆

---

## 最佳实践

### 1. Agent 设计原则

- **单一职责**：每个 Agent 只负责一个领域
- **明确边界**：清晰定义 Agent 的职责范围
- **合理分层**：Leader → Core → Sub 三级架构
- **工具精简**：只注册必要的工具

### 2. 工具设计原则

- **功能明确**：每个工具只做一件事
- **参数清晰**：参数命名和描述要准确
- **返回规范**：返回格式化的文本结果
- **错误处理**：提供明确的错误信息

### 3. Session 管理

- **及时保存**：每次交互后保存会话
- **合理清理**：定期清理过期会话
- **隔离存储**：不同用户使用不同 sessionId

### 4. Memory 管理

- **短期记忆**：使用 InMemoryMemory 存储对话历史
- **长期记忆**：使用 RAG 存储重要信息
- **定期清理**：避免记忆过长影响性能

## 参考资源

- [AgentScope-Java 官方文档](https://java.agentscope.io/zh/intro.html)
- [AgentScope-Java GitHub](https://github.com/agentscope-ai/agentscope-java)
- [AgentScope 论文](https://arxiv.org/abs/2402.14034)

## 许可证

Apache License 2.0

---

**作者：** 洛神系统开发团队  
**更新时间：** 2026-03-30