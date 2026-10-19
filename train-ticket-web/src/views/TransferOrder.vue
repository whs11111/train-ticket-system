<template>
  <div class="order-page">
    <el-card class="ticket-card">
      <div class="header-box">
        <el-button class="back-btn" @click="goBack"> ← </el-button>

        <div class="title">
          {{ pageTitle }}
        </div>
      </div>

      <div class="transfer-top-info">
        <div class="transfer-top-station">
          {{ firstStartStation }}
        </div>

        <div class="transfer-top-middle">
          <div class="transfer-top-detail transfer-station">中转 {{ transferStation }}</div>

          <div class="transfer-top-arrow">─────────►</div>

          <div class="transfer-top-detail total-duration">
            {{ totalDuration }}
          </div>
        </div>

        <div class="transfer-top-station">
          {{ secondEndStation }}
        </div>
      </div>

      <div class="date-info">乘车日期：{{ date }}</div>

      <el-divider />

      <div class="transfer-leg">
        <div class="leg-title">第一程</div>

        <div class="leg-train-info">
          <div class="leg-station">
            <div class="leg-station-time">
              {{ formatTime(firstDepartureTime) }}
            </div>

            <div class="leg-station-name">
              {{ firstStartStation }}
            </div>
          </div>

          <div class="leg-middle-info">
            <div class="leg-train-no">
              {{ firstTrainNo }}
            </div>

            <div class="leg-arrow">─────────►</div>

            <div class="leg-duration">
              {{ firstDuration }}
            </div>
          </div>

          <div class="leg-station">
            <div class="leg-station-time">
              {{ formatTime(firstArrivalTime) }}
            </div>

            <div class="leg-station-name">
              {{ firstEndStation }}
            </div>
          </div>
        </div>

        <div class="seat-type-box">
          <div
            v-for="item in firstSeats"
            :key="item.seatTypeId"
            class="seat-type-card"
            :class="{
              active: Number(item.seatTypeId) === firstSeatTypeId,
            }"
            @click="changeFirstSeatType(item)"
          >
            <div class="type-name">
              {{ item.seatType }}
            </div>

            <div class="type-price">￥{{ item.price }}</div>

            <div class="type-remain">余{{ item.remain }}张</div>
          </div>
        </div>

        <div class="seat-title">请选择座位</div>

        <div class="seat-tip">不选择座位将由系统自动分配</div>

        <div class="seat-map">
          <div class="seat-row">
            <div
              v-for="seat in leftSeats"
              :key="seat"
              class="seat"
              :class="{ active: firstSelectedSeat === seat }"
              @click="selectFirstSeat(seat)"
            >
              {{ seat }}
            </div>

            <div class="aisle">过道</div>

            <div
              v-for="seat in rightSeats"
              :key="seat"
              class="seat"
              :class="{ active: firstSelectedSeat === seat }"
              @click="selectFirstSeat(seat)"
            >
              {{ seat }}
            </div>
          </div>
        </div>

        <div class="selected-info">
          <span v-if="firstSelectedSeat">
            当前选择：
            <b>{{ firstSelectedSeat }}</b>
          </span>

          <span v-else> 当前未选择座位，系统将自动分配 </span>
        </div>
      </div>

      <el-divider />

      <div class="transfer-leg">
        <div class="leg-title">第二程</div>

        <div class="leg-train-info">
          <div class="leg-station">
            <div class="leg-station-time">
              {{ formatTime(secondDepartureTime) }}
            </div>

            <div class="leg-station-name">
              {{ secondStartStation }}
            </div>
          </div>

          <div class="leg-middle-info">
            <div class="leg-train-no">
              {{ secondTrainNo }}
            </div>

            <div class="leg-arrow">─────────►</div>

            <div class="leg-duration">
              {{ secondDuration }}
            </div>
          </div>

          <div class="leg-station">
            <div class="leg-station-time">
              {{ formatTime(secondArrivalTime) }}
            </div>

            <div class="leg-station-name">
              {{ secondEndStation }}
            </div>
          </div>
        </div>

        <div class="seat-type-box">
          <div
            v-for="item in secondSeats"
            :key="item.seatTypeId"
            class="seat-type-card"
            :class="{
              active: Number(item.seatTypeId) === secondSeatTypeId,
            }"
            @click="changeSecondSeatType(item)"
          >
            <div class="type-name">
              {{ item.seatType }}
            </div>

            <div class="type-price">￥{{ item.price }}</div>

            <div class="type-remain">余{{ item.remain }}张</div>
          </div>
        </div>

        <div class="seat-title">请选择座位</div>

        <div class="seat-tip">不选择座位将由系统自动分配</div>

        <div class="seat-map">
          <div class="seat-row">
            <div
              v-for="seat in leftSeats"
              :key="seat"
              class="seat"
              :class="{ active: secondSelectedSeat === seat }"
              @click="selectSecondSeat(seat)"
            >
              {{ seat }}
            </div>

            <div class="aisle">过道</div>

            <div
              v-for="seat in rightSeats"
              :key="seat"
              class="seat"
              :class="{ active: secondSelectedSeat === seat }"
              @click="selectSecondSeat(seat)"
            >
              {{ seat }}
            </div>
          </div>
        </div>

        <div class="selected-info">
          <span v-if="secondSelectedSeat">
            当前选择：
            <b>{{ secondSelectedSeat }}</b>
          </span>

          <span v-else> 当前未选择座位，系统将自动分配 </span>
        </div>
      </div>

      <div class="btn-box">
        <el-button type="primary" @click="submitTransferOrder">
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

