<template>
  <div class="order-page">
    <el-card class="ticket-card">
      <div class="header-box">
        <el-button class="back-btn" @click="goBack"> ← </el-button>

        <div class="title">
          {{ pageTitle }}
        </div>
      </div>

      <div class="train-info">
        <div class="station">
          <div class="station-time">
            {{ formatTime(departureTime) }}
          </div>

          <div class="station-name">
            {{ startStation }}
          </div>
        </div>

        <div class="middle-info">
          <div class="train-no">
            {{ trainNo }}
          </div>

          <div class="arrow">─────────►</div>

          <div class="duration">
            {{ duration }}
          </div>
        </div>

        <div class="station">
          <div class="station-time">
            {{ formatTime(arrivalTime) }}
          </div>

          <div class="station-name">
            {{ endStation }}
          </div>
        </div>
      </div>

      <div class="date-info">乘车日期：{{ date }}</div>

      <el-divider />

      <div class="seat-type-box">
        <div
          v-for="item in prices"
          :key="item.seatTypeId"
          class="seat-type-card"
          :class="{ active: Number(item.seatTypeId) === currentSeatTypeId }"
          @click="changeSeatType(item)"
        >
          <div class="type-name">
            {{ item.seatType }}
          </div>

          <div class="type-price">￥{{ item.price }}</div>

          <div class="type-remain">余{{ item.remain }}张</div>
        </div>
      </div>

      <el-divider />

      <div class="seat-title">请选择座位</div>

      <div class="seat-tip">不选择座位将由系统自动分配</div>

      <div class="seat-map">
        <div class="seat-row">
          <div
            v-for="seat in leftSeats"
            :key="seat"
            class="seat"
            :class="{ active: selectedSeat === seat }"
            @click="selectSeat(seat)"
          >
            {{ seat }}
          </div>

          <div class="aisle">过道</div>

          <div
            v-for="seat in rightSeats"
            :key="seat"
            class="seat"
            :class="{ active: selectedSeat === seat }"
            @click="selectSeat(seat)"
          >
            {{ seat }}
          </div>
        </div>
      </div>

      <div class="selected-info">
        <span v-if="selectedSeat">
          当前选择：
          <b>{{ selectedSeat }}</b>
        </span>

        <span v-else> 当前未选择座位，系统将自动分配 </span>
      </div>

      <div class="btn-box">
        <el-button type="primary" @click="submitOrder">
          {{ submitText }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute()

const router = useRouter()

const userStore = useUserStore()

const pageTitle = route.query.type === 'change' ? '确认改签' : '确认订单'

const submitText = route.query.type === 'change' ? '提交改签' : '提交订单'

const paymentPath = route.query.type === 'change' ? '/changePayment' : '/payment'

const prices = JSON.parse((route.query.prices as string) || '[]')

const scheduleId = Number(route.query.scheduleId)

const trainNo = route.query.trainNo as string

const startStation = route.query.startStation as string

const endStation = route.query.endStation as string

const departureTime = route.query.departureTime as string

const arrivalTime = route.query.arrivalTime as string

const date = route.query.date as string

const seatTypeId = Number(route.query.seatTypeId)

const currentSeatTypeId = ref(seatTypeId)

const leftSeats = ['A', 'B', 'C']

const rightSeats = ['E', 'F']

const selectedSeat = ref('')

const selectSeat = (seat: string) => {
  if (selectedSeat.value === seat) {
    selectedSeat.value = ''
  } else {
    selectedSeat.value = seat
  }
}

const changeSeatType = (item: any) => {
  currentSeatTypeId.value = Number(item.seatTypeId)
}

const formatTime = (time: string) => {
  if (!time) return ''

  return time.substring(0, 5)
}

const duration = (() => {
  const start = departureTime.split(':')

  const end = arrivalTime.split(':')

  const s = Number(start[0]) * 60 + Number(start[1])

  const e = Number(end[0]) * 60 + Number(end[1])

  const diff = e - s

  const h = Math.floor(diff / 60)

  const m = diff % 60

  return `${h}小时${m}分`
})()

const submitOrder = async () => {
  try {
    const res = await request.post('/order/create', {
      scheduleId,

      seatTypeId: currentSeatTypeId.value,

      startStation,

      endStation,

      departureTime,

      arrivalTime,

      seatColumn: selectedSeat.value || null,

      userId: userStore.user.userId,
    })

    ElMessage.success('提交订单成功')

    router.push({
      path: paymentPath,
      query: {
        orderId: res.data.orderId,

        trainNo: res.data.trainNo,

        startStation: res.data.startStation,

        endStation: res.data.endStation,

        departureTime: res.data.departureTime,

        arrivalTime: res.data.arrivalTime,

        seatType: res.data.seatType,

        coachNo: res.data.coachNo,

        seatNo: res.data.seatNo,

        name: res.data.name,

        price: res.data.price,

        seatChanged: res.data.seatChanged,

        message: res.data.message,

        date,

        createTime: res.data.createTime,

        type: route.query.type,

        oldOrderId: route.query.oldOrderId,

        oldPrice: route.query.oldPrice,
      },
    })
  } catch (e) {
    ElMessage.error('提交订单失败')
  }
}

const goBack = () => {
  router.push('/home')
}
</script>

<style scoped>
.order-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #eaf4ff 0%, #ffffff 100%);
  padding: 40px 0;
}

