<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>洛神系统</h2>
        <p>管理平台</p>
      </div>
      <el-menu
        :default-active="currentRoute"
        router
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表板</span>
        </el-menu-item>
        <el-menu-item index="/agents">
          <el-icon><Monitor /></el-icon>
          <span>Agent 管理</span>
        </el-menu-item>
        <el-menu-item index="/skills">
          <el-icon><Document /></el-icon>
          <span>Skill 管理</span>
        </el-menu-item>
        <el-menu-item index="/sessions">
          <el-icon><ChatLineRound /></el-icon>
          <span>Session 管理</span>
        </el-menu-item>
        <el-menu-item index="/mcp">
          <el-icon><Connection /></el-icon>
          <span>MCP 管理</span>
        </el-menu-item>
        <el-menu-item index="/memories">
          <el-icon><Cpu /></el-icon>
          <span>Memory 管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button @click="refreshPage">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-dropdown>
            <span class="el-dropdown-link">
              <el-avatar :size="32" icon="UserFilled" />
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  DataAnalysis,
  Monitor,
  Document,
  ChatLineRound,
  Connection,
  Cpu,
  Refresh
} from '@element-plus/icons-vue'

const router = useRouter()

const currentRoute = computed(() => router.currentRoute.value.path)

const currentPageTitle = computed(() => {
  const map: Record<string, string> = {
    '/': '仪表板',
    '/agents': 'Agent 管理',
    '/skills': 'Skill 管理',
    '/sessions': 'Session 管理',
    '/mcp': 'MCP 管理',
    '/memories': 'Memory 管理'
  }
  return map[currentRoute.value] || '仪表板'
})

const refreshPage = () => {
  window.location.reload()
}
</script>

<style scoped lang="scss">
.layout {
  display: flex;
  height: 100vh;

  .sidebar {
    background-color: #545c64;
    color: #fff;

    .logo {
      height: 80px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      border-bottom: 1px solid #434a50;

      h2 {
        margin: 0;
        font-size: 20px;
        color: #ffd04b;
      }

      p {
        margin: 5px 0 0 0;
        font-size: 12px;
        color: #ccc;
      }
    }

    .el-menu {
      border: none;
    }
  }

  .header {
    background-color: #fff;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;

    .header-right {
      display: flex;
      align-items: center;
      gap: 15px;

      .el-dropdown-link {
        cursor: pointer;
      }
    }
  }

  .main {
    background-color: #f0f2f5;
    overflow-y: auto;
  }
}
</style>
