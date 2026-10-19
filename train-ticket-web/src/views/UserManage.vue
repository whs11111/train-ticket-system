<template>
  <div class="page">
    <div class="page-card">
      <div class="top-bar">
        <el-button class="back-btn" @click="$router.push('/adminHome')"> ← </el-button>

        <div class="title">用户管理</div>
      </div>

      <div class="table-box">
        <el-table
          :data="users"
          border
          stripe
          style="width: 100%"
          header-align="center"
          align="center"
        >
          <el-table-column prop="userId" label="ID" width="100" />

          <el-table-column label="用户名" width="160">
            <template #default="scope">
              <el-input v-model="scope.row.username" />
            </template>
          </el-table-column>

          <el-table-column label="真实姓名" width="140">
            <template #default="scope">
              <el-input v-model="scope.row.name" />
            </template>
          </el-table-column>

          <el-table-column label="手机号" width="170">
            <template #default="scope">
              <el-input v-model="scope.row.phone" maxlength="11" />
            </template>
          </el-table-column>

          <el-table-column label="身份证号" width="240">
            <template #default="scope">
              <el-input v-model="scope.row.idnum" maxlength="18" />
            </template>
          </el-table-column>

          <el-table-column label="角色" width="190">
            <template #default="scope">
              <el-select v-model="scope.row.role" style="width: 100%">
                <el-option label="普通用户" value="USER" />

                <el-option label="车站工作人员" value="STAFF" />

                <el-option label="管理员" value="ADMIN" />
              </el-select>
            </template>
          </el-table-column>

          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" type="primary" @click="update(scope.row)"> 保存 </el-button>

              <el-button size="small" type="danger" @click="remove(scope.row.userId)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref<any[]>([])

const load = async () => {
  const res = await request.get('/user/list')

  users.value = res.data
}

onMounted(() => {
  load()
})

const update = async (user: any) => {
  if (!/^\d{11}$/.test(user.phone)) {
    ElMessage.error('手机号必须为11位数字')
    return
  }

  if (!/^\d{17}(\d|X|x)$/.test(user.idnum)) {
    ElMessage.error('身份证格式不正确')
    return
  }

  if (!/^[\u4e00-\u9fa5·]+$/.test(user.name)) {
    ElMessage.error('真实姓名只能包含汉字')
    return
  }

  await request.put('/user/admin/update', user)

  ElMessage.success('修改成功')

  load()
}

const remove = async (id: number) => {
  await ElMessageBox.confirm('确定删除该用户？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })

  await request.delete(`/user/admin/${id}`)

  ElMessage.success('删除成功')

  load()
}
</script>

<style scoped>
.page {
  width: 100%;
  min-height: 100vh;
  padding: 40px 0;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f5f9ff 0%, #ffffff 35%);
}

.page-card {
  width: 1220px;
  margin: 0 auto;
  padding: 25px 30px 35px;
  box-sizing: border-box;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
}

.top-bar {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 55px;
  margin-bottom: 30px;
}

.back-btn {
  position: absolute;
  left: 0;
  width: 45px;
  height: 45px;
  font-size: 22px;
}

.title {
  font-size: 30px;
  font-weight: 700;
  color: #1f4e8c;
  letter-spacing: 2px;
  text-align: center;
}

.title::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  margin: 10px auto 0;
  background: #409eff;
  border-radius: 5px;
}

.table-box {
  width: 100%;
  overflow: hidden;
  border-radius: 10px;
}

:deep(.el-table) {
  border-radius: 10px;
  overflow: hidden;
}

:deep(.el-table th.el-table__cell) {
  background: #eaf3ff;
  color: #1f4e8c;
  font-weight: 600;
  height: 55px;
}

:deep(.el-table td.el-table__cell) {
  height: 65px;
}

:deep(.el-table--border) {
  border-color: #dcecff;
}

:deep(.el-table--border .el-table__cell) {
  border-color: #e5edf7;
}

:deep(.el-table__row:hover > td.el-table__cell) {
  background: #f8fbff !important;
}

:deep(.el-input) {
  width: 100%;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
}
</style>
