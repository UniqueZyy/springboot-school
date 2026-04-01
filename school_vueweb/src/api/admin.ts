import request from '../utils/request'

// 管理员接口
export interface Admin {
  id: number
  name: string
  username: string
  password: string
  role: number
  status: number
  createTime: string
}

// 管理员 API
export const getAdminsPage = (current: number = 1, size: number = 10, keyword?: string, role?: string): Promise<any> => {
  let url = `/api/admins/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  if (role && role.trim()) {
    url += `&role=${role}`
  }
  return request.get(url)
}

export const createAdmin = (data: Omit<Admin, 'id' | 'createTime'>): Promise<boolean> => {
  return request.post('/api/admins', data)
}

export const updateAdmin = (id: number, data: Omit<Admin, 'id' | 'createTime'>): Promise<boolean> => {
  return request.put(`/api/admins/${id}`, data)
}

export const deleteAdmin = (id: number): Promise<boolean> => {
  return request.delete(`/api/admins/${id}`)
}

export const resetAdminPassword = (id: number, newPassword: string): Promise<boolean> => {
  return request.put(`/api/admins/${id}/reset-password`, { newPassword })
}

export const updateUser = (id: number, data: {
  name: string
  email: string
  phone: string
  gender: number
}): Promise<boolean> => {
  return request.put(`/api/users/${id}`, data)
}
