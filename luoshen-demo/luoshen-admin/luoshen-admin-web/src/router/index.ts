import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表板', icon: 'DataAnalysis' }
      },
      {
        path: 'agents',
        name: 'AgentList',
        component: () => import('@/views/AgentList.vue'),
        meta: { title: 'Agent 管理', icon: 'Monitor' }
      },
      {
        path: 'skills',
        name: 'SkillList',
        component: () => import('@/views/SkillList.vue'),
        meta: { title: 'Skill 管理', icon: 'Document' }
      },
      {
        path: 'sessions',
        name: 'SessionList',
        component: () => import('@/views/SessionList.vue'),
        meta: { title: 'Session 管理', icon: 'ChatLineRound' }
      },
      {
        path: 'mcp',
        name: 'McpList',
        component: () => import('@/views/McpList.vue'),
        meta: { title: 'MCP 管理', icon: 'Connection' }
      },
      {
        path: 'memories',
        name: 'MemoryList',
        component: () => import('@/views/MemoryList.vue'),
        meta: { title: 'Memory 管理', icon: 'Cpu' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta && to.meta.title) {
    document.title = `${to.meta.title} - 洛神系统管理平台`
  }
  next()
})

export default router
