import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: 'http://49.51.229.18:9090/api/admin',  // 修改为管理后台的地址
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 可以在这里添加 token
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data
  },
  (error) => {
    let message = '请求失败'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请登录'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data.message || '请求失败'
      }
    } else if (error.request) {
      message = '网络错误，请检查网络连接'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