const pageTitle = '确认订单'
const submitText = '提交订单'
const paymentPath = '/transferPayment'

const date = route.query.date as string

const leftSeats = ['A', 'B', 'C']
const rightSeats = ['E', 'F']

const firstScheduleId = Number(route.query.firstScheduleId)
const firstTrainNo = route.query.firstTrainNo as string
const firstStartStation = route.query.firstStartStation as string
const firstEndStation = route.query.firstEndStation as string
const firstDepartureTime = route.query.firstDepartureTime as string
const firstArrivalTime = route.query.firstArrivalTime as string

const firstSeats = JSON.parse((route.query.firstSeats as string) || '[]')

const firstSeatTypeId = ref(firstSeats.length > 0 ? Number(firstSeats[0].seatTypeId) : 0)

const firstSelectedSeat = ref('')

const secondScheduleId = Number(route.query.secondScheduleId)
const secondTrainNo = route.query.secondTrainNo as string
const secondStartStation = route.query.secondStartStation as string
const secondEndStation = route.query.secondEndStation as string
const secondDepartureTime = route.query.secondDepartureTime as string
const secondArrivalTime = route.query.secondArrivalTime as string

const secondSeats = JSON.parse((route.query.secondSeats as string) || '[]')

const secondSeatTypeId = ref(secondSeats.length > 0 ? Number(secondSeats[0].seatTypeId) : 0)

const secondSelectedSeat = ref('')

const transferStation = firstEndStation

const formatTime = (time: string) => {
  if (!time) return ''

  return time.substring(0, 5)
}

const calculateDuration = (departure: string, arrival: string) => {
  if (!departure || !arrival) return ''

  const start = departure.split(':')
  const end = arrival.split(':')

  const s = Number(start[0]) * 60 + Number(start[1])
  const e = Number(end[0]) * 60 + Number(end[1])

  let diff = e - s

  if (diff < 0) {
    diff += 24 * 60
  }

  const h = Math.floor(diff / 60)
  const m = diff % 60

  return `${h}小时${m}分`
}

const firstDuration = calculateDuration(firstDepartureTime, firstArrivalTime)

const secondDuration = calculateDuration(secondDepartureTime, secondArrivalTime)

const totalDuration = calculateDuration(firstDepartureTime, secondArrivalTime)

const changeFirstSeatType = (item: any) => {
  firstSeatTypeId.value = Number(item.seatTypeId)
}

const changeSecondSeatType = (item: any) => {
  secondSeatTypeId.value = Number(item.seatTypeId)
}

const selectFirstSeat = (seat: string) => {
  if (firstSelectedSeat.value === seat) {
    firstSelectedSeat.value = ''
  } else {
    firstSelectedSeat.value = seat
  }
}

const selectSecondSeat = (seat: string) => {
  if (secondSelectedSeat.value === seat) {
    secondSelectedSeat.value = ''
  } else {
    secondSelectedSeat.value = seat
  }
}

