import request from '../utils/request'

// 物品捐赠记录接口
export interface ItemDonation {
  id: number
  donorId: number
  donorName: string
  itemType: string
  itemName: string
  quantity: number
  donationDate: string
  remark: string
  status: number
}

// 资金捐赠记录接口
export interface FundDonation {
  id: number
  donorId: number
  donorName: string
  amount: number
  donationDate: string
  remark: string
  status: number
}

// 物品捐赠记录 API
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

export const getItemDonationsByDonorId = (donorId: number): Promise<ItemDonation[]> => {
  return request.get(`/api/item-donations/donor/${donorId}`)
}

export const createItemDonation = (data: Omit<ItemDonation, 'id' | 'donorName' | 'donationDate' | 'status'>): Promise<boolean> => {
  return request.post('/api/item-donations', data)
}

export const updateItemDonation = (id: number, data: Partial<ItemDonation>): Promise<boolean> => {
  return request.put(`/api/item-donations/${id}`, data)
}

export const deleteItemDonation = (id: number): Promise<boolean> => {
  return request.delete(`/api/item-donations/${id}`)
}

export const approveItemDonation = (id: number): Promise<boolean> => {
  return request.put(`/api/item-donations/${id}/approve`)
}

export const rejectItemDonation = (id: number): Promise<boolean> => {
  return request.put(`/api/item-donations/${id}/reject`)
}

export const shipItemDonation = (id: number): Promise<boolean> => {
  return request.put(`/api/item-donations/${id}/shipped`)
}

// 资金捐赠记录 API
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

export const getFundDonationsByDonorId = (donorId: number): Promise<FundDonation[]> => {
  return request.get(`/api/fund-donations/donor/${donorId}`)
}

export const createFundDonation = (data: Omit<FundDonation, 'id' | 'donorName' | 'donationDate' | 'status'>): Promise<boolean> => {
  return request.post('/api/fund-donations', data)
}

export const updateFundDonation = (id: number, data: Partial<FundDonation>): Promise<boolean> => {
  return request.put(`/api/fund-donations/${id}`, data)
}

export const deleteFundDonation = (id: number): Promise<boolean> => {
  return request.delete(`/api/fund-donations/${id}`)
}

export const payFundDonation = (id: number): Promise<boolean> => {
  return request.put(`/api/fund-donations/${id}/pay`)
}

export const failFundDonation = (id: number): Promise<boolean> => {
  return request.put(`/api/fund-donations/${id}/fail`)
}
