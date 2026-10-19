<template>
  <div class="register-page">
    <div class="register-card">
      <div class="header">
        <div class="title">用户注册</div>
      </div>

      <el-form :model="form" class="register-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            size="large"
          />
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入密码"
            size="large"
          />
        </el-form-item>

        <el-form-item>
          <el-input v-model="form.name" placeholder="请输入真实姓名" size="large" />
        </el-form-item>

        <el-form-item>
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" size="large" />
        </el-form-item>

        <el-form-item>
          <el-input v-model="form.idnum" placeholder="请输入身份证号" maxlength="18" size="large" />
        </el-form-item>

        <el-button class="register-button" size="large" @click="register"> 注册 </el-button>

        <div class="login-link">
          已有账号？
          <span @click="goLogin">立即登录</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
  idnum: '',
})

const register = async () => {
  if (!form.username) {
    ElMessage.error('请输入用户名')
    return
  }

  if (!form.password) {
    ElMessage.error('请输入密码')
    return
  }

  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  if (!/^[\u4e00-\u9fa5·]+$/.test(form.name)) {
    ElMessage.error('真实姓名只能包含汉字')
    return
  }

  if (!/^\d{11}$/.test(form.phone)) {
    ElMessage.error('手机号必须为11位数字')
    return
  }

  if (!/^\d{17}(\d|X|x)$/.test(form.idnum)) {
    ElMessage.error('身份证格式不正确')
    return
  }

  try {
    await request.post('/user/register', {
      username: form.username,
      password: form.password,
      name: form.name,
      phone: form.phone,
      idnum: form.idnum,
    })

    ElMessage.success('注册成功')

    router.push('/login')
  } catch (e) {
    ElMessage.error('注册失败，用户名可能已存在')
  }
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #eaf4ff 0%, #ffffff 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.register-card {
  width: 460px;
  background: #fff;
  border-radius: 14px;
  padding: 35px 45px 30px;
  box-sizing: border-box;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 30px;
  font-weight: 700;
  color: #1f4e8c;
  letter-spacing: 2px;
}

.subtitle {
  margin-top: 10px;
  color: #909399;
  font-size: 14px;
}

.register-form {
  width: 100%;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-button {
  width: 100%;
  height: 46px;
  margin-top: 5px;
  font-size: 18px;
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.register-button:hover,
.register-button:focus {
  background: #66b1ff;
  border-color: #66b1ff;
  color: #fff;
}

.login-link {
  margin-top: 22px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.login-link span {
  color: #409eff;
  cursor: pointer;
}

.login-link span:hover {
  color: #66b1ff;
}
</style>
