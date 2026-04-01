# Agent Skills 系统设计方案

## 设计原则

1. **最小侵入**：尽量不修改 OpenClaw 核心代码，通过扩展实现
2. **向后兼容**：保持现有技能系统不变，新功能作为增强
3. **模块化**：各组件职责清晰，易于测试和维护
4. **安全优先**：所有外部输入都要验证和沙箱化

## 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        User Interface                       │
│  (Chat / CLI / API)                                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Skills Manager (Core)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Skill Upload │  │ Skill Index  │  │ Skill Loader │      │
│  │   Handler    │  │   Manager    │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────────┬────────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ ~/.openclaw/ │ │ ~/.openclaw/ │ │ ~/.openclaw/ │
│  skills/     │ │  agents/     │ │  cache/      │
│              │ │  profiles/   │ │              │
└──────────────┘ └──────────────┘ └──────────────┘
```

### 数据流

```
用户上传技能包
    │
    ▼
验证格式 (ZIP + SKILL.md)
    │
    ▼
解压到临时目录
    │
    ▼
验证内容 (安全性、格式)
    │
    ▼
移动到 skills/user/
    │
    ▼
更新索引 (index.json)
    │
    ▼
返回结果
```

## 核心组件设计

### 1. 技能管理器 (SkillManager)

#### 文件结构
```
~/.openclaw/extensions/skills-manager/
├── src/
│   ├── SkillManager.ts       # 核心管理类
│   ├── SkillValidator.ts     # 验证器
│   ├── SkillIndex.ts         # 索引管理
│   ├── SkillLoader.ts        # 加载器
│   └── types.ts              # 类型定义
├── bin/
│   └── skills-cli.ts         # CLI 工具
├── package.json
└── SKILL.md                  # 本身的技能文档
```

#### API 设计

```typescript
interface SkillMetadata {
  id: string;                    // 技能唯一标识
  name: string;                  // 技能名称
  description: string;           // 描述
  version: string;               // 版本号
  path: string;                  // 安装路径
  source: 'builtin' | 'user';    // 来源
  installed_at: string;          // 安装时间
  risk_level: 'low' | 'medium' | 'high';  // 风险等级
  dependencies?: string[];       // 依赖的其他技能
}

interface SkillIndex {
  skills: SkillMetadata[];
  last_updated: string;
}

class SkillManager {
  // 上传技能包
  async uploadSkill(
    file: Buffer | string,
    options?: {
      id?: string;
      override?: boolean;
    }
  ): Promise<SkillMetadata>;

  // 列出技能
  listSkills(filter?: {
    source?: 'builtin' | 'user';
    risk_level?: string;
  }): Promise<SkillMetadata[]>;

  // 获取技能详情
  getSkill(id: string): Promise<SkillMetadata | null>;

  // 删除技能
  deleteSkill(id: string): Promise<boolean>;

  // 验证技能包
  validateSkill(packagePath: string): Promise<{
    valid: boolean;
    errors: string[];
    warnings: string[];
  }>;

  // 更新索引
  rebuildIndex(): Promise<void>;
}
```

### 2. Agent 技能配置

#### 配置文件结构

```
~/.openclaw/agents/
├── config.json                    # 全局配置
├── profiles/                      # Agent 配置文件
│   ├── main.json                  # 主 Agent
│   ├── sub-agent-*.json           # 子 Agent（动态生成）
│   └── templates/                 # 配置模板
│       ├── default.json
│       ├── coding-agent.json
│       └── research-agent.json
└── hierarchy.json                 # Agent 层级关系
```

#### 配置格式

```json
{
  "agent_id": "main",
  "agent_type": "main",
  "skills": {
    "mode": "custom",
    "allowed": ["*"],
    "denied": []
  },
  "created_at": "2026-04-01T00:00:00Z",
  "updated_at": "2026-04-01T00:00:00Z"
}
```

```json
{
  "agent_id": "coding-agent-template",
  "agent_type": "template",
  "description": "专门的编程助手，只包含开发相关技能",
  "skills": {
    "mode": "custom",
    "allowed": [
      "github",
      "obsidian",
      "skill-creator",
      "agent-browser"
    ],
    "denied": [
      "wecom-*"  // 通配符：禁用所有企业微信相关技能
    ]
  }
}
```

### 3. 技能加载器 (SkillLoader)

#### 加载策略

```typescript
interface AgentSkillConfig {
  mode: 'inherit' | 'custom' | 'none';
  allowed?: string[];  // 支持 ['skill-id', 'builtin:github', 'user:*']
  denied?: string[];
}

