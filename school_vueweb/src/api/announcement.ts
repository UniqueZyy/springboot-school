import request from '../utils/request'

// 公告接口
export interface Announcement {
  id: number
  title: string
  content: string
  publishTime: string
  expireTime: string
  isTop: number
  status: number
  publisherId: number
  publisherName: string
}

// 公告 API
export const getAnnouncementsPage = (current: number = 1, size: number = 10, keyword?: string, status?: string): Promise<any> => {
  let url = `/api/announcements/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  if (status && status.trim()) {
    url += `&status=${status}`
  }
  return request.get(url)
}

export const createAnnouncement = (data: Omit<Announcement, 'id' | 'publishTime' | 'status'>): Promise<boolean> => {
  return request.post('/api/announcements', data)
}

export const updateAnnouncement = (id: number, data: Omit<Announcement, 'id' | 'publishTime' | 'status'>): Promise<boolean> => {
  return request.put(`/api/announcements/${id}`, data)
}

export const deleteAnnouncement = (id: number): Promise<boolean> => {
  return request.delete(`/api/announcements/${id}`)
}

export const updateAnnouncementTop = (id: number, isTop: number): Promise<boolean> => {
  return request.put(`/api/announcements/${id}/top?isTop=${isTop}`)
}
