<template>
  <div class="page">
    <div class="top-bar">
      <el-button class="back-btn" @click="$router.push('/adminHome')"> ← </el-button>

      <h2>订单管理</h2>
    </div>

    <div class="table-card">
      <el-table
        :data="orders"
        border
        stripe
        style="width: 100%"
        header-align="center"
        align="center"
      >
        <el-table-column prop="orderId" label="订单ID" width="80" />

        <el-table-column prop="username" label="用户名" width="120" />

        <el-table-column prop="name" label="真实姓名" width="100" />

        <el-table-column prop="phone" label="手机号" width="130" />

        <el-table-column prop="idnum" label="身份证号" width="190" />

        <el-table-column prop="trainNo" label="车次" width="80" />

        <el-table-column prop="runDate" label="开行日期" width="120" />

        <el-table-column label="区间" width="160">
          <template #default="scope">
            {{ scope.row.startStationName }} → {{ scope.row.endStationName }}
          </template>
        </el-table-column>

        <el-table-column prop="seatInfo" label="座位号" width="150" />

        <el-table-column prop="price" label="金额" width="90" />

        <el-table-column prop="status" label="状态" width="100" />

        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ scope.row.createTime.replace('T', ' ') }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../api/request'

const orders = ref<any[]>([])

const load = async () => {
  const res = await request.get('/order/list')

  orders.value = res.data
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.page {
  width: 1520px;
  margin: 40px auto;
}

.top-bar {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 25px;
}

.top-bar h2 {
  font-size: 30px;
  font-weight: 700;
  color: #1f4e8c;
  letter-spacing: 2px;
  margin: 0;
}

.top-bar h2::after {
  content: '';
  display: block;
  width: 55px;
  height: 4px;
  margin: 10px auto 0;
  background: #409eff;
  border-radius: 5px;
}

.back-btn {
  position: absolute;
  left: 0;
  width: 45px;
  height: 40px;
  font-size: 22px;
  border-radius: 8px;
}

.table-card {
  background: #fff;
  border-radius: 14px;
  padding: 8px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

:deep(.el-table) {
  border-radius: 10px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #eaf3ff;
  color: #1f4e8c;
  font-weight: 600;
}

:deep(.el-table td) {
  height: 55px;
}
</style>
