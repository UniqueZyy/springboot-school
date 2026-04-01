import request from '../utils/request'

// 反馈接口
export interface Feedback {
  id: number
  type: number
  content: string
  submitterId: number
  submitterName: string
  submitTime: string
  status: number
  replyContent?: string
  replyId?: number
  replyName?: string
  replyTime?: string
}

// 反馈 API
export const getFeedbacksPage = (current: number = 1, size: number = 10, keyword?: string, type?: string, status?: string): Promise<any> => {
  let url = `/api/feedbacks/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  if (type && type.trim()) {
    url += `&type=${type}`
  }
  if (status && status.trim()) {
    url += `&status=${status}`
  }
  return request.get(url)
}

export const replyFeedback = (id: number, data: {
  replyContent: string
  replyId: number
  replyName: string
}): Promise<boolean> => {
  return request.put(`/api/feedbacks/${id}/reply`, data)
}

export const createFeedback = (data: Omit<Feedback, 'id' | 'submitterName' | 'submitTime' | 'status' | 'replyContent' | 'replyId' | 'replyName' | 'replyTime'>): Promise<boolean> => {
  return request.post('/api/feedbacks', data)
}

export const getFeedbacksBySubmitterId = (submitterId: number): Promise<Feedback[]> => {
  return request.get(`/api/feedbacks/submitter/${submitterId}`)
}
