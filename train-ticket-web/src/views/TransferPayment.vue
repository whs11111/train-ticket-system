<template>
  <div class="payment">
    <div class="payment-card">
      <div class="header-box">
        <el-button class="back-btn" @click="cancelOrder()"> ← </el-button>

        <div class="title">确认支付</div>
      </div>

      <div class="count-down">
        请在
        <span>{{ countDownText }}</span>
        内完成支付
      </div>

      <div class="ticket-info">
        <div class="section-title">第一程</div>

        <div class="train-row">
          <div class="station start">
            <div class="time">{{ formatTime(firstDepartureTime) }}</div>
            <div class="station-name">{{ firstStartStation }}</div>
          </div>

          <div class="middle">
            <div class="train-no">
              {{ firstTrainNo }}
            </div>

            <div class="arrow">━━━━━━━━━━━▶</div>

            <div class="duration">
              {{ firstDuration }}
            </div>
          </div>

          <div class="station end">
            <div class="time">{{ formatTime(firstArrivalTime) }}</div>
            <div class="station-name">{{ firstEndStation }}</div>
          </div>
        </div>

        <div class="date">
          <span class="label">乘车日期：</span>
          <span>{{ date }}</span>
        </div>

        <div class="order-info">
          <div class="row">
            <span>乘车人</span>
            <span>{{ name }}</span>
          </div>

          <div class="row">
            <span>座位</span>
            <span>{{ firstSeatType }} 0{{ firstCoachNo }}车 {{ firstSeatNo }}号</span>
          </div>

          <div class="row">
            <span>票价</span>
            <span class="price">￥{{ firstPrice }}</span>
          </div>
        </div>
      </div>

      <div class="ticket-info second-ticket">
        <div class="section-title">第二程</div>

        <div class="train-row">
          <div class="station start">
            <div class="time">{{ formatTime(secondDepartureTime) }}</div>
            <div class="station-name">{{ secondStartStation }}</div>
          </div>

          <div class="middle">
            <div class="train-no">
              {{ secondTrainNo }}
            </div>

            <div class="arrow">━━━━━━━━━━━▶</div>

            <div class="duration">
              {{ secondDuration }}
            </div>
          </div>

          <div class="station end">
            <div class="time">{{ formatTime(secondArrivalTime) }}</div>
            <div class="station-name">{{ secondEndStation }}</div>
          </div>
        </div>

        <div class="date">
          <span class="label">乘车日期：</span>
          <span>{{ date }}</span>
        </div>

        <div class="order-info">
          <div class="row">
            <span>乘车人</span>
            <span>{{ name }}</span>
          </div>

          <div class="row">
            <span>座位</span>
            <span>{{ secondSeatType }} 0{{ secondCoachNo }}车 {{ secondSeatNo }}号</span>
          </div>

          <div class="row">
            <span>票价</span>
            <span class="price">￥{{ secondPrice }}</span>
          </div>
        </div>
      </div>

      <div class="total-price">
        <span>总金额：</span>
        <span class="money">￥{{ totalPrice }}</span>
      </div>

      <div class="pay-method">
        <h3>请选择支付方式</h3>

        <el-radio-group v-model="payMethod">
          <el-radio value="支付宝"> 支付宝 </el-radio>
        </el-radio-group>
      </div>

      <div class="pay-btn">
        <el-button class="cancel-button" size="large" @click="cancelOrder()"> 取消订单 </el-button>

        <el-button class="pay-button" size="large" @click="pay"> 立即支付 </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute()
const router = useRouter()

const firstOrderId = Number(route.query.firstOrderId)
const secondOrderId = Number(route.query.secondOrderId)

const firstTrainNo = route.query.firstTrainNo as string
const firstStartStation = route.query.firstStartStation as string
const firstEndStation = route.query.firstEndStation as string
const firstDepartureTime = route.query.firstDepartureTime as string
const firstArrivalTime = route.query.firstArrivalTime as string
const firstSeatType = route.query.firstSeatType as string
const firstCoachNo = route.query.firstCoachNo
const firstSeatNo = route.query.firstSeatNo as string
const firstPrice = Number(route.query.firstPrice || 0)

const secondTrainNo = route.query.secondTrainNo as string
const secondStartStation = route.query.secondStartStation as string
const secondEndStation = route.query.secondEndStation as string
const secondDepartureTime = route.query.secondDepartureTime as string
const secondArrivalTime = route.query.secondArrivalTime as string
const secondSeatType = route.query.secondSeatType as string
const secondCoachNo = route.query.secondCoachNo
const secondSeatNo = route.query.secondSeatNo as string
const secondPrice = Number(route.query.secondPrice || 0)

const name = route.query.name as string
const date = route.query.date as string

const firstCreateTime = route.query.firstCreateTime as string
const secondCreateTime = route.query.secondCreateTime as string

const totalPrice = computed(() => {
  return Math.round(firstPrice + secondPrice)
})

const firstExpireTime = new Date(firstCreateTime).getTime() + 15 * 60 * 1000

const secondExpireTime = new Date(secondCreateTime).getTime() + 15 * 60 * 1000

const expireTime = Math.min(firstExpireTime, secondExpireTime)

const payMethod = ref('支付宝')

const countDown = ref(0)

let timer: number | undefined

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

const firstDuration = computed(() => {
  return calculateDuration(firstDepartureTime, firstArrivalTime)
})

