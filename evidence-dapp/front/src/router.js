import Login from '@/views/Login'
import Register from '@/views/Register'
import VerifyPage from '@/views/VerifyPage'
import Home from '@/views/Home'
import EvidenceCreate from '@/views/EvidenceCreate'
import EvidenceList from '@/views/EvidenceList'
import Individual from '@/views/Individual'

export default [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    component: Login
  },
  {
    path: '/register',
    component: Register
  },
  {
    path: '/verify',
    component: VerifyPage
  },
  {
    path: '/home',
    component: Home
  },
  {
    path: '/evidence/create',
    component: EvidenceCreate
  },
  {
    path: '/evidence/list',
    component: EvidenceList
  },
  {
    path: '/individual',
    component: Individual
  }
]
