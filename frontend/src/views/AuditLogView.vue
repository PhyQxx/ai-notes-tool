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

    <div class="stats-cards" v-if="logs.length > 0">
      <div ref="chartRef" class="activity-chart"></div>
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { queryAuditLogs, type AuditLog } from '@/api/auditLog'
import * as echarts from 'echarts'

const logs = ref<AuditLog[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

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
    
    await nextTick()
    renderChart()
  } catch { /* ignore */ } finally { loading.value = false }
}

function renderChart() {
  if (!chartRef.value || logs.value.length === 0) return
  if (!chart) chart = echarts.init(chartRef.value)
  
  // Group logs by hour or date for trend
  const stats = new Map<string, number>()
  logs.value.forEach(log => {
    const date = log.createdAt?.substring(0, 10) || 'Unknown'
    stats.set(date, (stats.get(date) || 0) + 1)
  })
  
  const sortedDates = Array.from(stats.keys()).sort()
  const data = sortedDates.map(date => stats.get(date))

  chart.setOption({
    title: { text: '操作活跃度趋势', textStyle: { fontSize: 14, fontWeight: 'normal' } },
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: sortedDates },
    yAxis: { type: 'value' },
    series: [
      {
        name: '操作数',
        type: 'line',
        smooth: true,
        data: data,
        areaStyle: { opacity: 0.1 },
        itemStyle: { color: '#8B5CF6' }
      }
    ]
  })
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

.stats-cards {
  margin-bottom: 24px;
  background: var(--el-bg-color);
  padding: 20px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
}

.activity-chart {
  height: 200px;
  width: 100%;
}

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
