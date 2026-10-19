<template>
  <div class="page">
    <div class="page-card">
      <div class="top-bar">
        <el-button class="back-btn" @click="$router.push('/adminHome')">←</el-button>
        <div class="title">车站管理</div>
      </div>

      <div class="table-box">
        <el-table
          :data="stations"
          border
          stripe
          style="width: 100%"
          header-align="center"
          align="center"
        >
          <el-table-column prop="stationId" label="ID" width="80" />

          <el-table-column label="车站名称" width="240">
            <template #default="scope">
              <el-input v-model="scope.row.stationName" />
            </template>
          </el-table-column>

          <el-table-column label="所在城市" width="220">
            <template #default="scope">
              <el-input v-model="scope.row.city" />
            </template>
          </el-table-column>

          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" type="primary" @click="update(scope.row)"> 保存 </el-button>

              <el-button size="small" type="danger" @click="remove(scope.row.stationId)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="add-btn">
        <el-button type="primary" @click="add">新增车站</el-button>
      </div>

      <el-dialog v-model="dialogVisible" title="新增车站" width="450px">
        <el-form>
          <el-form-item label="车站名称">
            <el-input v-model="form.stationName" />
          </el-form-item>

          <el-form-item label="所在城市">
            <el-input v-model="form.city" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>

          <el-button type="primary" @click="submit">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const stations = ref<any[]>([])

const dialogVisible = ref(false)

const form = ref({
  stationName: '',
  city: '',
})

const load = async () => {
  const res = await request.get('/station/list')

  stations.value = res.data
}

onMounted(() => {
  load()
})

const add = () => {
  form.value = {
    stationName: '',
    city: '',
  }

  dialogVisible.value = true
}

const checkStationName = (stationName: string, stationId?: number) => {
  const name = String(stationName || '').trim()

  if (!name) {
    ElMessage.error('请输入车站名称')
    return false
  }

  if (!/^[\u4e00-\u9fa5]+$/.test(name)) {
    ElMessage.error('车站名称只能包含中文')
    return false
  }

  const exists = stations.value.some((station: any) => {
    if (stationId !== undefined && station.stationId === stationId) {
      return false
    }

    return String(station.stationName || '').trim() === name
  })

  if (exists) {
    ElMessage.error('车站名称已存在')
    return false
  }

  return true
}

const checkStation = (station: any) => {
  const stationName = String(station.stationName || '').trim()
  const city = String(station.city || '').trim()

  station.stationName = stationName
  station.city = city

  if (!checkStationName(stationName, station.stationId)) {
    return false
  }

  if (!city) {
    ElMessage.error('请输入所在城市')
    return false
  }

  if (!/^[\u4e00-\u9fa5]+$/.test(city)) {
    ElMessage.error('城市只能包含中文')
    return false
  }

  return true
}

const submit = async () => {
  const stationName = String(form.value.stationName || '').trim()
  const city = String(form.value.city || '').trim()

  form.value.stationName = stationName
  form.value.city = city

  if (!checkStationName(stationName)) {
    return
  }

  if (!city) {
    ElMessage.error('请输入所在城市')
    return
  }

  if (!/^[\u4e00-\u9fa5]+$/.test(city)) {
    ElMessage.error('城市只能包含中文')
    return
  }

  try {
    await request.post('/station/add', form.value)

    ElMessage.success('新增成功')

    dialogVisible.value = false

    load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '车站名称已存在')
  }
}

const update = async (station: any) => {
  if (!checkStation(station)) {
    return
  }

  try {
    await request.put('/station/update', station)

    ElMessage.success('修改成功')

    load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '车站名称已存在')
  }
}

const remove = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该车站？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    await request.delete(`/station/delete/${id}`)

    ElMessage.success('删除成功')

    load()
  } catch (e: any) {
    ElMessage.error('该车站正在被列车使用，不能删除')
  }
}
</script>

<style scoped>
.page {
  width: 100%;
  min-height: 100vh;
  padding: 40px 0;
  box-sizing: border-box;
  background: linear-gradient(180deg, #eaf4ff 0%, #ffffff 100%);
}

.page-card {
  width: 770px;
  margin: 0 auto;
  padding: 25px 30px 40px;
  box-sizing: border-box;
  background: #fff;
  border-radius: 8px;
}

.top-bar {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 35px;
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
  text-align: center;
  letter-spacing: 2px;
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

.table-box {
  width: 100%;
  overflow: hidden;
  border: 1px solid #dcecff;
  border-radius: 10px;
}

:deep(.el-table) {
  --el-table-header-bg-color: #eaf3ff;
  --el-table-row-hover-bg-color: #f8fbff;
}

:deep(.el-table th.el-table__cell) {
  background: #eaf3ff;
  color: #1f4e8c;
  font-weight: 600;
  height: 55px;
}

:deep(.el-table td.el-table__cell) {
  height: 58px;
}

:deep(.el-input) {
  width: 100%;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
}

.add-btn {
  margin-top: 20px;
}

.add-btn .el-button {
  height: 40px;
  padding: 0 22px;
  font-size: 15px;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  color: #1f4e8c;
}

:deep(.el-dialog__title) {
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>
