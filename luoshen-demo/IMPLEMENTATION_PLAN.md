# 洛神系统 Demo 完善计划

## 🎯 目标

基于现有的 luoshen-demo，完善管理平台功能，实现：
1. 完整的 Web 管理界面
2. 实时监控和日志
3. 生产级部署支持
4. 详细的文档

---

## 📊 当前状态评估

### ✅ 已完成（85%）

| 模块 | 完成度 | 说明 |
|------|--------|------|
| **Agent 管理** | ✅ 90% | 动态创建、修改、删除、缓存 |
| **Skill 管理** | ✅ 85% | Markdown 加载、数据库存储 |
| **MCP 管理** | ✅ 80% | 连接配置、动态注册 |
| **Session 管理** | ✅ 90% | JSON 存储、生命周期管理 |
| **Memory 管理** | ✅ 90% | 短期 + 长期记忆 |
| **三级架构** | ✅ 100% | Leader → Device → Quality/Material |
| **REST API** | ✅ 100% | 完整的管理接口 |
| **公网部署** | ✅ 100% | http://49.51.229.18:8080 |

### ⚠️ 待完善（15%）

| 功能 | 优先级 | 预计时间 |
|------|--------|----------|
| **Web 管理界面** | 高 | 5-7 天 |
| **实时日志查看** | 中 | 2-3 天 |
| **监控指标** | 中 | 2-3 天 |
| **Docker 部署** | 低 | 1-2 天 |
| **权限管理** | 低 | 2-3 天 |

---

## 🚀 阶段一：Web 管理界面（1周）

### 技术选型

**推荐：React + TypeScript + Ant Design Pro**

**原因：**
- ✅ Ant Design Pro 提供完整的管理后台模板
- ✅ TypeScript 类型安全
- ✅ 丰富的业务组件
- ✅ 与 Spring Boot 后端完美配合

### 页面规划

#### 1. Dashboard（首页）

**功能：**
- Agent 统计（总数、启用数、禁用数）
- Skill 统计（总数、已加载数）
- Session 统计（活跃会话数）
- Memory 使用情况
- 系统健康状态

**实现：**
```typescript
// Dashboard.tsx
import { Card, Row, Col, Statistic } from 'antd';
import { RobotOutlined, ToolOutlined, MessageOutlined } from '@ant-design/icons';

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState({
    agents: 0,
    skills: 0,
    sessions: 0,
    memoryUsage: 0
  });

  return (
    <Row gutter={16}>
      <Col span={6}>
        <Card>
          <Statistic
            title="Agent 总数"
            value={stats.agents}
            prefix={<RobotOutlined />}
          />
        </Card>
      </Col>
      {/* 其他统计卡片 */}
    </Row>
  );
};
```

#### 2. Agent 管理

**功能：**
- Agent 列表（表格展示）
- 创建 Agent（表单）
- 编辑 Agent（表单）
- 删除 Agent（确认对话框）
- 启用/禁用 Agent（开关）
- 查看详情（抽屉）
- 测试对话（模态框）

**实现：**
```typescript
// AgentList.tsx
import { Table, Button, Switch, Space } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

const AgentList: React.FC = () => {
  const columns = [
    { title: 'ID', dataIndex: 'agentId' },
    { title: '名称', dataIndex: 'name' },
    { title: '类型', dataIndex: 'type' },
    { title: '状态', dataIndex: 'enabled', render: (enabled: boolean) => (
      <Switch checked={enabled} onChange={() => toggleAgent(record.agentId)} />
    )},
    {
      title: '操作',
      render: (text: any, record: AgentConfig) => (
        <Space>
          <Button icon={<EditOutlined />} onClick={() => editAgent(record)}>编辑</Button>
          <Button icon={<DeleteOutlined />} danger onClick={() => deleteAgent(record.agentId)}>删除</Button>
          <Button type="primary" onClick={() => testAgent(record.agentId)}>测试</Button>
        </Space>
      )
    }
  ];

  return (
    <div>
      <Button type="primary" icon={<PlusOutlined />}>创建 Agent</Button>
      <Table columns={columns} dataSource={agents} />
    </div>
  );
};
```

#### 3. Skill 管理

**功能：**
- Skill 列表
- 上传 Markdown 文件
- 创建/编辑 Skill（Markdown 编辑器）
- 加载/卸载 Skill
- 预览 Skill 内容

**实现：**
```typescript
// SkillList.tsx
import { Upload, Button, Modal } from 'antd';
import { UploadOutlined, EyeOutlined } from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';

const SkillList: React.FC = () => {
  const [previewSkill, setPreviewSkill] = useState<AgentSkill | null>(null);

  return (
    <div>
      <Upload
        accept=".md"
        beforeUpload={handleSkillUpload}
        showUploadList={false}
      >
        <Button icon={<UploadOutlined />}>上传 Skill</Button>
      </Upload>

      <Table columns={columns} dataSource={skills} />

      <Modal
        title="Skill 预览"
        visible={!!previewSkill}
        onCancel={() => setPreviewSkill(null)}
        width={800}
      >
        <ReactMarkdown>{previewSkill?.content || ''}</ReactMarkdown>
      </Modal>
    </div>
  );
};
```

