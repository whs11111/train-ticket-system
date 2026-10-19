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
        <div class="train-row">
          <div class="station start">
            <div class="time">{{ formatTime(departureTime) }}</div>
            <div class="station-name">{{ startStation }}</div>
          </div>

          <div class="middle">
            <div class="train-no">
              {{ trainNo }}
            </div>

            <div class="arrow">━━━━━━━━━━━▶</div>

            <div class="duration">
              {{ duration }}
            </div>
          </div>

          <div class="station end">
            <div class="time">{{ formatTime(arrivalTime) }}</div>
            <div class="station-name">{{ endStation }}</div>
          </div>
        </div>

        <div class="date">
          <span class="label">乘车日期：</span>
          <span>{{ date }}</span>
        </div>
      </div>

      <div class="order-info">
        <div class="row">
          <span>乘车人</span>
          <span>{{ name }}</span>
        </div>

        <div class="row">
          <span>座位</span>
          <span>{{ seatType }} 0{{ coachNo }}车 {{ seatNo }}号</span>
        </div>

        <div class="row">
          <span>票价</span>
          <span class="price">￥{{ price }}</span>
        </div>
      </div>

      <div class="total-price">
        <span>总金额：</span>
        <span class="money">￥{{ price }}</span>
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

const orderId = Number(route.query.orderId)

const trainNo = route.query.trainNo as string

const startStation = route.query.startStation as string

const endStation = route.query.endStation as string

const departureTime = route.query.departureTime as string

const arrivalTime = route.query.arrivalTime as string

const seatType = route.query.seatType as string

const coachNo = route.query.coachNo

const seatNo = route.query.seatNo as string

const name = route.query.name as string

const price = route.query.price

const date = route.query.date as string

const createTime = route.query.createTime as string

const expireTime = new Date(createTime).getTime() + 15 * 60 * 1000

const payMethod = ref('支付宝')

const countDown = ref(0)

let timer: number | undefined

onMounted(() => {
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

const formatTime = (time: string) => {
  if (!time) return ''

  return time.substring(0, 5)
}

const countDownText = computed(() => {
  const minute = Math.floor(countDown.value / 60)

  const second = countDown.value % 60

  return `${minute}分${second.toString().padStart(2, '0')}秒`
})

const duration = computed(() => {
  const start = departureTime.split(':')

  const end = arrivalTime.split(':')

  const s = Number(start[0]) * 60 + Number(start[1])

  const e = Number(end[0]) * 60 + Number(end[1])

  const diff = e - s

  const h = Math.floor(diff / 60)

  const m = diff % 60

  return `${h}小时${m}分`
})

const startCountDown = () => {
  timer = window.setInterval(() => {
    countDown.value = Math.max(0, Math.floor((expireTime - Date.now()) / 1000))

    if (countDown.value <= 0) {
      clearInterval(timer)

      cancelOrder(true)
    }
  }, 1000)
}

const pay = async () => {
  try {
    if (payMethod.value !== '支付宝') {
      ElMessage.warning('目前仅支持支付宝支付')
      return
    }

    if (timer) {
      clearInterval(timer)
    }

    const response = await request.post(`/payment/create/${orderId}`)

    const paymentHtml = response.data

    const form = document.createElement('div')
    form.innerHTML = paymentHtml

    document.body.appendChild(form)

    const paymentForm = form.querySelector('form')

    if (!paymentForm) {
      ElMessage.error('支付宝支付页面生成失败')
      form.remove()
      return
    }

    paymentForm.submit()
  } catch (e) {
    ElMessage.error('支付失败')
  }
}

const cancelOrder = async (timeout = false) => {
  try {
    await request.delete(`/order/cancel/${orderId}`)

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

.train-row {
  display: flex;
  justify-content: center;
  align-items: center;
}

.station {
  width: 180px;
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
