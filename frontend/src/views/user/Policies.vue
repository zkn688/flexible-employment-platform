<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNotices, getPolicies, getPolicyApplications } from '../../api/content'

const router = useRouter()
const activeTab = ref('notice')
const loading = ref(false)
const notices = ref([])
const policies = ref([])
const applications = ref([])

const applyStatusMap = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '通过', type: 'success' },
  2: { text: '拒绝', type: 'danger' },
}

async function loadData() {
  loading.value = true
  try {
    const [noticeRes, policyRes, applyRes] = await Promise.all([
      getNotices(),
      getPolicies(),
      getPolicyApplications(),
    ])
    notices.value = noticeRes.data || []
    policies.value = policyRes.data || []
    applications.value = applyRes.data || []
  } finally {
    loading.value = false
  }
}

function noticeTypeText(type) {
  return type === 2 ? '通知' : type === 3 ? '就业资讯' : '公告'
}

onMounted(loadData)
</script>

<template>
  <main class="policies-page">
    <section v-loading="loading" class="content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="公告通知" name="notice">
          <div class="list-panel">
            <article v-for="notice in notices" :key="notice.id" class="notice-item">
              <div>
                <h2>{{ notice.title }}</h2>
                <p>{{ notice.content }}</p>
              </div>
              <el-tag :type="notice.isTop ? 'danger' : 'info'">
                {{ notice.isTop ? '置顶' : noticeTypeText(notice.type) }}
              </el-tag>
            </article>
            <el-empty v-if="notices.length === 0" description="暂无公告" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="政策服务" name="policy">
          <div class="policy-grid">
            <article v-for="policy in policies" :key="policy.id" class="policy-card" @click="router.push(`/user/policies/${policy.id}`)">
              <h2>{{ policy.title }}</h2>
              <p>{{ policy.content }}</p>
              <el-button type="primary" link>查看详情并申报</el-button>
            </article>
            <el-empty v-if="policies.length === 0" description="暂无政策" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的申报" name="apply">
          <el-table :data="applications" border class="apply-table">
            <el-table-column prop="id" label="申报编号" width="100" />
            <el-table-column prop="policyId" label="政策ID" width="100" />
            <el-table-column prop="applicantName" label="申请人" width="120" />
            <el-table-column prop="phone" label="联系电话" width="140" />
            <el-table-column prop="applyTime" label="申请时间" min-width="180" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="applyStatusMap[row.status]?.type">{{ applyStatusMap[row.status]?.text || '未知' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditRemark" label="审核备注" min-width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>
  </main>
</template>

<style scoped>
.policies-page {
  min-height: calc(100vh - 124px);
}

.content {
  width: 100%;
  margin: 0;
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.list-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  gap: 16px;
  justify-content: space-between;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.notice-item h2,
.policy-card h2 {
  margin: 0;
  font-size: 18px;
}

.notice-item p,
.policy-card p {
  display: -webkit-box;
  margin: 10px 0 0;
  overflow: hidden;
  color: #64748b;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.policy-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.policy-card {
  min-height: 170px;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
}

.policy-card:hover {
  border-color: #93c5fd;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
  transform: translateY(-2px);
}

.apply-table {
  border-radius: 8px;
  overflow: hidden;
}

@media (max-width: 900px) {
  .notice-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .policy-grid {
    grid-template-columns: 1fr;
  }
}
</style>
