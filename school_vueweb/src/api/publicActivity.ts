import request from '../utils/request'

export interface PublicActivityType {
  id: number
  typeName: string
  description: string
  status: number
  createTime: string
  updateTime: string
}

export interface PublicActivity {
  id: number
  activityName: string
  typeId: number
  typeName?: string
  description: string
  location: string
  startTime: string
  endTime: string
  maxParticipants: number
  currentParticipants: number
  status: number
  creatorId: number
  createTime: string
  updateTime: string
}

export interface PublicActivityApplication {
  id: number
  userId: number
  userName?: string
  activityId: number
  activityName?: string
  activityCreatorName?: string
  activityStatus?: number
  activityStartTime?: string
  activityEndTime?: string
  applicationReason: string
  status: number
  applyTime: string
  approveTime: string | null
  approverId: number | null
  approveRemark: string | null
}

export const getActivityTypes = (): Promise<PublicActivityType[]> => {
  return request.get('/api/activity-types')
}

export const getActivityTypeById = (id: number): Promise<PublicActivityType> => {
  return request.get(`/api/activity-types/${id}`)
}

export const createActivityType = (data: Partial<PublicActivityType>): Promise<boolean> => {
  return request.post('/api/activity-types', data)
}

export const updateActivityType = (id: number, data: Partial<PublicActivityType>): Promise<boolean> => {
  return request.put(`/api/activity-types/${id}`, data)
}

export const deleteActivityType = (id: number): Promise<boolean> => {
  return request.delete(`/api/activity-types/${id}`)
}

export const getActivityTypesPage = (current: number = 1, size: number = 10): Promise<any> => {
  return request.get(`/api/activity-types/page?current=${current}&size=${size}`)
}

export const searchActivityTypes = (keyword: string): Promise<PublicActivityType[]> => {
  return request.get(`/api/activity-types/search?keyword=${encodeURIComponent(keyword)}`)
}

export const getActivitiesPage = (current: number = 1, size: number = 10): Promise<any> => {
  return request.get(`/api/activities/page?current=${current}&size=${size}`)
}

export const searchActivities = (keyword: string): Promise<PublicActivity[]> => {
  return request.get(`/api/activities/search?keyword=${encodeURIComponent(keyword)}`)
}

export const getApplicationsPage = (current: number = 1, size: number = 10, keyword?: string, status?: string): Promise<any> => {
  let url = `/api/applications/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  if (status && status.trim()) {
    url += `&status=${status}`
  }
  return request.get(url)
}

export const searchApplications = (keyword: string): Promise<PublicActivityApplication[]> => {
  return request.get(`/api/applications/search?keyword=${encodeURIComponent(keyword)}`)
}

export const searchApplicationsWithParams = (params: string): Promise<PublicActivityApplication[]> => {
  return request.get(`/api/applications/search?${params}`)
}

export const getApplicationsByUserId = (userId: number, current: number = 1, size: number = 10, status?: string): Promise<any> => {
  let url = `/api/applications/user/${userId}?current=${current}&size=${size}`
  if (status && status.trim()) {
    url += `&status=${status}`
  }
  return request.get(url)
}

export const getActivities = (): Promise<PublicActivity[]> => {
  return request.get('/api/activities')
}

export const getActivityById = (id: number): Promise<PublicActivity> => {
  return request.get(`/api/activities/${id}`)
}

export const createActivity = (data: Partial<PublicActivity>): Promise<boolean> => {
  return request.post('/api/activities', data)
}

export const updateActivity = (id: number, data: Partial<PublicActivity>): Promise<boolean> => {
  return request.put(`/api/activities/${id}`, data)
}

export const deleteActivity = (id: number): Promise<boolean> => {
  return request.delete(`/api/activities/${id}`)
}

export const getApplications = (): Promise<PublicActivityApplication[]> => {
  return request.get('/api/applications')
}

export const getApplicationById = (id: number): Promise<PublicActivityApplication> => {
  return request.get(`/api/applications/${id}`)
}

export const approveApplication = (id: number, approverId: number, approveRemark?: string): Promise<boolean> => {
  const params = new URLSearchParams()
  params.append('approverId', approverId.toString())
  if (approveRemark) {
    params.append('approveRemark', approveRemark)
  }
  return request.put(`/api/applications/${id}/approve?${params.toString()}`)
}

export const rejectApplication = (id: number, approverId: number, approveRemark?: string): Promise<boolean> => {
  const params = new URLSearchParams()
  params.append('approverId', approverId.toString())
  if (approveRemark) {
    params.append('approveRemark', approveRemark)
  }
  return request.put(`/api/applications/${id}/reject?${params.toString()}`)
}

export const applyActivity = (data: {
  userId: number
  activityId: number
  applicationReason: string
  status?: number
  applyTime?: string
  approveTime?: string | null
  approverId?: number | null
  approveRemark?: string | null
}): Promise<boolean> => {
  return request.post('/api/applications', data)
}

export const updateApplication = (id: number, data: { applicationReason: string }): Promise<boolean> => {
  return request.put(`/api/applications/${id}`, data)
}

export const cancelApplication = (id: number): Promise<boolean> => {
  return request.put(`/api/applications/${id}/cancel`)
}
