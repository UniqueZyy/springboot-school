import request from '../utils/request'

// 学生接口
export interface Student {
  id: number
  studentId: string
  name: string
  gender: number
  grade: number
  classNo: number
  major: string
  contact: string
  createTime: string
}

// 学生 API
export const getStudentsPage = (current: number = 1, size: number = 10, keyword?: string, grade?: string, classNo?: string): Promise<any> => {
  let url = `/api/students/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  if (grade && grade.trim()) {
    url += `&grade=${grade}`
  }
  if (classNo && classNo.trim()) {
    url += `&classNo=${classNo}`
  }
  return request.get(url)
}

export const createStudent = (data: Omit<Student, 'id' | 'createTime'>): Promise<boolean> => {
  return request.post('/api/students', data)
}

export const updateStudent = (id: number, data: Omit<Student, 'id' | 'createTime'>): Promise<boolean> => {
  return request.put(`/api/students/${id}`, data)
}

export const deleteStudent = (id: number): Promise<boolean> => {
  return request.delete(`/api/students/${id}`)
}
