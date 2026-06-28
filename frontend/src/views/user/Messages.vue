<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Refresh } from '@element-plus/icons-vue'
import { deleteMessage, getMessages, readMessage, sendMessage } from '../../api/message'

const loading = ref(false)
const sending = ref(false)
const messages = ref([])

const form = reactive({
  receiverType: 2,
  receiverId: 1,
  title: '',
  content: '',
})

async function loadMessages() {
  loading.value = true
  try {
    const res = await getMessages()
    messages.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function markRead(row) {
  await readMessage(row.id)
  ElMessage.success('已标记为已读')
  loadMessages()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除消息“${row.title || '无标题'}”？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  await deleteMessage(row.id)
  ElMessage.success('删除成功')
  loadMessages()
}

async function submitMessage() {
  if (!form.receiverId || !form.content) {
    ElMessage.warning('请填写接收方 ID 和消息内容')
    return
  }
  sending.value = true
  try {
    await sendMessage(form)
    ElMessage.success('发送成功')
    form.title = ''
    form.content = ''
  } finally {
    sending.value = false
  }
}

function senderTypeText(type) {
  return type === 2 ? '企业' : type === 3 ? '管理员' : '用户'
}

onMounted(loadMessages)
</script>

<template>
  <main class="messages-page">
    <section class="content">
      <article class="panel message-list">
        <div class="panel-head">
          <div>
            <h2>我的消息</h2>
            <span>未读消息会显示为醒目状态</span>
          </div>
          <el-button :icon="Refresh" @click="loadMessages">刷新</el-button>
        </div>

        <el-table v-loading="loading" :data="messages" border>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.isRead === 1 ? 'info' : 'danger'">
                {{ row.isRead === 1 ? '已读' : '未读' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发送方" width="120">
            <template #default="{ row }">
              {{ senderTypeText(row.senderType) }} {{ row.senderId }}
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
          <el-table-column prop="createTime" label="时间" min-width="180" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Check" :disabled="row.isRead === 1" @click="markRead(row)">
                已读
              </el-button>
              <el-button link type="danger" :icon="Delete" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </article>

      <aside class="panel send-panel">
        <h2>发送消息</h2>
        <el-form label-position="top">
          <el-form-item label="接收方类型">
            <el-radio-group v-model="form.receiverType">
              <el-radio :label="2">企业</el-radio>
              <el-radio :label="3">管理员</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="接收方 ID">
            <el-input-number v-model="form.receiverId" :min="1" />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="form.title" placeholder="请输入消息标题" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入消息内容" />
          </el-form-item>
        </el-form>
        <el-button type="primary" :loading="sending" @click="submitMessage">发送</el-button>
      </aside>
    </section>
  </main>
</template>

<style scoped>
.messages-page {
  min-height: calc(100vh - 124px);
}

.content {
  display: grid;
  width: 100%;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 18px;
  margin: 0;
  align-items: start;
}

.panel {
  padding: 22px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.panel h2 {
  margin: 0;
  font-size: 20px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.panel-head span {
  display: inline-block;
  margin-top: 6px;
  color: #64748b;
  font-size: 14px;
}

.send-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.send-panel .el-button {
  width: 100%;
}

@media (max-width: 1000px) {
  .content {
    grid-template-columns: 1fr;
  }

  .panel-head {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }
}
</style>
