<script setup>
import { onMounted, ref } from 'vue'
import { getContractDetail, getContracts } from '../../api/contract'

const loading = ref(false)
const detailLoading = ref(false)
const drawerVisible = ref(false)
const contracts = ref([])
const currentContract = ref(null)

async function loadContracts() {
  loading.value = true
  try {
    const res = await getContracts()
    contracts.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  drawerVisible.value = true
  detailLoading.value = true
  try {
    const res = await getContractDetail(row.id)
    currentContract.value = res.data
  } finally {
    detailLoading.value = false
  }
}

onMounted(loadContracts)
</script>

<template>
  <main class="contracts-page">
    <section class="content">
      <el-table v-loading="loading" :data="contracts" border class="contract-table">
        <el-table-column prop="contractNo" label="合同编号" min-width="160" />
        <el-table-column prop="title" label="合同标题" min-width="180" />
        <el-table-column prop="companyId" label="企业ID" width="100" />
        <el-table-column prop="jobId" label="岗位ID" width="100" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '有效' : '失效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-drawer v-model="drawerVisible" title="合同详情" size="420px">
      <div v-loading="detailLoading" class="contract-detail" v-if="currentContract">
        <h2>{{ currentContract.title }}</h2>
        <p><span>合同编号</span>{{ currentContract.contractNo }}</p>
        <p><span>企业ID</span>{{ currentContract.companyId }}</p>
        <p><span>岗位ID</span>{{ currentContract.jobId }}</p>
        <p><span>开始日期</span>{{ currentContract.startDate }}</p>
        <p><span>结束日期</span>{{ currentContract.endDate }}</p>
        <p><span>合同状态</span>{{ currentContract.status === 1 ? '有效' : '失效' }}</p>
        <p><span>文件地址</span>{{ currentContract.fileUrl }}</p>
      </div>
    </el-drawer>
  </main>
</template>

<style scoped>
.contracts-page {
  min-height: calc(100vh - 124px);
}

.content {
  width: 100%;
  margin: 0;
}

.contract-table {
  border-radius: 8px;
  overflow: hidden;
}

.contract-detail h2 {
  margin: 0 0 20px;
  font-size: 20px;
}

.contract-detail p {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 12px 0;
  margin: 0;
  border-bottom: 1px solid #e5e7eb;
  color: #1f2937;
  line-height: 1.6;
}

.contract-detail span {
  flex: 0 0 88px;
  color: #64748b;
}

</style>
