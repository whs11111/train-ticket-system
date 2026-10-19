<template>
  <div class="login">
    <div class="mask"></div>

    <el-card class="login-card">
      <div class="title">
        <h1>12704 火车票系统</h1>
      </div>

      <el-input v-model="username" placeholder="请输入用户名" size="large" class="input" />

      <el-input
        v-model="password"
        placeholder="请输入密码"
        type="password"
        size="large"
        class="input"
        show-password
      />

      <el-button type="primary" size="large" class="button" @click="login"> 登录 </el-button>

      <div class="register-link">
        还没有账号？
        <span @click="goRegister">立即注册</span>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '../api/request'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const username = ref('')
const password = ref('')

const userStore = useUserStore()

const router = useRouter()

const login = async () => {
  try {
    const res = await request.post('/user/login', {
      username: username.value,
      password: password.value,
    })

    userStore.setUser(res.data)

    if (res.data.role === 'ADMIN') {
      router.push('/adminHome')
    } else if (res.data.role === 'STAFF') {
      router.push('/staffHome')
    } else {
      router.push('/home')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '用户名或密码错误')
  }
}

const goRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #409eff, #67c23a);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.mask {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(2px);
}

.login-card {
  position: relative;
  width: 420px;
  padding: 20px;
  border-radius: 18px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.18);
  z-index: 1;
}

.title {
  text-align: center;
  margin-bottom: 30px;
}

.title h1 {
  margin: 0;
  color: #409eff;
  font-size: 28px;
  font-weight: bold;
}

.title p {
  margin-top: 8px;
  color: #999;
  font-size: 14px;
}

.input {
  margin-bottom: 22px;
}

.button {
  width: 100%;
  height: 45px;
  font-size: 17px;
  border-radius: 8px;
}

.button:hover {
  transform: translateY(-2px);
  transition: 0.25s;
}

.register-link {
  margin-top: 20px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.register-link span {
  color: #409eff;
  cursor: pointer;
  margin-left: 3px;
}

.register-link span:hover {
  color: #66b1ff;
}
</style>
