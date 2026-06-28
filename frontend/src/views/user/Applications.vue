<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Briefcase, Clock, CircleCheck, RefreshLeft, View } from '@element-plus/icons-vue'
import { getApplications, withdrawApplication } from '../../api/application'

const router = useRouter()
const loading = ref(false)
const applications = ref([])
const activeStatus = ref('all')

const statusMap = {
  0: { text: '待处理', type: 'warning' },
  1: { text: '企业已查看', type: 'primary' },
  2: { text: '已通过', type: 'success' },
  3: { text: '已拒绝', type: 'danger' },
  4: { text: '已撤回', type: 'info' },
}

const statusTabs = [
  { label: '全部', value: 'all' },
  { label: '待处理', value: 0 },
  { label: '已查看', value: 1 },
  { label: '已通过', value: 2 },
  { label: '已拒绝', value: 3 },
  { label: '已撤回', value: 4 },
]

const filteredApplications = computed(() => {
  if (activeStatus.value === 'all') {
    return applications.value
  }
  return applications.value.filter((item) => item.status === activeStatus.value)
})

const stats = computed(() => [
  {
    label: '全部投递',
    value: applications.value.length,
    icon: Briefcase,
    color: '#2563eb',
  },
  {
    label: '待企业处理',
    value: applications.value.filter((item) => item.status === 0).length,
    icon: Clock,
    color: '#d97706',
  },
  {
    label: '企业已查看',
    value: applications.value.filter((item) => item.status === 1).length,
    icon: View,
    color: '#0891b2',
  },
  {
    label: '已通过',
    value: applications.value.filter((item) => item.status === 2).length,
    icon: CircleCheck,
    color: '#16a34a',
  },
])

async function loadApplications() {
  loading.value = true
  try {
    const res = await getApplications()
    applications.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function withdraw(row) {
  await ElMessageBox.confirm('确认撤回该投递记录？', '撤回确认', {
    type: 'warning',
    confirmButtonText: '撤回',
    cancelButtonText: '取消',
  })
  await withdrawApplication(row.id)
  ElMessage.success('撤回成功')
  loadApplications()
}

function canWithdraw(status) {
  return status === 0 || status === 1
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

function goJob(row) {
  router.push(`/user/jobs/${row.jobId}`)
}

onMounted(loadApplications)
</script>

<template>
  <main class="applications-page">
    <section class="content">
      <div class="toolbar">
        <div>
          <h2>投递进度</h2>
          <span>跟踪已投递岗位的企业处理状态，待处理或已查看的投递可以撤回。</span>
        </div>
        <div class="toolbar-actions">
          <el-button type="primary" @click="router.push('/user/jobs')">继续投递</el-button>
          <el-button :icon="RefreshLeft" @click="loadApplications">刷新</el-button>
        </div>
      </div>

      <section class="stats-grid">
        <article v-for="item in stats" :key="item.label" class="stat-card">
          <div class="stat-icon" :style="{ background: item.color }">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div>
            <p>{{ item.label }}</p>
            <strong>{{ item.value }}</strong>
          </div>
        </article>
      </section>

      <div class="filter-bar">
        <el-segmented v-model="activeStatus" :options="statusTabs" />
      </div>

      <el-table
        v-loading="loading"
        :data="filteredApplications"
        border
        class="application-table"
        empty-text="暂无投递记录"
      >
        <el-table-column prop="id" label="投递编号" width="100" />
        <el-table-column label="岗位信息" min-width="210">
          <template #default="{ row }">
            <div class="main-text">{{ row.jobTitle || `岗位 ${row.jobId}` }}</div>
            <span class="sub-text">岗位ID：{{ row.jobId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="企业" min-width="170">
          <template #default="{ row }">
            <div class="main-text">{{ row.companyName || `企业 ${row.companyId}` }}</div>
            <span class="sub-text">企业ID：{{ row.companyId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="投递简历" min-width="160">
          <template #default="{ row }">
            <div class="main-text">{{ row.resumeTitle || `简历 ${row.resumeId}` }}</div>
            <span class="sub-text">简历ID：{{ row.resumeId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="投递时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="处理时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.handleTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.text || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goJob(row)">岗位详情</el-button>
            <el-button
              link
              type="danger"
              :disabled="!canWithdraw(row.status)"
              @click="withdraw(row)"
            >
              撤回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && applications.length === 0"
        class="empty-guide"
        description="暂无投递记录，先去岗位中心选择感兴趣的岗位吧"
      >
        <el-button type="primary" @click="router.push('/user/jobs')">浏览岗位</el-button>
      </el-empty>
    </section>
  </main>
</template>

<style scoped>
.applications-page {
  min-height: calc(100vh - 124px);
}

.content {
  width: 100%;
  margin: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  margin-bottom: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.toolbar h2 {
  margin: 0;
  font-size: 20px;
}

.toolbar span {
  display: inline-block;
  margin-top: 6px;
  color: #64748b;
  font-size: 14px;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.stat-icon {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  font-size: 22px;
}

.stat-card p {
  margin: 0 0 6px;
  color: #64748b;
}

.stat-card strong {
  font-size: 26px;
}

.filter-bar {
  padding: 14px 16px;
  margin-bottom: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.application-table {
  border-radius: 8px;
  overflow: hidden;
}

.main-text {
  color: #1f2937;
  font-weight: 600;
  line-height: 1.5;
}

.sub-text {
  color: #94a3b8;
  font-size: 12px;
}

.empty-guide {
  margin-top: 24px;
  padding: 32px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

@media (max-width: 900px) {
  .toolbar,
  .toolbar-actions {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
