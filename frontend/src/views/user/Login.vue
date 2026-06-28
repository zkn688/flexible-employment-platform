<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha, login } from '../../api/user'

const router = useRouter()
const loading = ref(false)
const captchaLoading = ref(false)
const captchaImage = ref('')

const form = reactive({
  username: 'user',
  password: '123456',
  captchaId: '',
  captchaCode: '',
})

async function loadCaptcha() {
  captchaLoading.value = true
  try {
    const res = await getCaptcha('login')
    form.captchaId = res.data.captchaId
    form.captchaCode = ''
    captchaImage.value = res.data.image
  } finally {
    captchaLoading.value = false
  }
}

async function handleLogin() {
  if (!form.username || !form.password || !form.captchaCode) {
    ElMessage.warning('请输入用户名、密码和验证码')
    return
  }
  loading.value = true
  try {
    const res = await login(form)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    ElMessage.success('登录成功')
    router.push('/user/home')
  } catch (error) {
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="brand">
        <div class="brand-mark">灵</div>
        <div>
          <h1>灵活就业服务平台</h1>
          <p>用户端</p>
        </div>
      </div>

      <el-form class="login-form" label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input
              v-model.trim="form.captchaCode"
              size="large"
              maxlength="4"
              placeholder="请输入验证码"
              @keyup.enter="handleLogin"
            />
            <button class="captcha-image" type="button" :disabled="captchaLoading" @click="loadCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else>刷新</span>
            </button>
          </div>
        </el-form-item>
        <el-button class="login-button" type="primary" size="large" :loading="loading" @click="handleLogin">
          登录
        </el-button>
        <div class="form-footer">
          <span>还没有账号？</span>
          <el-button link type="primary" @click="router.push('/user/register')">立即注册</el-button>
        </div>
      </el-form>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    linear-gradient(rgba(18, 31, 48, 0.55), rgba(18, 31, 48, 0.45)),
    url("https://images.unsplash.com/photo-1551836022-d5d88e9218df?auto=format&fit=crop&w=1600&q=80") center/cover;
}

.login-panel {
  width: min(420px, 100%);
  padding: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.25);
}

.brand {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 28px;
}

.brand-mark {
  display: grid;
  width: 52px;
  height: 52px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: #2563eb;
  font-size: 24px;
  font-weight: 700;
}

.brand h1 {
  margin: 0;
  font-size: 24px;
  line-height: 1.25;
}

.brand p {
  margin: 6px 0 0;
  color: #64748b;
}

.login-form {
  margin-top: 8px;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 124px;
  gap: 10px;
  width: 100%;
}

.captcha-image {
  display: grid;
  height: 40px;
  place-items: center;
  padding: 0;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #eff6ff;
  cursor: pointer;
}

.captcha-image img {
  display: block;
  width: 124px;
  height: 44px;
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 16px;
  color: #64748b;
  font-size: 14px;
}
</style>
