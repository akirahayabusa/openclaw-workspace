# 洛神平台技能系统 - 实施计划

## ✅ 确认的设计方案

基于我们的讨论，确认以下设计：

### 核心功能
1. **技能包上传** - 支持上传 .skill 压缩包（包含脚本、资源等）
2. **灵活的类型系统** - 分类 + 标签 + 自定义类型（混合模式）
3. **Agent 技能绑定** - 多对多关系，支持继承/自定义/禁用
4. **Web 管理界面** - 表单 + 上传混合模式

### 数据模型

#### SkillConfigEntity（扩展）
```java
@Entity
@Table(name = "luoshen_skill_config")
public class SkillConfigEntity {
    // 原有字段
    private Long id;
    private String skillId;
    private String name;
    private String description;
    private String content;  // Markdown 内容

    // 新增字段
    private String packagePath;      // 技能包路径
    private String source;           // builtin, user_upload, marketplace
    private String riskLevel;        // low, medium, high
    private Long fileSize;           // 文件大小
    private String checksum;         // 校验和
    private String dependencies;     // 依赖（JSON）
    private Boolean hasScripts;      // 是否包含脚本
    private String scriptFiles;      // 脚本列表（JSON）

    // 灵活的类型系统
    private String category;         // 主分类（可选）
    private String subCategory;      // 子分类（可选）
    private String tags;             // 标签（JSON 数组）
    private String customType;       // 自定义类型
    private String capabilities;     // 能力特征（JSON）
}
```

#### AgentSkillRelation（新增）
```java
@Entity
@Table(name = "luoshen_agent_skill_relation")
public class AgentSkillRelation {
    private Long id;
    private String agentId;
    private String skillId;
    private String bindMode;         // inherit, custom, none
    private Boolean enabled;
    private Integer priority;
    private String config;           // JSON 配置
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## 实施步骤

### Phase 1: 数据模型扩展（1天）
- [ ] 创建 AgentSkillRelation 实体
- [ ] 扩展 SkillConfigEntity
- [ ] 创建 Repository
- [ ] 编写单元测试

### Phase 2: 技能包上传功能（2天）
- [ ] 实现文件上传 API
- [ ] 实现 ZIP 解压
- [ ] 实现安全验证
- [ ] 实现技能元数据提取
- [ ] 编写集成测试

### Phase 3: Agent 技能加载（2天）
- [ ] 实现 AgentSkillLoader
- [ ] 集成到 AgentDynamicService
- [ ] 实现技能绑定 API
- [ ] 测试多对多关系

### Phase 4: 前端界面（2天）
- [ ] 技能管理页面
- [ ] 新增/编辑技能弹窗
- [ ] Agent 技能绑定页面
- [ ] API 对接

### Phase 5: 测试和文档（1天）
- [ ] 端到端测试
- [ ] 编写用户文档
- [ ] 编写 API 文档

---

现在开始 Phase 1 的实现...