const submitTransferOrder = async () => {
  try {
    const res = await request.post('/order/createTransfer', {
      userId: userStore.user.userId,
      date,

      firstScheduleId,
      firstTrainNo,
      firstStartStation,
      firstEndStation,
      firstDepartureTime,
      firstArrivalTime,
      firstSeatTypeId: firstSeatTypeId.value,
      firstSeatColumn: firstSelectedSeat.value || null,

      secondScheduleId,
      secondTrainNo,
      secondStartStation,
      secondEndStation,
      secondDepartureTime,
      secondArrivalTime,
      secondSeatTypeId: secondSeatTypeId.value,
      secondSeatColumn: secondSelectedSeat.value || null,
    })

    ElMessage.success('提交订单成功')

    router.push({
      path: paymentPath,
      query: {
        transfer: 'true',

        firstOrderId: res.data.firstOrderId,
        secondOrderId: res.data.secondOrderId,

        firstTrainNo: res.data.firstTrainNo,
        secondTrainNo: res.data.secondTrainNo,

        firstStartStation: res.data.firstStartStation,
        firstEndStation: res.data.firstEndStation,

        secondStartStation: res.data.secondStartStation,
        secondEndStation: res.data.secondEndStation,

        firstDepartureTime: res.data.firstDepartureTime,
        firstArrivalTime: res.data.firstArrivalTime,

        secondDepartureTime: res.data.secondDepartureTime,
        secondArrivalTime: res.data.secondArrivalTime,

        firstSeatType: res.data.firstSeatType,
        secondSeatType: res.data.secondSeatType,

        firstCoachNo: res.data.firstCoachNo,
        secondCoachNo: res.data.secondCoachNo,

        firstSeatNo: res.data.firstSeatNo,
        secondSeatNo: res.data.secondSeatNo,

        firstPrice: res.data.firstPrice,
        secondPrice: res.data.secondPrice,

        name: res.data.name,

        date,

        firstCreateTime: res.data.firstCreateTime,
        secondCreateTime: res.data.secondCreateTime,

        firstSeatChanged: res.data.firstSeatChanged,
        secondSeatChanged: res.data.secondSeatChanged,

        message: res.data.message,

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

.transfer-top-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 25px;
  margin: 10px 20px 25px;
}

.transfer-top-station {
  width: 180px;
  text-align: center;
  font-size: 35px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.transfer-top-middle {
  width: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.transfer-top-detail {
  width: 100%;
  text-align: center;
  font-size: 16px;
  color: black;
  line-height: 1;
}

.transfer-station {
  margin-bottom: -5px;
  color: #606266;
}

.transfer-top-arrow {
  font-size: 28px;
  color: black;
  line-height: 1;
  margin: 0;
}

.total-duration {
  margin-top: -5px;
}

.date-info {
  text-align: center;
  font-size: 17px;
  color: black;
  margin-top: 10px;
}

.seat-type-box {
  display: flex;
  justify-content: center;
  gap: 70px;
  margin: 35px 0 30px;
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

.seat-title {
  margin-top: 0;
  text-align: center;
  font-size: 21px;
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
  font-size: 17px;
}

.selected-info b {
  color: #f19202;
}

.btn-box {
  margin-top: 20px;
  margin-bottom: 10px;
  display: flex;
  justify-content: center;
}

.btn-box .el-button {
  width: 300px;
  height: 46px;
  font-size: 16px;
}

.transfer-leg {
  margin: 10px 20px 25px;
}

.leg-title {
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  color: #1f4e8c;
  margin-bottom: 18px;
}

.leg-train-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 40px;
  margin: 10px 20px 35px;
}

.leg-station {
  width: 180px;
  text-align: center;
  transform: translateY(12px);
}

.leg-station-time {
  font-size: 42px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.leg-station-name {
  margin-top: 8px;
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}

.leg-middle-info {
  margin-top: -15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  transform: translateY(0);
}

.leg-train-no {
  font-size: 22px;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
}

.leg-arrow {
  font-size: 28px;
  color: black;
  margin: -8px 0 -6px 0;
  line-height: 1;
}

.leg-duration {
  font-size: 16px;
  color: black;
  line-height: 1;
}
</style>
