<template>
  <div class="session-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索会话 ID 或用户 ID"
        style="width: 250px"
        @keyup.enter="searchSessions"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 300px"
        @change="searchSessions"
      />

      <el-select 
        v-model="selectedAgentId" 
        placeholder="选择 Agent"
        style="width: 200px"
        clearable
        @change="searchSessions"
      >
        <el-option
          v-for="agent in agents"
          :key="agent.agentId"
          :label="agent.name"
          :value="agent.agentId"
        />
      </el-select>

      <el-button type="danger" @click="clearExpiredSessions">
        <el-icon><Delete /></el-icon>
        清理过期会话
      </el-button>

      <el-button @click="refreshList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- Session 列表 -->
    <el-table
      :data="sessions"
      stripe
      v-loading="loading"
      @expand-change="loadSessionMessages"
    >
      <el-table-column type="expand">
        <template #default="scope">
          <div v-if="scope.row.messages && scope.row.messages.length > 0" class="messages-container">
            <h4>会话消息历史</h4>
            <el-timeline>
              <el-timeline-item
                v-for="msg in scope.row.messages"
                :key="msg.id"
                :timestamp="msg.timestamp"
                placement="top"
              >
                <el-card>
                  <div class="message-item">
                    <el-tag 
                      :type="msg.role === 'user' ? 'primary' : 'success'"
                      size="small"
                    >
                      {{ msg.role }}
                    </el-tag>
                    <div class="message-content">{{ msg.content }}</div>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
          <div v-else class="no-messages">
            暂无消息记录
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="sessionId" label="Session ID" width="250" />
      <el-table-column prop="userId" label="用户 ID" width="150" />
      <el-table-column prop="agentId" label="Agent ID" width="150" />
      <el-table-column prop="messageCount" label="消息数" width="80">
        <template #default="scope">
          <el-badge :value="scope.row.messageCount" type="primary" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" fixed="right" width="100">
        <template #default="scope">
          <el-button 
            size="small" 
            type="danger"
            @click="deleteSession(scope.row.sessionId)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalElements"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadSessions"
        @current-change="loadSessions"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete, Refresh } from '@element-plus/icons-vue'
import { sessionApi, agentApi, type SessionInfo, type AgentConfig } from '@/api'

// 响应式数据
const sessions = ref<SessionInfo[]>([])
const agents = ref<AgentConfig[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const searchKeyword = ref('')
const dateRange = ref<Date[]>([])
const selectedAgentId = ref('')

// 加载会话列表
const loadSessions = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    if (selectedAgentId.value) {
      params.agentId = selectedAgentId.value
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0].toISOString()
      params.endTime = dateRange.value[1].toISOString()
    }

    const data = await sessionApi.list(params)
    sessions.value = data.content
    totalElements.value = data.totalElements
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载 Session 消息
const loadSessionMessages = async (row: SessionInfo) => {
  if (!row.messages) {
    try {
      const data = await sessionApi.get(row.sessionId)
      row.messages = data.messages
      row.messageCount = data.messages?.length || 0
    } catch (error) {
      console.error('加载消息失败:', error)
    }
  }
}

// 搜索会话
const searchSessions = () => {
  currentPage.value = 1
  loadSessions()
}

// 刷新列表
const refreshList = async () => {
  await loadSessions()
  ElMessage.success('列表已刷新')
}

// 清理过期会话
const clearExpiredSessions = async () => {
  try {
    const { value: days } = await ElMessageBox.prompt(
      '请输入要清理多少天前的会话',
      '清理过期会话',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^\d+$/,
        inputErrorMessage: '请输入数字'
      }
    )
    
    const count = await sessionApi.clearExpired(parseInt(days))
    ElMessage.success(`已清理 ${count} 个会话`)
    loadSessions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理失败:', error)
    }
  }
}

// 删除会话
const deleteSession = async (sessionId: string) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该会话吗？此操作不可恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await sessionApi.delete(sessionId)
    ElMessage.success('会话已删除')
    loadSessions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 加载 Agent 列表（用于筛选）
const loadAgents = async () => {
  try {
    const data = await agentApi.list()
    agents.value = data
  } catch (error) {
    console.error('加载 Agent 列表失败:', error)
  }
}

// 加载数据
onMounted(() => {
  loadAgents()
  loadSessions()
})
</script>

<style scoped lang="scss">
.session-list {
  padding: 20px;

  .search-bar {
    margin-bottom: 20px;
    display: flex;
    gap: 15px;
    align-items: center;
  }

  .messages-container {
    padding: 20px;

    h4 {
      margin-bottom: 15px;
      color: #303133;
    }

    .message-item {
      display: flex;
      gap: 10px;
      align-items: flex-start;

      .message-content {
        flex: 1;
        line-height: 1.6;
      }
    }
  }

  .no-messages {
    padding: 40px;
    text-align: center;
    color: #909399;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>
