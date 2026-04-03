import { http } from '../utils/request'

export interface AuditLog {
  id: number
  userId: number
  username: string
  action: string
  targetType: string
  targetId: number
  detail: string
  ip: string
  createdAt: string
}

export interface AuditLogPage {
  records: AuditLog[]
  total: number
  pages: number
}

export function queryAuditLogs(params: {
  page?: number
  size?: number
  userId?: number
  action?: string
  startTime?: string
  endTime?: string
}) {
  return http.get<AuditLogPage>('/audit-logs', { params })
}
