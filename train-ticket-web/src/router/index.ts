import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Order from '../views/Order.vue'
import Payment from '../views/Payment.vue'
import OrderDetail from '../views/OrderDetail.vue'
import MyOrder from '../views/MyOrder.vue'
import Profile from '../views/Profile.vue'
import ChangeTicket from '../views/ChangeTicket.vue'
import ChangePayment from '../views/ChangePayment.vue'
import Refund from '../views/Refund.vue'
import AdminHome from '../views/AdminHome.vue'
import UserManage from '../views/UserManage.vue'
import TrainManage from '@/views/TrainManage.vue'
import StationManage from '@/views/StationManage.vue'
import OrderManage from '@/views/OrderManage.vue'
import StaffHome from '@/views/StaffHome.vue'
import TransferOrder from '@/views/TransferOrder.vue'
import TransferPayment from '@/views/TransferPayment.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }

    return {
      top: 0,
      left: 0,
    }
  },

  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'Login',
      component: Login,
    },
    {
      path: '/register',
      name: 'Register',
      component: Register,
    },
    {
      path: '/order',
      name: 'Order',
      component: Order,
    },
    {
      path: '/orderDetail',
      name: 'OrderDetail',
      component: OrderDetail,
    },
    {
      path: '/home',
      name: 'Home',
      component: Home,
    },
    {
      path: '/myOrder',
      name: 'MyOrder',
      component: MyOrder,
    },
    {
      path: '/payment',
      name: 'Payment',
      component: Payment,
    },
    {
      path: '/profile',
      name: 'Profile',
      component: Profile,
    },
    {
      path: '/changeTicket',
      name: 'ChangeTicket',
      component: ChangeTicket,
    },
    {
      path: '/changepayment',
      name: 'ChangePayment',
      component: ChangePayment,
    },
    {
      path: '/refund',
      name: 'Refund',
      component: Refund,
    },
    {
      path: '/adminHome',
      name: 'AdminHome',
      component: AdminHome,
    },
    {
      path: '/staffHome',
      name: 'StaffHome',
      component: StaffHome,
    },
    {
      path: '/userManage',
      name: 'UserManage',
      component: UserManage,
    },
    {
      path: '/trainManage',
      name: 'TrainManage',
      component: TrainManage,
    },
    {
      path: '/stationManage',
      name: 'StationManage',
      component: StationManage,
    },

    {
      path: '/transferOrder',
      name: 'TransferOrder',
      component: TransferOrder,
    },
    {
      path: '/transferPayment',
      name: 'TransferPayment',
      component: TransferPayment,
    },
    {
      path: '/orderManage',
      name: 'OrderManage',
      component: OrderManage,
    },
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  if (to.path === '/login' || to.path === '/register') {
    return
  }

  if (!userStore.user) {
    return '/login'
  }

  if (to.path === '/adminHome' && userStore.user.role !== 'ADMIN') {
    if (userStore.user.role === 'STAFF') {
      return '/staffHome'
    }

    return '/home'
  }

  return
})

export default router
