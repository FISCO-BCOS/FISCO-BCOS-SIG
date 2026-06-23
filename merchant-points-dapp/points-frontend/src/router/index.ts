import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/RegisterPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardPage.vue') },
      { path: 'merchants', name: 'Merchants', component: () => import('@/views/merchants/MerchantListPage.vue') },
      { path: 'merchants/:id', name: 'MerchantDetail', component: () => import('@/views/merchants/MerchantDetailPage.vue') },
      { path: 'points/issue', name: 'IssuePoints', component: () => import('@/views/points/IssuePointsPage.vue') },
      { path: 'points/transfer', name: 'TransferPoints', component: () => import('@/views/points/TransferPointsPage.vue') },
      { path: 'points/consume', name: 'ConsumePoints', component: () => import('@/views/points/ConsumePointsPage.vue') },
      { path: 'transactions', name: 'Transactions', component: () => import('@/views/transactions/TransactionListPage.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/profile/ProfilePage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth !== false && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router