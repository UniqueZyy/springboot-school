import request from '../utils/request'
import type { ResponseType } from '../utils/request'

export interface LoginRequest {
  username: string
  password: string
}

export interface UserVO {
  id: number
  username: string
  name: string
  email: string
  phone: string
  roleType: number
  code: string
  gender: number
}

export type LoginResponse = ResponseType<UserVO>

export const login = (data: LoginRequest): Promise<LoginResponse> => {
  return request.post('/api/auth/login', data)
}

export const getUsersPage = (current: number = 1, size: number = 10, keyword?: string): Promise<any> => {
  let url = `/api/users/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  return request.get(url)
}

export const createUser = (data: any): Promise<boolean> => {
  return request.post('/api/users', data)
}

export const updateUser = (id: number, data: any): Promise<boolean> => {
  return request.put(`/api/users/${id}`, data)
}

export const deleteUser = (id: number): Promise<boolean> => {
  return request.delete(`/api/users/${id}`)
}
