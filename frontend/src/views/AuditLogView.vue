<template>
  <div class="audit-log-view">
    <div class="page-header">
      <h2>📋 操作日志</h2>
    </div>

    <div class="filter-bar">
      <el-input v-model="filters.userId" placeholder="用户ID" clearable style="width: 120px" />
      <el-select v-model="filters.action" placeholder="操作类型" clearable style="width: 150px">
        <el-option label="创建笔记" value="CREATE_NOTE" />
        <el-option label="更新笔记" value="UPDATE_NOTE" />
        <el-option label="删除笔记" value="DELETE_NOTE" />
        <el-option label="用户登录" value="LOGIN" />
      </el-select>
      <el-date-picker v-model="filters.dateRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss" style="width: 360px" />
      <el-button type="primary" @click="search(1)">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table :data="logs" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="action" label="操作" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ actionLabel(row.action) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetType" label="目标类型" width="90" />
      <el-table-column prop="targetId" label="目标ID" width="80" />
      <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="createdAt" label="时间" width="170" />
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :page-size="20"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="search"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { queryAuditLogs, type AuditLog } from '@/api/auditLog'

const logs = ref<AuditLog[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const filters = reactive({ userId: '', action: '', dateRange: null as string[] | null })

function actionLabel(action: string) {
  const map: Record<string, string> = { CREATE_NOTE: '创建笔记', UPDATE_NOTE: '更新笔记', DELETE_NOTE: '删除笔记', LOGIN: '登录' }
  return map[action] || action
}

async function search(p?: number) {
  if (p) page.value = p
  loading.value = true
  try {
    const d = await queryAuditLogs({
      page: page.value,
      size: 20,
      userId: filters.userId ? Number(filters.userId) : undefined,
      action: filters.action || undefined,
      startTime: filters.dateRange?.[0],
      endTime: filters.dateRange?.[1]
    })
    logs.value = d?.records || []
    total.value = d?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function resetFilters() {
  filters.userId = ''
  filters.action = ''
  filters.dateRange = null
  search(1)
}

onMounted(() => search(1))
</script>

<style scoped>
.audit-log-view { padding: 24px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
