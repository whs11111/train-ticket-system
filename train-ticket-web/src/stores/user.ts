import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null as any,
  }),

  getters: {
    isAdmin: (state) => state.user?.role === 'ADMIN',

    isUser: (state) => state.user?.role === 'USER',
  },

  actions: {
    setUser(user: any) {
      this.user = user
    },

    logout() {
      this.user = null
    },
  },

  persist: true,
})
