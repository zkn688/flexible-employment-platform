<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Link, UploadFilled } from '@element-plus/icons-vue'
import {
  applySocialSecurity,
  getSocialApplications,
  getSocialPayments,
  paySocialPayment,
} from '../../api/social'
import { uploadMaterial } from '../../api/user'
import { resolveFileUrl } from '../../utils/url'

const loading = ref(false)
const applying = ref(false)
const materialUploading = ref(false)
const applications = ref([])
const payments = ref([])

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const form = reactive({
  applicantName: userInfo.realName || '',
  idCard: '',
  phone: userInfo.phone || '',
  insuranceType: '灵活就业人员养老保险',
  materialUrl: '',
})

const statusMap = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '通过', type: 'success' },
  2: { text: '拒绝', type: 'danger' },
}

async function loadData() {
  loading.value = true
  try {
    const [applyRes, paymentRes] = await Promise.all([
      getSocialApplications(),
      getSocialPayments(),
    ])
    applications.value = applyRes.data || []
    payments.value = paymentRes.data || []
  } finally {
    loading.value = false
  }
}

async function submitApply() {
  if (!form.applicantName || !form.idCard || !form.phone || !form.materialUrl) {
    ElMessage.warning('请填写申请人、身份证号、联系电话并上传材料')
    return
  }
  applying.value = true
  try {
    await applySocialSecurity(form)
    ElMessage.success('社保申请提交成功')
    loadData()
  } finally {
    applying.value = false
  }
}

function beforeMaterialUpload(file) {
  const extension = file.name.split('.').pop()?.toLowerCase()
  const allowed = ['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png', 'webp']
  const isAllowed = allowed.includes(extension)
  const isLt10M = file.size / 1024 / 1024 <= 10
  if (!isAllowed) {
    ElMessage.error('仅支持 PDF、Word 和图片材料')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('材料大小不能超过 10MB')
    return false
  }
  return true
}

async function handleMaterialUpload(options) {
  materialUploading.value = true
  try {
    const res = await uploadMaterial(options.file)
    form.materialUrl = res.data.url
    ElMessage.success('材料上传成功')
    options.onSuccess(res)
  } catch (error) {
    options.onError(error)
  } finally {
    materialUploading.value = false
  }
}

function getFileUrl(url) {
  return resolveFileUrl(url)
}

function openFile(url) {
  const fileUrl = getFileUrl(url)
  if (fileUrl) {
    window.open(fileUrl, '_blank')
  }
}

async function pay(row) {
  await ElMessageBox.confirm(`确认缴纳 ${row.paymentMonth} 账单 ${row.amount} 元？`, '缴费确认', {
    type: 'warning',
    confirmButtonText: '确认缴费',
    cancelButtonText: '取消',
  })
  await paySocialPayment(row.id)
  ElMessage.success('缴费成功')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <main class="social-page">
    <section v-loading="loading" class="content">
      <article class="panel apply-panel">
        <h2>社保参保申请</h2>
        <el-form label-position="top" class="apply-form">
          <el-form-item label="申请人">
            <el-input v-model="form.applicantName" />
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input v-model="form.idCard" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="参保类型">
            <el-select v-model="form.insuranceType">
              <el-option label="灵活就业人员养老保险" value="灵活就业人员养老保险" />
              <el-option label="灵活就业人员医疗保险" value="灵活就业人员医疗保险" />
            </el-select>
          </el-form-item>
          <el-form-item class="full-row" label="材料地址">
            <div class="material-upload-row">
              <el-upload
                :show-file-list="false"
                :http-request="handleMaterialUpload"
                :before-upload="beforeMaterialUpload"
                accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.webp"
              >
                <el-button :loading="materialUploading" :icon="UploadFilled">上传材料</el-button>
              </el-upload>
              <el-input
                v-model="form.materialUrl"
                placeholder="上传后自动生成，也可以填写已有材料地址"
              />
              <el-button
                v-if="form.materialUrl"
                :icon="Link"
                @click="openFile(form.materialUrl)"
              >
                查看
              </el-button>
            </div>
          </el-form-item>
        </el-form>
        <el-button type="primary" :loading="applying" @click="submitApply">提交申请</el-button>
      </article>

      <article class="panel">
        <h2>申请记录</h2>
        <el-table :data="applications" border>
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column prop="insuranceType" label="参保类型" min-width="180" />
          <el-table-column prop="applyTime" label="申请时间" min-width="180" />
          <el-table-column label="材料" width="100">
            <template #default="{ row }">
              <el-button
                v-if="row.materialUrl"
                link
                type="primary"
                @click="openFile(row.materialUrl)"
              >
                查看
              </el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.text || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditRemark" label="审核备注" min-width="160" />
        </el-table>
      </article>

      <article class="panel">
        <h2>缴费账单</h2>
        <el-table :data="payments" border>
          <el-table-column prop="billNo" label="账单编号" min-width="150" />
          <el-table-column prop="paymentMonth" label="缴费月份" width="120" />
          <el-table-column prop="amount" label="金额" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'warning'">
                {{ row.status === 1 ? '已缴费' : '待缴费' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="payTime" label="缴费时间" min-width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status === 1" @click="pay(row)">缴费</el-button>
            </template>
          </el-table-column>
        </el-table>
      </article>
    </section>
  </main>
</template>

<style scoped>
.social-page {
  min-height: calc(100vh - 124px);
}

.content {
  display: grid;
  width: 100%;
  gap: 18px;
  margin: 0;
}

.panel {
  padding: 22px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.panel h2 {
  margin: 0 0 18px;
  font-size: 20px;
}

.apply-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.full-row {
  grid-column: 1 / -1;
}

.material-upload-row {
  display: flex;
  width: 100%;
  gap: 10px;
  align-items: center;
}

.material-upload-row .el-input {
  min-width: 0;
  flex: 1;
}

@media (max-width: 900px) {
  .apply-form {
    grid-template-columns: 1fr;
  }

  .material-upload-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
