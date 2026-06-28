<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelFavoriteJob, getFavoriteJobs } from '../../api/job'

const router = useRouter()
const loading = ref(false)
const jobs = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 9,
})

async function loadFavorites() {
  loading.value = true
  try {
    const res = await getFavoriteJobs(query)
    jobs.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function pageChange(page) {
  query.pageNum = page
  loadFavorites()
}

function goDetail(id) {
  router.push(`/user/jobs/${id}`)
}

async function removeFavorite(job) {
  await ElMessageBox.confirm(`确定取消收藏“${job.title}”吗？`, '取消收藏', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })
  await cancelFavoriteJob(job.id)
  ElMessage.success('已取消收藏')
  if (jobs.value.length === 1 && query.pageNum > 1) {
    query.pageNum -= 1
  }
  loadFavorites()
}

onMounted(loadFavorites)
</script>

<template>
  <main class="favorites-page">
    <section class="content">
      <div class="toolbar">
        <div>
          <h2>我的收藏</h2>
          <p>集中查看已收藏岗位，方便后续投递和对比。</p>
        </div>
        <el-button type="primary" @click="router.push('/user/jobs')">继续浏览岗位</el-button>
      </div>

      <el-skeleton :loading="loading" animated :rows="8">
        <el-empty v-if="jobs.length === 0" description="暂无收藏岗位">
          <el-button type="primary" @click="router.push('/user/jobs')">去收藏岗位</el-button>
        </el-empty>

        <div v-else class="job-grid">
          <article v-for="job in jobs" :key="job.id" class="job-card">
            <div class="job-title">
              <h3>{{ job.title }}</h3>
              <el-tag size="small">{{ job.jobType }}</el-tag>
            </div>
            <p class="salary">{{ job.salaryMin }} - {{ job.salaryMax }} / {{ job.salaryUnit }}</p>
            <p class="meta">{{ job.industry }} · {{ job.workCity }} · 招 {{ job.recruitCount }} 人</p>
            <p class="address">{{ job.workAddress }}</p>
            <p class="desc">{{ job.description }}</p>

            <div class="card-actions">
              <el-button type="primary" :icon="View" @click="goDetail(job.id)">查看详情</el-button>
              <el-button :icon="Delete" @click="removeFavorite(job)">取消收藏</el-button>
            </div>
          </article>
        </div>
      </el-skeleton>

      <div v-if="total > query.pageSize" class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="query.pageSize"
          :current-page="query.pageNum"
          @current-change="pageChange"
        />
      </div>
    </section>
  </main>
</template>

<style scoped>
.favorites-page {
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
  margin-bottom: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.toolbar h2 {
  margin: 0;
  font-size: 20px;
}

.toolbar p {
  margin: 6px 0 0;
  color: #64748b;
}

.job-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.job-card {
  display: flex;
  min-height: 260px;
  flex-direction: column;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.job-title {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
}

.job-title h3 {
  margin: 0;
  font-size: 18px;
}

.salary {
  margin: 14px 0 8px;
  color: #dc2626;
  font-weight: 700;
}

.meta,
.address,
.desc {
  color: #64748b;
  line-height: 1.6;
}

.address {
  margin: 8px 0;
}

.desc {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.card-actions {
  display: flex;
  gap: 10px;
  margin-top: auto;
  padding-top: 16px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 22px;
}

@media (max-width: 1024px) {
  .job-grid {
    grid-template-columns: 1fr;
  }

  .toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