class SkillLoader {
  // 为 Agent 加载技能
  async loadSkillsForAgent(
    agentId: string,
    config: AgentSkillConfig
  ): Promise<SkillMetadata[]> {
    switch (config.mode) {
      case 'inherit':
        return this.loadInheritSkills(agentId);
      case 'custom':
        return this.loadCustomSkills(config.allowed || [], config.denied || []);
      case 'none':
        return [];
    }
  }

  // 继承模式：递归获取父级技能
  private async loadInheritSkills(agentId: string): Promise<SkillMetadata[]> {
    const parentId = await this.getParentAgentId(agentId);
    if (!parentId) return this.loadAllSkills();
    const parentConfig = await this.getAgentConfig(parentId);
    return this.loadSkillsForAgent(parentId, parentConfig.skills);
  }

  // 自定义模式：按白名单/黑名单加载
  private async loadCustomSkills(
    allowed: string[],
    denied: string[]
  ): Promise<SkillMetadata[]> {
    const allSkills = await this.loadAllSkills();

    // 应用白名单
    const whitelisted = this.filterByPatterns(allSkills, allowed);

    // 应用黑名单
    const result = whitelisted.filter(skill =>
      !this.matchesPattern(skill.id, denied)
    );

    return result;
  }

  // 模式匹配（支持通配符）
  private matchesPattern(id: string, patterns: string[]): boolean {
    return patterns.some(pattern => {
      const regex = new RegExp(
        '^' + pattern.replace(/\*/g, '.*').replace(/\?/g, '.') + '$'
      );
      return regex.test(id);
    });
  }
}
```

### 4. 技能验证器 (SkillValidator)

#### 验证规则

```typescript
interface ValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
  metadata?: {
    name: string;
    description: string;
    version?: string;
  };
}

class SkillValidator {
  async validateSkill(packagePath: string): Promise<ValidationResult> {
    const result: ValidationResult = {
      valid: true,
      errors: [],
      warnings: []
    };

    // 1. 检查 ZIP 格式
    const isValidZip = await this.checkZipFormat(packagePath);
    if (!isValidZip) {
      result.errors.push('不是有效的 ZIP 文件');
      result.valid = false;
      return result;
    }

    // 2. 解压到临时目录
    const tempDir = await this.extractToTemp(packagePath);

    try {
      // 3. 检查必需文件
      const hasSkillMd = await this.checkRequiredFiles(tempDir);
      if (!hasSkillMd) {
        result.errors.push('缺少必需的 SKILL.md 文件');
        result.valid = false;
      }

      // 4. 验证 SKILL.md 格式
      const metadata = await this.parseSkillMetadata(tempDir);
      if (!metadata) {
        result.errors.push('SKILL.md 格式错误或缺少必需字段');
        result.valid = false;
      } else {
        result.metadata = metadata;
      }

      // 5. 安全检查
      const securityIssues = await this.checkSecurity(tempDir);
      if (securityIssues.length > 0) {
        result.errors.push(...securityIssues);
        result.valid = false;
      }

      // 6. 警告（不影响安装）
      const warnings = await this.checkWarnings(tempDir);
      result.warnings.push(...warnings);

    } finally {
      // 清理临时目录
      await this.cleanupTemp(tempDir);
    }

    return result;
  }

