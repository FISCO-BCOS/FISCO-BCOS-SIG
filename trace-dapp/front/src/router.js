import Home from '@/views/Home'
import Login from '@/views/Login'
import Register from '@/views/Register'
import ProductsCreate from '@/views/ProductsCreate'
import ProductsList from '@/views/ProductsList'
import ProductsDetail from '@/views/ProductsDetail'
import Planting from '@/views/Planting'
import Processing from '@/views/Processing'
import Testing from '@/views/Testing'
import Logistics from '@/views/Logistics'
import TraceQuery from '@/views/TraceQuery'
import Individual from '@/views/Individual'
import ChainApproval from '@/views/ChainApproval'

export default [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    component: Home
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
    path: '/products/create',
    component: ProductsCreate
  },
  {
    path: '/products/list',
    component: ProductsList
  },
  {
    path: '/products/detail/:id',
    component: ProductsDetail
  },
  {
    path: '/trace/planting',
    component: Planting
  },
  {
    path: '/trace/processing',
    component: Processing
  },
  {
    path: '/trace/testing',
    component: Testing
  },
  {
    path: '/trace/logistics',
    component: Logistics
  },
  {
    path: '/trace/query',
    component: TraceQuery
  },
  {
    path: '/individual',
    component: Individual
  },
  {
    path: '/chain/approval',
    component: ChainApproval
  }
]