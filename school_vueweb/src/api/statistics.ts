import request from '../utils/request'

export interface MonthlyFundData {
  year: number
  month: number
  label: string
  amount: number
}

export interface ItemDonationByType {
  itemType: string
  quantity: number
}

export interface DonationDistribution {
  distribution: { name: string; value: number }[]
  totalFundAmount: number
  totalItemCount: number
}

export interface ActivityParticipation {
  typeId: number
  typeName: string
  participantCount: number
}

export interface DashboardSummary {
  totalFundAmount: number
  totalItemCount: number
  totalActivityCount: number
  totalApplicationCount: number
  totalStudentCount: number
}

export const getDashboardSummary = (): Promise<DashboardSummary> => {
  return request.get('/api/statistics/dashboard-summary')
}

export const getMonthlyFundDonation = (): Promise<MonthlyFundData[]> => {
  return request.get('/api/statistics/monthly-fund-donation')
}

export const getItemDonationByType = (): Promise<ItemDonationByType[]> => {
  return request.get('/api/statistics/item-donation-by-type')
}

export const getDonationDistribution = (): Promise<DonationDistribution> => {
  return request.get('/api/statistics/donation-distribution')
}

export const getActivityParticipationByType = (): Promise<ActivityParticipation[]> => {
  return request.get('/api/statistics/activity-participation-by-type')
}
