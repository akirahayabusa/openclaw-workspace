<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- Agent 统计 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Monitor /></el-icon>
              <span>Agent 统计</span>
            </div>
          </template>
          <div class="stat-item">
            <div class="stat-value">{{ stats.agents.total }}</div>
            <div class="stat-label">总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value success">{{ stats.agents.enabled }}</div>
            <div class="stat-label">启用</div>
          </div>
          <div class="stat-item">
            <div class="stat-value danger">{{ stats.agents.disabled }}</div>
            <div class="stat-label">禁用</div>
          </div>
        </el-card>
      </el-col>

      <!-- Skill 统计 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>Skill 统计</span>
            </div>
          </template>
          <div class="stat-item">
            <div class="stat-value">{{ stats.skills.total }}</div>
            <div class="stat-label">总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value success">{{ stats.skills.loaded }}</div>
            <div class="stat-label">已加载</div>
          </div>
        </el-card>
      </el-col>

      <!-- Session 统计 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><ChatLineRound /></el-icon>
              <span>Session 统计</span>
            </div>
          </template>
          <div class="stat-item">
            <div class="stat-value">{{ stats.sessions.active }}</div>
            <div class="stat-label">活跃会话</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.sessions.total }}</div>
            <div class="stat-label">总会话数</div>
          </div>
        </el-card>
      </el-col>

      <!-- Memory 统计 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Cpu /></el-icon>
              <span>Memory 统计</span>
            </div>
          </template>
          <div class="stat-item">
            <div class="stat-value">{{ stats.memories.count }}</div>
            <div class="stat-label">记忆条数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.memories.size }}</div>
            <div class="stat-label">占用空间</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统状态 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Setting /></el-icon>
              <span>系统状态</span>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="系统状态">
              <el-tag 
                :type="stats.system.status === 'healthy' ? 'success' : 
                       stats.system.status === 'warning' ? 'warning' : 'danger'"
              >
                {{ stats.system.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="运行时间">
              {{ stats.system.uptime }}
            </el-descriptions-item>
            <el-descriptions-item label="系统版本">
              {{ stats.system.version }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Operation /></el-icon>
              <span>快捷操作</span>
            </div>
          </template>
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/agents')">
              <el-icon><Plus /></el-icon>
              管理 Agent
            </el-button>
            <el-button type="success" @click="$router.push('/skills')">
              <el-icon><Upload /></el-icon>
              上传 Skill
            </el-button>
            <el-button type="warning" @click="refreshStats">
              <el-icon><Refresh /></el-icon>
              刷新统计
            </el-button>
            <el-button type="info" @click="$router.push('/sessions')">
              <el-icon><View /></el-icon>
              查看会话
            </el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Monitor,
  Document,
  ChatLineRound,
  Cpu,
  Setting,
  Operation,
  Plus,
  Upload,
  Refresh,
  View
} from '@element-plus/icons-vue'
import { dashboardApi, type DashboardStats } from '@/api'

// 响应式数据
const stats = ref<DashboardStats>({
  agents: { total: 0, enabled: 0, disabled: 0 },
  skills: { total: 0, loaded: 0 },
  sessions: { active: 0, total: 0 },
  memories: { count: 0, size: '0 KB' },
  system: { status: 'healthy', uptime: '0s', version: '1.0.0' }
})

// 刷新统计
const refreshStats = async () => {
  try {
    const data = await dashboardApi.stats()
    stats.value = data
    ElMessage.success('统计信息已刷新')
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 加载数据
onMounted(() => {
  refreshStats()
  // 每 30 秒自动刷新
  setInterval(refreshStats, 30000)
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 20px;

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: bold;
    font-size: 16px;
  }

  .stat-item {
    text-align: center;
    margin: 15px 0;

    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: #409eff;

      &.success {
        color: #67c23a;
      }

      &.danger {
        color: #f56c6c;
      }
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 5px;
    }
  }
}
</style>
