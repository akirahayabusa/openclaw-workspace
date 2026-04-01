# 洛神平台技能系统 - 用户手册

## 目录

1. [系统概述](#系统概述)
2. [快速开始](#快速开始)
3. [技能管理](#技能管理)
4. [Agent 技能绑定](#agent-技能绑定)
5. [API 参考](#api-参考)
6. [常见问题](#常见问题)
7. [故障排除](#故障排除)

---

## 系统概述

洛神平台技能系统是一个多智能体协作平台的技能管理模块，支持：

- 📦 **技能包上传** - 上传包含脚本和资源的 .skill 压缩包
- 📝 **纯文本技能** - 创建只包含 Markdown 内容的技能
- 🏷️️ **灵活分类** - 分类 + 标签 + 自定义类型
- 🎯 **Agent 绑定** - 为不同的 Agent 配置不同的技能
- 🔒 **安全检查** - 自动检测危险脚本和敏感文件

### 核心概念

#### 技能（Skill）

技能是 Agent 可以使用的能力单元，包含：

- **SKILL.md** - 技能定义文件（必需）
- **脚本** - 可执行的 Python/Shell 脚本（可选）
- **资源** - 图片、模板等文件（可选）
- **文档** - 参考文档（可选）

#### Agent（智能体）

Agent 是使用技能执行任务的程序，洛神平台支持三级架构：

- **Leader** - 顶层主控 Agent
- **Core** - 核心业务 Agent
- **Sub** - 专项功能 Agent

#### 技能绑定模式

- **inherit** - 继承父级或所有启用的技能
- **custom** - 只加载指定的技能列表
- **none** - 禁用所有技能

---

## 快速开始

### 1. 启动服务

```bash
cd luoshen-demo
mvn clean install
cd luoshen-admin
mvn spring-boot:run
```

### 2. 访问管理后台

打开浏览器访问：
```
http://localhost:9090/
```

### 3. 首页功能

- 📊 查看系统统计
- 📋 技能管理入口
- 🎯 Agent 绑定入口
- 📖 API 文档链接

---

## 技能管理

### 上传技能包

#### 方式 1：Web 界面上传

1. 访问 `http://localhost:9090/skills/upload`
2. 拖拽 `.skill` 或 `.zip` 文件到上传区域
3. （可选）填写或修改元数据：
   - 技能 ID
   - 技能名称
   - 技能描述
   - 分类
   - 标签（JSON 数组）
4. 点击"确认创建"

#### 方式 2：API 上传

```bash
curl -X POST http://localhost:9090/api/admin/skills/upload \
  -F "file=@github.skill" \
  -F "skillId=github" \
  -F "name=GitHub 集成" \
  -F "description=用于 GitHub 操作" \
  -F "category=dev" \
  -F 'tags=["github","git","api"]'
```

### 创建纯文本技能

#### 适合场景
- 提示词模板
- 工作流程指导
- API 使用说明

#### 创建方式

```bash
curl -X POST http://localhost:9090/api/admin/skills/create \
  -H "Content-Type: application/json" \
  -d '{
    "skillId": "prompt-template",
    "name": "提示词模板",
    "description": "用于生成提示词",
    "content": "# 提示词模板\n\n这是一个模板",
    "category": "dev",
    "riskLevel": "low"
  }'
```

### 管理技能

#### 查看技能列表

访问：`http://localhost:9090/skills`

**功能：**
- 📄 分页浏览所有技能
  - 默认每页 20 条
  - 支持翻页导航
  
- 🔍 搜索技能
  - 按名称或 ID 搜索
  - 实时结果展示
  
- 🏷️️ 筛选技能
  - 按分类筛选
  - 按来源筛选（内置/用户）
  - 按状态筛选（启用/禁用）
  
- ⚙️ 操作技能
  - 查看详情
  - 启用/禁用
  - 删除技能

#### 技能状态

| 状态 | 说明 | 建议 |
|------|------|------|
| ✓ 启用 | 技能可用，Agent 可以使用 | 正常状态 |
| ✗ 禁用 | 技能已禁用，Agent 不可使用 | 临时禁用或测试中 |

#### 搜索技巧

- **按关键词搜索**：输入技能名称或 ID
- **组合筛选**：分类 + 来源 + 状态
- **重置筛选**：点击"重置"按钮清除所有筛选条件

---

## Agent 技能绑定

### 绑定页面

访问：`http://localhost:9090/agents/skills`

### 绑定流程

#### 步骤 1：选择 Agent

从下拉菜单选择要配置的 Agent：
```
device-agent    # 设备管理 Agent
quality-agent  # 质量检测 Agent
material-agent # 物料管理 Agent
```

#### 步骤 2：绑定技能

**方式 1：双击绑定**
- 在左侧"可用技能"列表中
- 双击技能项即可绑定

**方式 2：拖拽绑定**
- 按住技能项拖动到右侧
- 释放鼠标完成绑定

**方式 3：批量配置**
- 点击"保存配置"按钮
- 在弹出窗口中选择多个技能
- 一次性批量绑定

#### 步骤 3：保存配置

点击"保存配置"按钮应用更改。

### 绑定模式说明

#### inherit（继承模式）

- **行为**：加载所有启用的技能
- **适用场景**：希望 Agent 可以访问所有技能
- **注意**：可能加载到不需要的技能

**示例：**
```json
{
  "mode": "inherit",
  "allowed": [],
  "denied": []
}
```

#### custom（自定义模式）

- **行为**：只加载 `allowed` 列表中的技能
- **适用场景**：精确控制 Agent 可以使用哪些技能
- **优势**：明确、可控

**示例：**
```json
{
  "mode": "custom",
  "allowed": ["github", "git"],
  "denied": []
}
```

#### none（禁用模式）

- **行为**：不加载任何技能
- **适用场景**：测试或禁用某个 Agent
- **注意**：Agent 将无法使用任何技能

**示例：**
```json
{
  "mode": "none",
  "allowed": [],
  " denied: []
}
```

### API 操作示例

#### 绑定单个技能

```bash
curl -X POST http://localhost:9090/api/admin/agents/device-agent/skills/bind \
  -H "Content-Type: application/json" \
  -d '{
    "skillId": "github",
    "bindMode": "custom",
    "priority": 0
  }'
```

#### 批量绑定

```bash
curl -X POST http://localhost:9090/api/admin/agents/device-agent/skills/batch \
  -H "Content-Type: application/json" \
  -d '{
    "skillIds": ["github", "git", "api"],
    "bindMode": "custom"
  }'
```

#### 设置技能配置

```bash
curl -X PUT http://localhost:9090/api/admin/agents/device-agent/skills/config \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "custom",
    "allowed": ["github", "git"],
    "denied": []
  }'
```

#### 解绑技能

```bash
curl -X DELETE http://localhost:9090/api/admin/agents/device-agent/skills/github
```

#### 查询 Agent 的技能

```bash
curl http://localhost:9090/api/admin/agents/device-agent/skills
```

---

## API 参考

### 技能管理 API

#### 1. 上传技能包

```http
POST /api/admin/skills/upload
Content-Type: multipart/form-data

参数：
- file: 技能包文件（必填）
- skillId: 技能 ID（可选）
- name: 技能名称（可选）
- description: 技能描述（可选）
- category: 主分类（可选）
- subCategory: 子分类（可选）
- tags: 标签（JSON 数组，可选）
- customType: 自定义类型（可选）
```

#### 2. 创建纯文本技能

```http
POST /api/admin/skills/create
Content-Type: application/json

{
  "skillId": "prompt-template",
  "name": "提示词模板",
  "description": "用于生成提示词",
  "content": "# 提示词模板\n\n这是一个模板",
  "category": "dev",
  "tags": ["prompt", "template"]
}
```

#### 3. 查询技能列表

```http
GET /api/admin/skills?category=dev&enabled=true&page=0&size=20
```

#### 4. 获取技能详情

```http
GET /api/admin/skills/{skillId}
```

#### 5. 更新技能

```http
PUT /api/admin/skills/{skillId}
Content-Type: application/json

{
  "name": "更新后的名称",
  "description": "更新后的描述"
}
```

#### 6. 删除技能

```http
DELETE /api/admin/skills/{skillId}
```

### Agent 技能绑定 API

#### 1. 绑定技能

```http
POST /api/admin/agents/{agentId}/skills/bind
Content-Type: application/json

{
  "skillId": "github",
  "bindMode": "custom",
  "priority": 0,
  "config": {}
}
```

#### 2. 解绑技能

```http
DELETE /api/admin/agents/{agentId}/skills/{skillId}
```

#### 3. 查询 Agent 的技能

```http
GET /api/admin/agents/{agentId}/skills
```

#### 4. 批量绑定

```http
POST /api/admin/agents/{agentId}/skills/batch
Content-Type: application/json

{
  "skillIds": ["github", "git"],
  "bindMode": "custom"
}
```

#### 5. 设置技能配置

```http
PUT /api/admin/agents/{agentId}/skills/config
Content-Type: application/json

{
  "mode": "custom",
  "allowed": ["github", "git"],
  "denied": []
}
```

#### 6. 删除所有绑定

```http
DELETE /api/admin/agents/{agentId}/skills
```

---

## 常见问题

### Q1: 技能包上传失败？

**可能原因：**
- 文件格式不对（必须是 .skill 或 .zip）
- 文件太大（最大 50MB）
- SKILL.md 缺失或格式错误
- 包含危险脚本被拦截

**解决方法：**
1. 检查文件格式和大小
2. 确保 SKILL.md 在根目录
3. 查看错误提示信息
4. 检查安全检查日志

### Q2: 如何创建纯文本技能？

**步骤：**
1. 访问 `http://localhost:9090/skills`
2. 点击"+ 新增"
3. 选择"手动填写内容"
4. 填写技能 ID、名称、描述、内容
5. 点击"确认创建"

### Q3: 分类和标签有什么区别？

| 类型 | 说明 | 示例 |
|------|------|------|
| **分类** | 层级结构，用于快速筛选 | dev → git |
| **标签** | 平面结构，可以多个 | ["github", "git", "api"] |
| **自定义类型** | 完全自由定义 | my-custom-type |

### Q4: 如何为不同的 Agent 配置不同的技能？

**步骤：**
1. 访问 `http://localhost:9090/agents/skills`
2. 选择要配置的 Agent
3. 从左侧"可用技能"拖拽到右侧"已绑定技能"
4. 点击"保存配置"

或者使用 API 设置不同的 `allowed` 列表。

### Q5: 继承模式和自定义模式有什么区别？

| 模式 | 行为 | 适用场景 |
|------|------|----------|
| **inherit** | 加载所有启用的技能 | 通用的 Agent |
| **custom** | 只加载指定的技能 | 专用 Agent |
| **none** | 不加载任何技能 | 测试或禁用 |

### Q6: 技能绑定后如何生效？

**步骤：**
1. 绑定技能到 Agent
2. 下次创建 Agent 时会自动加载绑定的技能
3. 如果 Agent 已在运行，需要重启或刷新配置

---

## 故障排除

### 问题 1：页面无法访问

**检查：**
- 服务是否启动：`ps aux | grep luoshen`
- 端口是否占用：`ss -tuln | grep 9090`
- 防火墙是否阻止

**解决：**
```bash
# 检查服务状态
curl http://localhost:9090/api/admin/skills

# 查看日志
tail -f /path/to/luoshen-admin.log
```

### 问题 2：技能绑定后 Agent 没有加载

**检查：**
1. 技能是否启用：`/skills` 页面查看
2. Agent 是否绑定了技能：`/agents/skills` 页面查看
3. 查看日志：`tail -f /path/to/luoshen-admin.log`

**解决：**
- 确保技能已启用
- 检查绑定模式（custom 模式需要技能在 allowed 列表中）
- 尝试重新创建 Agent

### 问题 3：上传技能包后找不到

**检查：**
1. 查看数据库中是否有记录：`GET /api/admin/skills/{skillId}`
2. 检查文件系统：`ls -la ~/.luoshen/skills/user/`
3. 查看日志：检查是否有错误信息

**解决：**
- 确认上传成功
- 刷新技能列表
- 清除浏览器缓存

### 问题 4：ZIP Slip 错误

**原因：**
- 技能包包含恶意文件路径

**解决：**
- 检查技能包内容
- 移除危险的 `../` 路径
- 重新打包上传

---

## 附录

### A. 技能包模板

下载完整模板：
```bash
curl -O https://example.com/skill-template.zip
```

### B. SKILL.md 模板

```markdown
---
name: My Skill
description: 这个技能用于...
---

# 技能说明

详细描述技能的功能...

## 使用指南

1. 前置条件
2. 操作步骤
3. 注意事项
```

### C. 配置文件说明

配置文件：`~/.luoshen/luoshen-admin/src/main/resources/application.yml`

```yaml
luoshen:
  skills:
    storage-path: ${user.home}/.luoshen/skills
    max-file-size: 52428800  # 50MB
    allowed-extensions: .skill,.zip
```

---

**文档版本：** v1.0
**更新时间：** 2026-04-01
**状态：** Phase 1-4 完成