const secondDuration = computed(() => {
  return calculateDuration(secondDepartureTime, secondArrivalTime)
})

const countDownText = computed(() => {
  const minute = Math.floor(countDown.value / 60)
  const second = countDown.value % 60

  return `${minute}分${second.toString().padStart(2, '0')}秒`
})

onMounted(() => {
  window.scrollTo(0, 0)
  countDown.value = Math.max(0, Math.floor((expireTime - Date.now()) / 1000))

  if (countDown.value <= 0) {
    cancelOrder(true)
    return
  }

  startCountDown()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const startCountDown = () => {
  timer = window.setInterval(() => {
    countDown.value = Math.max(0, Math.floor((expireTime - Date.now()) / 1000))

    if (countDown.value <= 0) {
      if (timer) {
        clearInterval(timer)
      }

      cancelOrder(true)
    }
  }, 1000)
}

const pay = async () => {
  try {
    if (payMethod.value !== '支付宝') {
      ElMessage.warning('目前中转票仅支持支付宝支付')
      return
    }

    if (timer) {
      clearInterval(timer)
    }

    const response = await request.post(
      `/payment/createTransfer/${firstOrderId}/${secondOrderId}`,
    )

    const form = document.createElement('div')
    form.innerHTML = response.data
    document.body.appendChild(form)

    const paymentForm = form.querySelector('form')

    if (!paymentForm) {
      ElMessage.error('支付宝支付页面生成失败')
      form.remove()
      startCountDown()
      return
    }

    paymentForm.submit()
  } catch (e) {
    ElMessage.error('支付失败')

    if (countDown.value > 0) {
      startCountDown()
    }
  }
}

const cancelOrder = async (timeout = false) => {
  try {
    await request.delete(`/order/cancel/${firstOrderId}`)

    await request.delete(`/order/cancel/${secondOrderId}`)

    if (timeout) {
      ElMessage.warning('支付超时，订单已取消')
    } else {
      ElMessage.success('订单已取消')
    }

    router.push('/home')
  } catch (e) {
    ElMessage.error('取消订单失败')
  }
}
</script>
<style scoped>
.payment {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #eaf4ff 0%, #ffffff 100%);
  padding: 40px 0;
  box-sizing: border-box;
}

.payment-card {
  width: 900px;
  margin: auto;
  background: #fff;
  border-radius: 8px;
  padding: 25px 30px 40px;
  box-sizing: border-box;
}

.header-box {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 35px;
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

.count-down {
  margin-bottom: 20px;
  text-align: center;
  font-size: 18px;
  color: #666;
}

.count-down span {
  color: #ff5500;
  font-size: 26px;
  font-weight: bold;
}

.ticket-info {
  border: 1px solid #dcecff;
  border-radius: 8px;
  padding: 25px 30px;
  background: #fbfdff;
}

.second-ticket {
  margin-top: 25px;
}

.section-title {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #1f4e8c;
}

.train-row {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.station {
  width: 180px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 37px;
}

.start,
.end {
  text-align: center;
}

.middle {
  width: 190px;
  flex-shrink: 0;
  margin: 0 55px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.time {
  font-size: 47px;
  font-weight: bold;
  line-height: 1;
  color: #303133;
}

.station-name {
  margin-top: 8px;
  font-size: 22px;
  font-weight: bold;
}

.train-no {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: -11px;
  color: black;
}

.arrow {
  width: 270px;
  text-align: center;
  color: black;
  font-weight: bold;
  font-size: 20px;
  line-height: 20px;
  margin: 2px 0;
}

.duration {
  margin-top: -5px;
  color: #7d7e80;
  font-size: 15px;
}

.date {
  margin-top: 10px;
  text-align: center;
  color: #585858;
  font-size: 17px;
}

.order-info {
  margin-top: 30px;
  border: 1px solid #dcecff;
  border-radius: 8px;
  overflow: hidden;
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 18px 25px;
  border-bottom: 1px solid #eee;
}

.row:last-child {
  border-bottom: none;
}

.price {
  color: #ff5500;
  font-size: 22px;
  font-weight: bold;
}

.total-price {
  margin-top: 25px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  font-size: 18px;
}

.money {
  margin-left: 10px;
  font-size: 25px;
  font-weight: bold;
  color: #ff5500;
}

.pay-method {
  margin-top: 30px;
  border: 1px solid #dcecff;
  border-radius: 8px;
  padding: 25px;
  background: #fbfdff;
}

.pay-method h3 {
  margin: 0 0 18px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f4e8c;
}

.pay-btn {
  margin-top: 40px;
  text-align: center;
}

.pay-btn .el-button {
  margin: 0 15px;
}

.pay-button {
  width: 220px;
  height: 46px;
  font-size: 18px;
  background: #ff742e;
  border-color: #ff742e;
  color: #fff;
}

.pay-button:hover,
.pay-button:focus {
  background: #ffad33;
  border-color: #ffad33;
  color: #fff;
}

.pay-button:active {
  background: #e68900;
  border-color: #e68900;
  color: #fff;
}

.cancel-button {
  width: 220px;
  height: 46px;
  font-size: 18px;
  background: #909399;
  border-color: #909399;
  color: #fff;
}

.cancel-button:hover,
.cancel-button:focus {
  background: #b1b3b8;
  border-color: #b1b3b8;
  color: #fff;
}

.cancel-button:active {
  background: #73767a;
  border-color: #73767a;
  color: #fff;
}
</style>