.ticket-card {
  width: 900px;
  margin: auto;
}

.title {
  font-size: 30px;
  font-weight: 700;
  color: #1f4e8c;
  text-align: center;
  letter-spacing: 2px;
  margin-bottom: 25px;
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

.train-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 40px;
  margin: 10px 20px;
}

.middle-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  transform: translateY(-13px);
}

.train-no {
  font-size: 22px;
  font-weight: bold;
  color: #409eff;
  margin: 0;
  line-height: 1;
}

.arrow {
  font-size: 28px;
  color: black;
  margin: -8px 0 -6px 0;
  line-height: 1;
}

.duration {
  font-size: 16px;
  color: black;
  margin: 0;
  line-height: 1;
}

.station-box {
  display: flex;
  align-items: center;
  gap: 50px;
}

.station {
  width: 180px;
  text-align: center;
}

.station-name {
  font-size: 22px;
  font-weight: bold;
}

.station-time {
  font-size: 48px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.seat-title {
  margin-top: -15px;
  text-align: center;
  font-size: 22px;
  font-weight: bold;
}

.seat-tip {
  margin-top: 6px;
  margin-bottom: 20px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.seat-map {
  display: flex;
  justify-content: center;
}

.seat-row {
  display: flex;
  align-items: center;
}

.seat {
  width: 70px;
  height: 70px;
  margin: 0 8px;
  border: 1px solid #afafaf;
  border-radius: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 26px;
  cursor: pointer;
  transition: 0.2s;
  user-select: none;
}

.seat:hover {
  background: #ecf5ff;
}

.seat.active {
  background: #f19202;
  color: #fff;
}

.aisle {
  width: 70px;
  text-align: center;
  color: #8e8e8e;
  font-size: 16px;
}

.selected-info {
  margin-top: 20px;
  text-align: center;
  font-size: 15px;
}

.selected-info b {
  color: #f19202;
}

.btn-box {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.btn-box .el-button {
  width: 300px;
  height: 46px;
  font-size: 16px;
}

.header-box {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 55px;
}

.back-btn {
  position: absolute;
  left: 0;
  font-size: 22px;
  width: 45px;
  height: 45px;
}

.title {
  font-size: 30px;
  font-weight: 700;
  color: #1f4e8c;
  text-align: center;
  letter-spacing: 2px;
  margin: 0;
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

.date-info {
  text-align: center;
  font-size: 20px;
  color: black;
  margin-top: -10px;
}

.seat-type-box {
  display: flex;
  justify-content: center;
  gap: 70px;
  margin: 20px 0;
}

.seat-type-card {
  width: 130px;
  padding: 12px 0;
  text-align: center;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
}

.seat-type-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.type-name {
  font-size: 18px;
  font-weight: bold;
}

.type-price {
  margin-top: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #ff9800;
}

.type-remain {
  margin-top: 6px;
  color: #67c23a;
  font-size: 14px;
}
</style>
