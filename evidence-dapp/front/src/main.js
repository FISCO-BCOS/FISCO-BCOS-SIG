import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import VueRouter from 'vue-router'
import axios from 'axios'
import VueAxios from 'vue-axios'
import VueCookies from 'vue-cookies'
import routes from './router'
import App from './App.vue'
import './assets/styles/global.css'

axios.defaults.baseURL = ''

let accessToken = null
let isRefreshing = false
let refreshSubscribers = []

function onTokenRefreshed(newToken) {
  refreshSubscribers.forEach(cb => cb(newToken))
  refreshSubscribers = []
}

function addRefreshSubscriber(cb) {
  refreshSubscribers.push(cb)
}

function clearAuth() {
  accessToken = null
  isRefreshing = false
  refreshSubscribers = []
  VueCookies.remove('token')
  VueCookies.remove('userId')
  VueCookies.remove('username')
  VueCookies.remove('userType')
  VueCookies.remove('realName')
}

function goToLogin() {
  clearAuth()
  window.location.href = '/#/login'
}

async function tryRefreshToken() {
  if (isRefreshing) {
    return new Promise((resolve) => {
      addRefreshSubscriber((token) => resolve(token))
    })
  }
  isRefreshing = true
  try {
    const res = await axios.get('/api/auth/refresh', { skipAuthInterceptor: true })
    const data = res.data
    if (data.code === 200 && data.data) {
      accessToken = data.data.token
      if (data.data.user) {
        VueCookies.set('userId', data.data.user.id)
        VueCookies.set('username', data.data.user.username || '')
        VueCookies.set('userType', data.data.user.userType || '')
        VueCookies.set('realName', data.data.user.realName || '')
      }
      onTokenRefreshed(accessToken)
      return accessToken
    }
    throw new Error('refresh failed')
  } catch (e) {
    goToLogin()
    throw e
  } finally {
    isRefreshing = false
  }
}

axios.interceptors.request.use(config => {
  if (config.skipAuthInterceptor) return config
  if (accessToken) {
    config.headers.Authorization = 'Bearer ' + accessToken
  }
  return config
})

axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      try {
        const newToken = await tryRefreshToken()
        originalRequest.headers.Authorization = 'Bearer ' + newToken
        return axios(originalRequest)
      } catch (e) {
        return Promise.reject(e)
      }
    }
    return Promise.reject(error)
  }
)

Vue.use(ElementUI)
Vue.use(VueRouter)
Vue.use(VueAxios, axios)
Vue.use(VueCookies)

Vue.config.productionTip = false

const router = new VueRouter({ routes })

function setAccessToken(token) {
  accessToken = token
}

function getAccessToken() {
  return accessToken
}

Vue.prototype.$accessToken = { set: setAccessToken, get: getAccessToken }

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
