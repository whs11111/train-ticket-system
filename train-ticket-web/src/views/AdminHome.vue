<template>
  <div class="admin-home">
    <div class="welcome-user">
      <span class="welcome-icon">👤</span>
      <span>欢迎，{{ userName }}</span>
    </div>

    <h1 class="title">欢迎来到12704火车票购票系统后台！</h1>

    <div class="nav-bar">
      <div class="nav-item active" @click="goHome">首页</div>

      <div class="nav-item" @click="goUserManage">用户管理</div>

      <div class="nav-item" @click="goTrainManage">车次管理</div>

      <div class="nav-item" @click="goStationManage">车站管理</div>

      <div class="nav-item" @click="goOrderManage">订单管理</div>

      <div class="nav-item logout" @click="logout">退出登录</div>
    </div>

    <div class="welcome">
      <h2>欢迎进入管理员后台</h2>

      <p>请选择上方功能进行管理</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import request from '../api/request'

const router = useRouter()

const userStore = useUserStore()

const userName = computed(() => {
  const user: any = userStore.user

  return user?.username || user?.userName || user?.name || user?.nickname || '用户'
})

const goHome = () => {
  router.push('/adminHome')
}

const goUserManage = () => {
  router.push('/userManage')
}

const goTrainManage = () => {
  router.push('/trainManage')
}

const goStationManage = () => {
  router.push('/stationManage')
}

const goOrderManage = () => {
  router.push('/orderManage')
}

const logout = async () => {
  await request.post('/user/logout')

  userStore.logout()

  router.push('/login')
}
</script>

<style scoped>
.admin-home {
  position: relative;
  min-height: 100vh;
  padding: 35px 50px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f5f9ff 0%, #ffffff 35%);
}
.welcome-user {
  position: absolute;
  top: 60px;
  right: 60px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 16px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #e4edf8;
  border-radius: 22px;
  box-shadow: 0 4px 15px rgba(31, 78, 140, 0.08);
  color: #1f4e8c;
  font-size: 15px;
  font-weight: 600;
  user-select: none;
  white-space: nowrap;
}
.welcome-icon {
  font-size: 20px;
  line-height: 1;
}

.title {
  text-align: center;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #1f4e8c;
  margin: 10px 0 25px;
}

.nav-bar {
  min-height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 38px;
  padding: 0 25px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 35px;
  box-sizing: border-box;
}

.nav-item {
  height: 60px;
  line-height: 60px;
  font-size: 17px;
  color: #606266;
  cursor: pointer;
  position: relative;
  transition: 0.3s;
  white-space: nowrap;
}

.nav-item:hover {
  color: #409eff;
}

.nav-item.active {
  color: #409eff;
  font-weight: 600;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 10%;
  width: 80%;
  height: 3px;
  background: #409eff;
  border-radius: 5px;
}

.logout {
  color: #f56c6c;
}

.logout:hover {
  color: #ff7875;
}

.welcome {
  width: 92%;
  min-height: 360px;
  margin: 0 auto;
  background: white;
  border-radius: 18px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
}

.welcome h2 {
  margin: 0 0 20px;
  font-size: 34px;
  font-weight: 700;
  color: #303133;
}

.welcome p {
  margin: 0;
  color: #909399;
  font-size: 18px;
}
</style>
