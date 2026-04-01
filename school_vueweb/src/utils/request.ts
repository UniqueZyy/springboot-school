import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

const instance: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 10000
})

instance.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  response => {
    // 对于分页请求，返回完整的 response.data
    // 对于其他请求，返回 response.data
    return response.data
  },
  error => {
    console.error('请求错误:', error.message)
    if (error.response) {
      console.error('响应状态:', error.response.status)
      console.error('响应数据:', error.response.data)
    } else if (error.request) {
      console.error('请求已发送但没有收到响应')
    } else {
      console.error('请求配置错误:', error.config)
    }
    return Promise.reject(error)
  }
)

const request = instance as any

export default request

export type ResponseType<T = any> = {
  code: number
  message: string
  data: T
}