#### 4. Session 管理

**功能：**
- Session 列表（分页）
- 查看会话详情（消息历史）
- 删除会话
- 批量清理过期会话
- 搜索会话

**实现：**
```typescript
// SessionList.tsx
import { Table, Button, Input, DatePicker } from 'antd';
import { SearchOutlined, DeleteOutlined } from '@ant-design/icons';

const SessionList: React.FC = () => {
  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Input.Search placeholder="搜索会话" onSearch={handleSearch} />
        <DatePicker.RangePicker onChange={handleDateRangeChange} />
        <Button danger onClick={clearExpiredSessions}>清理过期会话</Button>
      </Space>

      <Table
        columns={columns}
        dataSource={sessions}
        expandable={{
          expandedRowRender: (record) => (
            <div>
              {record.messages.map((msg, index) => (
                <div key={index}>
                  <strong>{msg.role}:</strong> {msg.content}
                </div>
              ))}
            </div>
          )
        }}
      />
    </div>
  );
};
```

#### 5. MCP 管理

**功能：**
- MCP 服务器列表
- 添加/编辑 MCP 配置
- 连接/断开 MCP 服务器
- 查看可用工具
- 测试 MCP 工具

**实现：**
```typescript
// McpList.tsx
import { Table, Button, Badge } from 'antd';
import { LinkOutlined, DisconnectOutlined } from '@ant-design/icons';

const McpList: React.FC = () => {
  return (
    <div>
      <Button type="primary" onClick={showAddMcpModal}>添加 MCP 服务器</Button>

      <Table
        columns={[
          { title: '名称', dataIndex: 'name' },
          { title: '类型', dataIndex: 'transportType' },
          {
            title: '状态',
            dataIndex: 'connected',
            render: (connected: boolean) => (
              <Badge status={connected ? 'success' : 'error'} text={connected ? '已连接' : '未连接'} />
            )
          },
          {
            title: '操作',
            render: (text: any, record: McpConfig) => (
              <Space>
                <Button
                  icon={record.connected ? <DisconnectOutlined /> : <LinkOutlined />}
                  onClick={() => toggleMcpConnection(record)}
                >
                  {record.connected ? '断开' : '连接'}
                </Button>
                <Button onClick={() => viewMcpTools(record)}>查看工具</Button>
              </Space>
            )
          }
        ]}
        dataSource={mcpServers}
      />
    </div>
  );
};
```

### API 对接

**API 服务层：**
```typescript
// api.ts
import axios from 'axios';

const API_BASE = 'http://49.51.229.18:8080/api/admin';

export const agentApi = {
  list: () => axios.get(`${API_BASE}/agents`),
  get: (id: string) => axios.get(`${API_BASE}/agents/${id}`),
  create: (data: AgentConfig) => axios.post(`${API_BASE}/agents`, data),
  update: (id: string, data: AgentConfig) => axios.put(`${API_BASE}/agents/${id}`, data),
  delete: (id: string) => axios.delete(`${API_BASE}/agents/${id}`),
  toggle: (id: string) => axios.post(`${API_BASE}/agents/${id}/toggle`),
  test: (id: string, message: string) => axios.post(`${API_BASE}/agents/${id}/test`, { message }),
};

export const skillApi = {
  list: () => axios.get(`${API_BASE}/skills`),
  upload: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return axios.post(`${API_BASE}/skills/upload`, formData);
  },
  load: (name: string) => axios.post(`${API_BASE}/skills/${name}/load`),
  unload: (name: string) => axios.delete(`${API_BASE}/skills/${name}`),
};

export const sessionApi = {
  list: (params: any) => axios.get(`${API_BASE}/sessions`, { params }),
  get: (id: string) => axios.get(`${API_BASE}/sessions/${id}`),
  delete: (id: string) => axios.delete(`${API_BASE}/sessions/${id}`),
  clearExpired: () => axios.post(`${API_BASE}/sessions/clear-expired`),
};
```

### 项目结构

```
luoshen-admin-web/
├── public/
│   └── index.html
├── src/
│   ├── api/                 # API 接口
│   │   ├── agent.ts
│   │   ├── skill.ts
│   │   ├── session.ts
│   │   └── mcp.ts
│   ├── components/          # 通用组件
│   │   ├── Layout/
│   │   ├── Header/
│   │   └── Sider/
│   ├── pages/               # 页面组件
│   │   ├── Dashboard/
│   │   ├── AgentList/
│   │   ├── SkillList/
│   │   ├── SessionList/
│   │   └── McpList/
│   ├── models/              # 数据模型
│   │   ├── agent.ts
│   │   ├── skill.ts
│   │   └── session.ts
│   ├── utils/               # 工具函数
│   ├── App.tsx
│   └── index.tsx
├── package.json
└── tsconfig.json
```

