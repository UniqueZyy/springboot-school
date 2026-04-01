import request from '../utils/request'

// 捐赠物资类型接口
export interface DonationType {
  id: number
  typeName: string
  description: string
  status: number
  createTime: string
}

// 捐赠申请接口
export interface DonationApplication {
  id: number
  applicantId: number
  applicantName: string
  applicantRole?: number
  applicationType?: number
  typeId: number
  donationTypeName: string
  quantity: number
  reason: string
  status: number
  applyTime: string
  approverId?: number
  approverName?: string
  approveTime?: string
  approvalComment?: string
}

// 捐赠分配接口
export interface DonationDistribution {
  id: number
  typeId: number
  donationTypeName?: string
  quantity: number
  distributionObject: string
  reason: string
  distributionTime: string
  operatorId: number
  operatorName?: string
}

// 捐赠记录接口
export interface DonationRecord {
  id: number
  userId: number
  projectId: number
  amount: number
  donationTime: string
  remark: string
}

// 捐赠项目接口
export interface DonationProject {
  id: number
  projectName: string
  description: string
  targetAmount: number
  currentAmount: number
  startDate: string
  endDate: string
  status: number
}

// 捐赠物资类型 API
export const getDonationTypes = (): Promise<DonationType[]> => {
  return request.get('/api/donation-types')
}

export const getDonationTypesPage = (current: number = 1, size: number = 10): Promise<any> => {
  return request.get(`/api/donation-types/page?current=${current}&size=${size}`)
}

export const searchDonationTypes = (keyword: string): Promise<DonationType[]> => {
  return request.get(`/api/donation-types/search?keyword=${encodeURIComponent(keyword)}`)
}

export const createDonationType = (data: Omit<DonationType, 'id' | 'createTime'>): Promise<boolean> => {
  return request.post('/api/donation-types', data)
}

export const updateDonationType = (id: number, data: Omit<DonationType, 'id' | 'createTime'>): Promise<boolean> => {
  return request.put(`/api/donation-types/${id}`, data)
}

export const deleteDonationType = (id: number): Promise<boolean> => {
  return request.delete(`/api/donation-types/${id}`)
}

// 捐赠申请 API
export const getDonationApplicationsPage = (current: number = 1, size: number = 10, keyword?: string, status?: string): Promise<any> => {
  let url = `/api/donation-applications/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  if (status && status.trim()) {
    url += `&status=${status}`
  }
  return request.get(url)
}

export const approveDonationApplication = (id: number, comment: string): Promise<boolean> => {
  return request.put(`/api/donation-applications/${id}/approve`, { comment })
}

export const rejectDonationApplication = (id: number, comment: string): Promise<boolean> => {
  return request.put(`/api/donation-applications/${id}/reject`, { comment })
}

export const createDonationApplication = (data: Omit<DonationApplication, 'id' | 'applicantName' | 'donationTypeName' | 'status' | 'applyTime' | 'approverId' | 'approverName' | 'approveTime' | 'approvalComment'>): Promise<boolean> => {
  return request.post('/api/donation-applications', data)
}

// 捐赠分配 API
export const getDonationDistributionsPage = (current: number = 1, size: number = 10, keyword?: string, typeId?: string): Promise<any> => {
  let url = `/api/donation-distributions/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  if (typeId && typeId.trim()) {
    url += `&typeId=${typeId}`
  }
  return request.get(url)
}

export const createDonationDistribution = (data: Omit<DonationDistribution, 'id' | 'distributionTime'>): Promise<boolean> => {
  return request.post('/api/donation-distributions', data)
}

export const updateDonationDistribution = (id: number, data: Omit<DonationDistribution, 'id' | 'distributionTime'>): Promise<boolean> => {
  return request.put(`/api/donation-distributions/${id}`, data)
}

export const deleteDonationDistribution = (id: number): Promise<boolean> => {
  return request.delete(`/api/donation-distributions/${id}`)
}

// 捐赠记录 API
export const getDonationRecords = (): Promise<DonationRecord[]> => {
  return request.get('/api/donation-records')
}

// 捐赠项目 API
export const getDonationProjects = (): Promise<DonationProject[]> => {
  return request.get('/api/donation-projects')
}

// 爱心事迹相关API
export const getLoveStories = (keyword?: string): Promise<any[]> => {
  let url = '/api/love-stories'
  if (keyword && keyword.trim()) {
    url += `?keyword=${encodeURIComponent(keyword)}`
  }
  return request.get(url)
}

export const getLoveStoriesPage = (current: number = 1, size: number = 10, keyword?: string): Promise<any> => {
  let url = `/api/love-stories/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  return request.get(url)
}

// 爱心榜单相关API
export const getDonationRanking = (): Promise<any[]> => {
  return request.get('/api/love-rankings/donation')
}

export const getItemRanking = (): Promise<any[]> => {
  return request.get('/api/love-rankings/item')
}

export const getActivityRanking = (): Promise<any[]> => {
  return request.get('/api/love-rankings/activity')
}

// 物品捐赠相关API
export const getItemDonationsPage = (current: number = 1, size: number = 10, keyword?: string, itemType?: string, status?: number): Promise<any> => {
  let url = `/api/item-donations/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  if (itemType && itemType.trim()) {
    url += `&itemType=${encodeURIComponent(itemType)}`
  }
  if (status !== undefined) {
    url += `&status=${status}`
  }
  return request.get(url)
}

export const approveItemDonation = (id: number): Promise<boolean> => {
  return request.put(`/api/item-donations/${id}/approve`)
}

export const rejectItemDonation = (id: number): Promise<boolean> => {
  return request.put(`/api/item-donations/${id}/reject`)
}

// 资金捐赠相关API
export const getFundDonationsPage = (current: number = 1, size: number = 10, keyword?: string, status?: number): Promise<any> => {
  let url = `/api/fund-donations/page?current=${current}&size=${size}`
  if (keyword && keyword.trim()) {
    url += `&keyword=${encodeURIComponent(keyword)}`
  }
  if (status !== undefined) {
    url += `&status=${status}`
  }
  return request.get(url)
}

export const payFundDonation = (id: number): Promise<boolean> => {
  return request.put(`/api/fund-donations/${id}/pay`)
}