  // 安全检查
  private async checkSecurity(dir: string): Promise<string[]> {
    const issues: string[] = [];

    // 检查敏感文件
    const sensitiveFiles = [
      '../.ssh',
      '../.aws',
      '../token',
      '../password',
      '../secret'
    ];

    for (const pattern of sensitiveFiles) {
      const found = await this.scanForPattern(dir, pattern);
      if (found) {
        issues.push(`检测到敏感文件：${pattern}`);
      }
    }

    // 检查脚本中的危险操作
    const scripts = await this.findScripts(dir);
    for (const script of scripts) {
      const content = await fs.readFile(script, 'utf-8');
      const dangerous = [
        /rm\s+-rf/,
        /eval\s*\(/,
        /exec\s*\(/,
        /child_process\.exec/
      ];
      for (const pattern of dangerous) {
        if (pattern.test(content)) {
          issues.push(`脚本包含危险操作：${script}`);
        }
      }
    }

    return issues;
  }

  // 警告检查
  private async checkWarnings(dir: string): Promise<string[]> {
    const warnings: string[] = [];

    // 检查是否有大文件
    const files = await this.getAllFiles(dir);
    for (const file of files) {
      const stats = await fs.stat(file);
      if (stats.size > 10 * 1024 * 1024) { // 10MB
        warnings.push(`包含大文件：${file} (${(stats.size / 1024 / 1024).toFixed(2)}MB)`);
      }
    }

    // 检查是否有 README.md（不应该出现在技能包中）
    if (await this.pathExists(dir + '/README.md')) {
      warnings.push('技能包不应包含 README.md（说明应放在 SKILL.md 中）');
    }

    return warnings;
  }
}
```

## 集成方案

### 与现有系统集成

#### 1. 扩展 sessions_spawn

```typescript
// 现有调用
sessions_spawn({
  task: "Do something",
  runtime: "subagent"
});

// 新增参数
sessions_spawn({
  task: "Do something",
  runtime: "subagent",
  skills: {
    mode: "custom",
    allowed: ["github", "obsidian"]
  }
  // 或者简化版
  skills: "github,obsidian"  // 继承模式，只添加这些技能
});
```

#### 2. CLI 工具

```bash
# 列出所有技能
openclaw skills list

# 上传技能包
openclaw skills upload ./my-skill.skill

# 删除技能
openclaw skills delete my-skill

# 验证技能包
openclaw skills validate ./my-skill.skill

# 查看 Agent 技能配置
openclaw agents skills main

# 设置 Agent 技能
openclaw agents skills set main --mode custom --allow github,obsidian
```

#### 3. 聊天接口

```
用户: "创建一个只会写代码的子 Agent"
AI: "好的，我来创建一个专门的编程助手..."
AI: (创建时指定 skills: ["github", "coding-agent", "obsidian"])

用户: "上传一个新技能"
AI: "请发送 .skill 压缩包"
用户: [上传文件]
AI: "正在验证..."
AI: "✅ 技能已安装：my-skill v1.0.0"
AI: "⚠️  警告：包含大文件（15MB）"
```

## 数据存储设计

### 目录结构

```
~/.openclaw/
├── skills/
│   ├── builtin/               # 内置技能（符号链接到安装目录）
│   │   ├── github -> ../../node_modules/.../skills/github
│   │   └── obsidian -> ../../node_modules/.../skills/obsidian
│   │
│   ├── user/                  # 用户技能
│   │   ├── my-skill/
│   │   │   ├── SKILL.md
│   │   │   ├── scripts/
│   │   │   └── assets/
│   │   └── another-skill/
│   │
│   ├── index.json             # 技能索引
│   └── cache/                 # 缓存（解析后的元数据）
│
├── agents/
│   ├── config.json            # 全局配置
│   ├── profiles/              # Agent 配置
│   │   ├── main.json
│   │   ├── templates/
│   │   │   ├── default.json
│   │   │   └── coding-agent.json
│   │   └── sub-agent-*.json   # 动态生成
│   └── hierarchy.json         # 层级关系
│
└── cache/
    └── skills/                # 技能加载缓存
        └── main/
            └── skills-meta.json
```

### 索引文件示例

```json
{
  "version": "1.0",
  "skills": [
    {
      "id": "github",
      "name": "GitHub",
      "description": "Interact with GitHub using the gh CLI...",
      "version": "1.0.0",
      "path": "/root/.openclaw/skills/builtin/github",
      "source": "builtin",
      "installed_at": "2026-03-26T00:00:00Z",
      "risk_level": "low"
    },
    {
      "id": "my-custom-skill",
      "name": "My Custom Skill",
      "description": "Does something special",
      "version": "1.0.0",
      "path": "/root/.openclaw/skills/user/my-custom-skill",
      "source": "user",
      "installed_at": "2026-04-01T09:00:00Z",
      "risk_level": "medium"
    }
  ],
  "last_updated": "2026-04-01T09:00:00Z"
}
```

## 安全设计

### 1. 技能沙箱

```typescript
class SkillSandbox {
  // 在受限环境中执行脚本
  async executeScript(
    scriptPath: string,
    args: string[]
  ): Promise<{ stdout: string; stderr: string }> {
    // 使用 worker_threads 或 child_process
    // 限制：
    // - 只读访问指定目录
    // - 禁止网络访问（除非显式授权）
    // - 超时限制
    // - 内存限制
  }
}
```

### 2. 权限控制

```json
{
  "skill_permissions": {
    "dangerous": {
      "wecom-edit-todo": {
        "risk_level": "high",
        "requires_explicit consent": true,
        "default_denied_for": ["subagent"]
      }
    },
    "safe": {
      "github": {
        "risk_level": "low",
        "default_allowed": true
      }
    }
  }
}
```

### 3. 审计日志

```typescript
interface SkillAuditLog {
  timestamp: string;
  action: 'install' | 'uninstall' | 'use' | 'error';
  skill_id: string;
  agent_id: string;
  user: string;
  details: any;
}

// 记录所有技能操作
class SkillAuditor {
  async log(event: SkillAuditLog): Promise<void> {
    // 写入 ~/.openclaw/logs/skills-audit.jsonl
  }
}
```

## 实现计划

### Phase 1: 核心功能（2-3天）
- [x] SkillManager 基础实现
- [ ] SkillValidator 实现
- [ ] 技能索引管理
- [ ] CLI 基础命令（list, upload, delete）

### Phase 2: Agent 集成（2-3天）
- [ ] Agent 配置文件管理
- [ ] SkillLoader 实现
- [ ] 扩展 sessions_spawn
- [ ] 测试继承和自定义模式

### Phase 3: 安全和增强（2-3天）
- [ ] 安全检查增强
- [ ] 权限控制系统
- [ ] 审计日志
- [ ] CLI 完善

### Phase 4: 文档和测试（1-2天）
- [ ] API 文档
- [ ] 用户指南
- [ ] 单元测试
- [ ] 集成测试

**总计：7-11 天**

## 技术栈

- **语言**: TypeScript/Node.js
- **压缩**: adm-zip 或 jszip
- **验证**: yaml、ajv
- **CLI**: commander.js
- **测试**: jest
- **日志**: pino 或 winston

## 兼容性

- **Node.js**: >= 18.0.0
- **OpenClaw**: >= 2026.3.24
- **操作系统**: Linux, macOS, Windows (WSL)

## 风险和挑战

1. **向后兼容**: 需要确保不影响现有技能加载
2. **性能**: 技能索引不应显著增加启动时间
3. **安全**: 外部技能包可能包含恶意代码
4. **用户体验**: 上传失败时需要清晰的错误提示

## 未来扩展

1. **技能依赖管理**: 支持技能之间的依赖关系
2. **技能版本控制**: 支持同一技能的多个版本
3. **技能市场**: 集成 ClawHub，支持在线安装
4. **技能开发工具**: 提供 CLI 工具帮助开发和测试技能

---

**创建时间**: 2026-04-01
**作者**: 绫音 (AI Assistant)
**状态**: 设计方案 v1.0
