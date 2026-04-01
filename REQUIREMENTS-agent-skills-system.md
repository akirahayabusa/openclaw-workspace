# Agent Skills 系统需求文档

## 需求概述

为 OpenClaw Agent 系统设计一个可扩展的技能管理架构，支持技能包的上传、分发和多层 Agent 的技能配置。

## 核心需求

### 1. 技能包上传与管理

#### 1.1 技能包格式
- **格式**：ZIP 压缩包（.skill 或 .zip）
- **结构**：
  ```
  my-skill.skill (ZIP)
  ├── SKILL.md           # 必需：技能定义文件
  ├── scripts/           # 可选：可执行脚本
  │   ├── script.py
  │   └── setup.sh
  ├── references/        # 可选：参考文档
  │   └── api-docs.md
  └── assets/           # 可选：资源文件
      └── template.png
  ```

#### 1.2 上传流程
1. 用户通过接口上传 .skill 压缩包
2. 系统自动解压到指定目录
3. 验证 SKILL.md 格式和必需字段
4. 提取技能元数据（name, description）
5. 注册到技能索引

#### 1.3 存储位置
```
~/.openclaw/
├── skills/
│   ├── builtin/         # 内置技能
│   │   └── github/
│   ├── user/            # 用户自定义技能
│   │   └── my-skill/
│   └── index.json       # 技能索引文件
```

#### 1.4 索引文件格式
```json
{
  "skills": [
    {
      "id": "my-skill",
      "name": "My Skill",
      "description": "Does something cool",
      "path": "/root/.openclaw/skills/user/my-skill",
      "installed_at": "2026-04-01T09:00:00+08:00",
      "version": "1.0.0",
      "source": "upload"
    }
  ],
  "last_updated": "2026-04-01T09:00:00+08:00"
}
```

### 2. 多层 Agent 技能配置

#### 2.1 Agent 层级
```
Main Agent (主会话)
├── Sub-Agent 1 (子代理 1)
│   ├── Sub-Agent 1.1
│   └── Sub-Agent 1.2
└── Sub-Agent 2 (子代理 2)
    └── Sub-Agent 2.1
```

#### 2.2 技能继承规则
- **默认行为**：子 Agent 继承父 Agent 的技能
- **可配置**：每个 Agent 可以指定自己的技能列表
- **技能隔离**：不同 Agent 的技能配置互不影响

#### 2.3 Agent 技能配置
```json
{
  "agent_id": "sub-agent-1",
  "agent_type": "subagent",
  "skills": {
    "mode": "custom",  // "inherit" | "custom" | "none"
    "allowed": [
      "my-skill",
      "another-skill",
      "builtin:github"
    ],
    "denied": [
      "dangerous-skill"
    ]
  }
}
```

#### 2.4 创建 Agent 时指定技能
```bash
# 通过 sessions_spawn 创建时指定
sessions_spawn \
  task="Do something" \
  runtime="subagent" \
  skills="my-skill,another-skill"
```

## 技术实现方案

### 3.1 技能包管理器

#### 上传接口
```python
class SkillManager:
    def upload_skill(self, skill_file: bytes, skill_id: str) -> dict:
        """
        上传并安装技能包
        
        Args:
            skill_file: ZIP 文件内容
            skill_id: 技能标识符
            
        Returns:
            安装结果和技能元数据
        """
        # 1. 验证 ZIP 格式
        # 2. 解压到临时目录
        # 3. 验证 SKILL.md
        # 4. 移动到目标位置
        # 5. 更新索引
        pass
    
    def list_skills(self, source: str = None) -> list:
        """列出所有可用技能"""
        pass
    
    def get_skill(self, skill_id: str) -> dict:
        """获取技能详情"""
        pass
    
    def delete_skill(self, skill_id: str) -> bool:
        """删除技能"""
        pass
```

#### 技能验证器
```python
class SkillValidator:
    def validate_skill_package(self, zip_path: str) -> tuple[bool, list]:
        """
        验证技能包
        
        Returns:
            (是否有效, 错误列表)
        """
        # 检查必需文件
        # 验证 YAML frontmatter
        # 检查脚本权限
        pass
```

### 3.2 Agent 技能配置

#### 配置存储
```
~/.openclaw/
├── agents/
│   ├── config.json          # 全局配置
│   └── profiles/
│       ├── main.json
│       ├── sub-agent-1.json
│       └── sub-agent-2.json
```

#### 配置文件示例
```json
{
  "agent_id": "main",
  "skills": {
    "mode": "custom",
    "allowed": ["*"],
    "denied": []
  }
}
```

```json
{
  "agent_id": "coding-agent",
  "skills": {
    "mode": "custom",
    "allowed": [
      "github",
      "obsidian",
      "skill-creator"
    ],
    "denied": []
  }
}
```

### 3.3 技能加载器

#### 扫描逻辑
```python
class SkillLoader:
    def load_skills_for_agent(self, agent_config: dict) -> list:
        """
        根据 Agent 配置加载技能
        
        Args:
            agent_config: Agent 技能配置
            
        Returns:
            可用技能列表
        """
        if agent_config["mode"] == "inherit":
            return self._get_parent_skills()
        elif agent_config["mode"] == "custom":
            return self._load_custom_skills(agent_config["allowed"])
        else:
            return []
    
    def _load_custom_skills(self, skill_list: list) -> list:
        """加载指定技能"""
        skills = []
        for skill_id in skill_list:
            skill = self._find_skill(skill_id)
            if skill:
                skills.append(skill)
        return skills
```

## 用户交互流程

### 4.1 上传技能
```
用户: "上传一个新技能"
AI: "请发送 .skill 压缩包"
用户: [上传文件]
AI: "✅ 技能已安装：my-skill v1.0.0"
```

### 4.2 配置 Agent 技能
```
用户: "创建一个只有 GitHub 技能的子 Agent"
AI: "正在创建 Agent..."
AI: "✅ Agent 已创建，技能：[github]"
```

### 4.3 查看技能列表
```
用户: "列出所有可用技能"
AI: "📋 可用技能：
  - github (内置)
  - my-skill (用户) v1.0.0
  - coding-agent (内置)"
```

## 安全考虑

### 5.1 技能沙箱
- 脚本执行前进行安全检查
- 禁止访问敏感路径
- 限制系统调用

### 5.2 权限控制
- 技能标记风险等级（low/medium/high）
- 高风险技能需要显式授权
- 子 Agent 默认限制危险技能

### 5.3 验证和审计
- 上传时验证 SKILL.md 格式
- 记录所有技能安装/卸载操作
- 定期审计已安装技能

## 实现优先级

### P0 (核心功能)
1. 技能包上传和解析
2. 技能索引管理
3. Agent 技能配置存储

### P1 (增强功能)
1. 技能验证和安全检查
2. 技能继承机制
3. CLI 命令支持

### P2 (高级功能)
1. 技能依赖管理
2. 技能版本控制
3. 技能市场集成

## 备注

- 需要修改现有 sessions_spawn 接口，支持 skills 参数
- 需要扩展 agents_list，显示 Agent 技能配置
- 需要创建技能管理 CLI 命令
- 需要更新文档，说明技能包格式

---

**创建时间**: 2026-04-01
**状态**: 需求定义中