---

## 📈 阶段二：实时监控和日志（3天）

### WebSocket 实时日志

**后端实现：**
```java
// AgentLogWebSocketHandler.java
@Component
public class AgentLogWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端请求（如订阅特定 Agent 的日志）
    }

    // 广播日志消息
    public void broadcastLog(String agentId, String logMessage) {
        String json = String.format("{\"agentId\":\"%s\",\"message\":\"%s\"}",
            agentId, logMessage);
        sessions.values().forEach(session -> {
            try {
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.error("发送日志失败", e);
            }
        });
    }
}
```

**前端实现：**
```typescript
// useAgentLogs.ts
import { useEffect, useState } from 'react';

export const useAgentLogs = (agentId?: string) => {
  const [logs, setLogs] = useState<LogMessage[]>([]);
  const [ws, setWs] = useState<WebSocket | null>(null);

  useEffect(() => {
    const websocket = new WebSocket('ws://49.51.229.18:8080/ws/logs');

    websocket.onmessage = (event) => {
      const log = JSON.parse(event.data);
      if (!agentId || log.agentId === agentId) {
        setLogs(prev => [...prev, log].slice(-100)); // 保留最近 100 条
      }
    };

    setWs(websocket);

    return () => websocket.close();
  }, [agentId]);

  return { logs, isConnected: ws?.readyState === WebSocket.OPEN };
};
```

### Prometheus 监控

**依赖：**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**自定义指标：**
```java
@Component
public class LuoshenMetrics {

    private final MeterRegistry meterRegistry;

    // Agent 调用计数
    private final Counter agentCallCounter;

    // Agent 执行时长
    private final Timer agentExecutionTimer;

    public LuoshenMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.agentCallCounter = Counter.builder("luoshen.agent.calls")
            .description("Agent 调用次数")
            .tag("agent", "")
            .register(meterRegistry);

        this.agentExecutionTimer = Timer.builder("luoshen.agent.execution.time")
            .description("Agent 执行时长")
            .register(meterRegistry);
    }

    public void recordAgentCall(String agentId, long durationMs) {
        agentCallCounter.increment();
        agentExecutionTimer.record(Duration.ofMillis(durationMs));
    }
}
```

---

## 🐳 阶段三：Docker 部署（1天）

### Dockerfile

```dockerfile
# luoshen-leader-agent/Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/luoshen-leader-agent-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  leader-agent:
    build: ./luoshen-leader-agent
    ports:
      - "8080:8080"
    environment:
      - DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}
      - SPRING_PROFILES_ACTIVE=docker
    volumes:
      - ./data/sessions:/app/sessions
    networks:
      - luoshen-network

  device-agent:
    build: ./luoshen-device-agent
    ports:
      - "8081:8081"
    environment:
      - DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}
    networks:
      - luoshen-network

  quality-agent:
    build: ./luoshen-quality-agent
    ports:
      - "8082:8082"
    environment:
      - DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}
    networks:
      - luoshen-network

  material-agent:
    build: ./luoshen-material-agent
    ports:
      - "8083:8083"
    environment:
      - DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}
    networks:
      - luoshen-network

networks:
  luoshen-network:
    driver: bridge
```

---

## 📝 阶段四：文档完善（1天）

### README 更新

需要补充：
1. Web 管理界面使用说明
2. Docker 部署指南
3. API 文档（Swagger）
4. 性能调优指南
5. 故障排查手册

### API 文档

使用 Swagger/Knife4j 自动生成：
```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-spring-boot-starter</artifactId>
    <version>4.1.0</version>
</dependency>
```

---

## 🎯 总结

### 当前 Demo 满足度：**85%**

**核心功能已完成：**
✅ Agent/Skill/MCP/Session/Memory 管理
✅ 三级智能体架构
✅ 动态配置即时生效
✅ REST API 接口
✅ 公网部署

**需要完善（15%）：**
⚠️ Web 管理界面（React）
⚠️ 实时日志监控（WebSocket）
⚠️ 性能指标监控（Prometheus）
⚠️ Docker 部署支持

### 建议行动

1. **不要重写** - 当前代码质量良好，保留并完善
2. **优先 Web 界面** - 这是最大的短板
3. **渐进增强** - 一步步添加功能，不要一次性重写
4. **测试验证** - 每个阶段完成后都要充分测试

### 时间估算

- Web 管理界面：5-7 天
- 实时监控：2-3 天
- Docker 部署：1-2 天
- 文档完善：1 天
- **总计：9-13 天**

---

**结论：现有 demo 是一个很好的基础，建议在现有代码上完善，而不是从头重写！**