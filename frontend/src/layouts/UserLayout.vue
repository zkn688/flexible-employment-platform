<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell,
  Briefcase,
  ChatDotRound,
  Document,
  Files,
  House,
  MagicStick,
  Memo,
  Money,
  Setting,
  Star,
  User,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userInfo = computed(() => JSON.parse(localStorage.getItem('userInfo') || '{}'))

const menuItems = [
  { path: '/user/home', label: '用户首页', icon: House },
  { path: '/user/jobs', label: '岗位中心', icon: Briefcase },
  { path: '/user/favorites', label: '我的收藏', icon: Star },
  { path: '/user/resumes', label: '简历管理', icon: Document },
  { path: '/user/applications', label: '我的投递', icon: Files },
  { path: '/user/policies', label: '公告政策', icon: Bell },
  { path: '/user/social-security', label: '社保服务', icon: Money },
  { path: '/user/contracts', label: '电子合同', icon: Memo },
  { path: '/user/messages', label: '消息中心', icon: ChatDotRound },
  { path: '/user/ai-interview', label: 'AI面试', icon: MagicStick },
  { path: '/user/preference', label: '求职偏好', icon: Setting },
  { path: '/user/profile', label: '个人中心', icon: User },
]

const activeMenu = computed(() => {
  const matched = menuItems.find((item) => route.path === item.path || route.path.startsWith(`${item.path}/`))
  return matched?.path || '/user/home'
})

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/user/login')
}
</script>

<template>
  <div class="user-layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">灵</div>
        <div>
          <h1>灵活就业平台</h1>
          <p>用户端</p>
        </div>
      </div>

      <el-menu class="side-menu" :default-active="activeMenu" router>
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <section class="main-shell">
      <header class="layout-header">
        <div>
          <h2>{{ menuItems.find((item) => item.path === activeMenu)?.label || '用户端' }}</h2>
          <p>欢迎回来，{{ userInfo.realName || userInfo.username || '用户' }}</p>
        </div>
        <el-button @click="logout">退出登录</el-button>
      </header>

      <main class="layout-content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<style scoped>
.user-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fb;
}

.sidebar {
  position: sticky;
  top: 0;
  width: 248px;
  height: 100vh;
  flex: 0 0 248px;
  border-right: 1px solid #e5e7eb;
  background: #101828;
}

.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  height: 76px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  color: #fff;
}

.brand-mark {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 8px;
  background: #2563eb;
  font-size: 22px;
  font-weight: 700;
}

.brand h1 {
  margin: 0;
  font-size: 18px;
}

.brand p {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 13px;
}

.side-menu {
  border-right: 0;
  background: transparent;
}

.side-menu :deep(.el-menu-item) {
  height: 46px;
  margin: 4px 10px;
  border-radius: 8px;
  color: #cbd5e1;
}

.side-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: #2563eb;
  color: #fff;
}

.main-shell {
  min-width: 0;
  flex: 1;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 76px;
  padding: 0 32px;
  border-bottom: 1px solid #e5e7eb;
  background: #fff;
}

.layout-header h2 {
  margin: 0;
  font-size: 22px;
}

.layout-header p {
  margin: 6px 0 0;
  color: #64748b;
}

.layout-content {
  padding: 24px;
}

@media (max-width: 900px) {
  .user-layout {
    flex-direction: column;
  }

  .sidebar {
    position: static;
    width: 100%;
    height: auto;
    flex: none;
  }

  .brand {
    height: 68px;
  }

  .side-menu {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    padding: 8px;
  }

  .side-menu :deep(.el-menu-item) {
    margin: 2px;
  }

  .layout-header {
    height: auto;
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
    padding: 18px 24px;
  }
}
</style>
