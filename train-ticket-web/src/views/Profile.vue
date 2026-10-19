<template>
  <div class="profile">
    <div class="top-bar">
      <el-button class="back-btn" @click="goBack"> ← </el-button>

      <h2 class="title">个人中心</h2>

      <el-button class="edit-btn" @click="edit"> 修改信息 </el-button>
    </div>

    <div class="info-card">
      <div class="row">
        <span>用户名</span>
        <span>{{ user.username }}</span>
      </div>

      <div class="row">
        <span>真实姓名</span>
        <span>{{ user.name }}</span>
      </div>

      <div class="row">
        <span>手机号</span>
        <span>{{ user.phone }}</span>
      </div>

      <div class="row">
        <span>身份证号</span>
        <span>{{ user.idnum }}</span>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="修改个人信息" width="450px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.name" />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>

        <el-form-item label="身份证号">
          <el-input v-model="editForm.idnum" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false"> 取消 </el-button>

        <el-button type="primary" @click="save"> 保存 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useUserStore } from '../stores/user'
import request from '../api/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const userStore = useUserStore()

const user = ref<any>({})

const dialogVisible = ref(false)

const editForm = reactive({
  userId: 0,

  name: '',

  phone: '',

  idnum: '',
})

const loadUser = async () => {
  const userId = userStore.user.userId

  const res = await request.get('/user/info', {
    params: {
      userId,
    },
  })

  user.value = res.data
}

const edit = () => {
  editForm.userId = user.value.userId

  editForm.name = user.value.name

  editForm.phone = user.value.phone

  editForm.idnum = user.value.idnum

  dialogVisible.value = true
}

const save = async () => {
  if (!/^[\u4e00-\u9fa5]{2,10}$/.test(editForm.name)) {
    ElMessage.error('真实姓名只能包含汉字')
    return
  }

  if (!/^1\d{10}$/.test(editForm.phone)) {
    ElMessage.error('手机号必须为11位数字')
    return
  }

  if (!/^\d{17}[\dXx]$/.test(editForm.idnum)) {
    ElMessage.error('身份证号格式错误')
    return
  }

  try {
    await request.put('/user/update', editForm)

    ElMessage.success('修改成功')

    dialogVisible.value = false

    loadUser()
  } catch (e) {
    ElMessage.error('修改失败')
  }
}

onMounted(() => {
  loadUser()
})

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.profile {
  width: 700px;
  min-height: 100vh;
  margin: 0 auto;
  padding-top: 40px;
  position: relative;
}

.profile::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #eaf4ff 0%, #ffffff 100%);
  z-index: -1;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 35px;
}

.back-btn {
  position: absolute;
  left: 25px;
  width: 42px;
  height: 38px;
  padding: 0;
  border-radius: 10px;
}

.edit-btn {
  position: absolute;
  right: 25px;
  height: 38px;
  padding: 0 18px;
  border-radius: 10px;
}

.title {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
  color: #1f4e8c;
  letter-spacing: 2px;
  line-height: 40px;
}

.title::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  margin: 12px auto 0;
  background: #409eff;
  border-radius: 5px;
}

.info-card {
  width: 620px;
  margin: 0 auto;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 5px 18px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: 0.3s;
}

.info-card:hover {
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.12);
}

.row {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 35px;
  border-bottom: 1px solid #ebeef5;
  font-size: 16px;
}

.row:last-child {
  border-bottom: none;
}

.row span:first-child {
  color: #606266;
  font-weight: 600;
}

.row span:last-child {
  color: #303133;
}

:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__title) {
  font-size: 22px;
  font-weight: 600;
  color: #1f4e8c;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
}

:deep(.el-button) {
  border-radius: 10px;
}
</style>
