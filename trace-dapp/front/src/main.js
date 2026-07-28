import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import VueRouter from 'vue-router'
import axios from 'axios'
import VueAxios from 'vue-axios'
import VueCookies from 'vue-cookies'
import routes from './router'
import App from './App'

Vue.use(ElementUI)
Vue.use(VueRouter)
Vue.use(VueAxios, axios)
Vue.use(VueCookies)

const router = new VueRouter({
  mode: 'history',
  routes
})

const publicPaths = ['/login', '/register']

router.beforeEach((to, from, next) => {
  const token = VueCookies.get('token')
  if (!token && publicPaths.indexOf(to.path) === -1) {
    next('/login')
    return
  }
  if (token && publicPaths.indexOf(to.path) !== -1) {
    next('/home')
    return
  }
  next()
})

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')

Vue.config.productionTip = false
