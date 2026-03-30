import request from '@/utils/request'

// Agent 相关接口

export interface AgentConfig {
  agentId: string
  name: string
  description: string
  type: 'leader' | 'core' | 'sub'
  systemPrompt: string
  modelName: string
  toolsJson?: string
  skillsJson?: string
  parentAgentId?: string
  enabled: boolean
  createTime?: string
  updateTime?: string
}

export interface AgentStats {
  total: number
  enabled: number
  disabled: number
}

export const agentApi = {
  // 获取所有 Agent
  list: (): Promise<AgentConfig[]> => {
    return request.get('/agents')
  },

  // 获取单个 Agent
  get: (agentId: string): Promise<AgentConfig> => {
    return request.get(`/agents/${agentId}`)
  },

  // 创建 Agent
  create: (data: AgentConfig): Promise<AgentConfig> => {
    return request.post('/agents', data)
  },

  // 更新 Agent
  update: (agentId: string, data: Partial<AgentConfig>): Promise<AgentConfig> => {
    return request.put(`/agents/${agentId}`, data)
  },

  // 删除 Agent
  delete: (agentId: string): Promise<void> => {
    return request.delete(`/agents/${agentId}`)
  },

  // 启用/禁用 Agent
  toggle: (agentId: string): Promise<void> => {
    return request.post(`/agents/${agentId}/toggle`)
  },

  // 刷新所有 Agent
  refresh: (): Promise<void> => {
    return request.post('/agents/refresh')
  },

  // 测试 Agent
  test: (agentId: string, message: string): Promise<any> => {
    return request.post(`/agents/${agentId}/test`, { message })
  },

  // 获取统计信息
  stats: (): Promise<AgentStats> => {
    return request.get('/agents/stats')
  }
}

// Skill 相关接口

export interface SkillConfig {
  skillId: string
  name: string
  description: string
  content: string
  enabled: boolean
  createTime?: string
  updateTime?: string
}

export const skillApi = {
  // 获取所有 Skill
  list: (): Promise<SkillConfig[]> => {
    return request.get('/skills')
  },

  // 获取单个 Skill
  get: (skillId: string): Promise<SkillConfig> => {
    return request.get(`/skills/${skillId}`)
  },

  // 上传 Skill
  upload: (file: File): Promise<SkillConfig> => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/skills/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 创建 Skill
  create: (data: SkillConfig): Promise<SkillConfig> => {
    return request.post('/skills', data)
  },

  // 更新 Skill
  update: (skillId: string, data: Partial<SkillConfig>): Promise<SkillConfig> => {
    return request.put(`/skills/${skillId}`, data)
  },

  // 删除 Skill
  delete: (skillId: string): Promise<void> => {
    return request.delete(`/skills/${skillId}`)
  },

  // 加载 Skill
  load: (skillId: string): Promise<void> => {
    return request.post(`/skills/${skillId}/load`)
  },

  // 卸载 Skill
  unload: (skillId: string): Promise<void> => {
    return request.delete(`/skills/${skillId}/unload`)
  }
}

// Session 相关接口

export interface SessionInfo {
  sessionId: string
  userId: string
  agentId: string
  createTime: string
  updateTime: string
  messageCount: number
  messages?: MessageInfo[]
}

export interface MessageInfo {
  id: string
  role: string
  content: string
  timestamp: string
}

export const sessionApi = {
  // 获取所有 Session
  list: (params?: {
    agentId?: string
    userId?: string
    startTime?: string
    endTime?: string
    page?: number
    size?: number
  }): Promise<{ content: SessionInfo[]; totalElements: number }> => {
    return request.get('/sessions', { params })
  },

  // 获取单个 Session
  get: (sessionId: string): Promise<SessionInfo> => {
    return request.get(`/sessions/${sessionId}`)
  },

  // 删除 Session
  delete: (sessionId: string): Promise<void> => {
    return request.delete(`/sessions/${sessionId}`)
  },

  // 清理过期 Session
  clearExpired: (days: number): Promise<number> => {
    return request.post('/sessions/clear-expired', null, { params: { days } })
  }
}

// Memory 相关接口

export interface MemoryInfo {
  id: string
  agentId: string
  sessionId: string
  type: 'short_term' | 'long_term'
  content: string
  timestamp: string
}

export const memoryApi = {
  // 获取所有 Memory
  list: (params?: {
    agentId?: string
    sessionId?: string
    type?: string
    keyword?: string
    page?: number
    size?: number
  }): Promise<{ content: MemoryInfo[]; totalElements: number }> => {
    return request.get('/memories', { params })
  },

  // 搜索 Memory
  search: (keyword: string, agentId?: string): Promise<MemoryInfo[]> => {
    return request.get('/memories/search', { params: { keyword, agentId } })
  },

  // 删除 Memory
  delete: (memoryId: string): Promise<void> => {
    return request.delete(`/memories/${memoryId}`)
  },

  // 清空 Agent 的所有 Memory
  clearByAgent: (agentId: string): Promise<void> => {
    return request.delete(`/memories/agent/${agentId}`)
  }
}

// MCP 相关接口

export interface McpConfig {
  mcpId: string
  name: string
  transportType: 'stdio' | 'websocket'
  command?: string
  url?: string
  args?: string[]
  env?: Record<string, string>
  connected: boolean
  createTime?: string
  updateTime?: string
}

export const mcpApi = {
  // 获取所有 MCP 服务器
  list: (): Promise<McpConfig[]> => {
    return request.get('/mcp')
  },

  // 获取单个 MCP
  get: (mcpId: string): Promise<McpConfig> => {
    return request.get(`/mcp/${mcpId}`)
  },

  // 创建 MCP 配置
  create: (data: McpConfig): Promise<McpConfig> => {
    return request.post('/mcp', data)
  },

  // 更新 MCP 配置
  update: (mcpId: string, data: Partial<McpConfig>): Promise<McpConfig> => {
    return request.put(`/mcp/${mcpId}`, data)
  },

  // 删除 MCP 配置
  delete: (mcpId: string): Promise<void> => {
    return request.delete(`/mcp/${mcpId}`)
  },

  // 连接 MCP 服务器
  connect: (mcpId: string): Promise<void> => {
    return request.post(`/mcp/${mcpId}/connect`)
  },

  // 断开 MCP 服务器
  disconnect: (mcpId: string): Promise<void> => {
    return request.post(`/mcp/${mcpId}/disconnect`)
  },

  // 获取 MCP 工具列表
  getTools: (mcpId: string): Promise<any[]> => {
    return request.get(`/mcp/${mcpId}/tools`)
  }
}

// Dashboard 统计接口

export interface DashboardStats {
  agents: {
    total: number
    enabled: number
    disabled: number
  }
  skills: {
    total: number
    loaded: number
  }
  sessions: {
    active: number
    total: number
  }
  memories: {
    count: number
    size: string
  }
  system: {
    status: 'healthy' | 'warning' | 'error'
    uptime: string
    version: string
  }
}

export const dashboardApi = {
  // 获取仪表板统计信息
  stats: (): Promise<DashboardStats> => {
    return request.get('/dashboard/stats')
  }
}
