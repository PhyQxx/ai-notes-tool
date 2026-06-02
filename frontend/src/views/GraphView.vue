<template>
  <div class="graph-view">
    <div class="graph-header">
      <h2>知识图谱</h2>
      <div class="graph-legend">
        <span class="legend-item"><span class="legend-line link-line"></span> 双向链接</span>
        <span class="legend-item"><span class="legend-line tag-line"></span> 共同标签</span>
      </div>
      
      <div class="graph-controls">
        <el-button 
          size="small" 
          type="primary" 
          :loading="clusterLoading" 
          @click="handleRunClustering"
        >
          AI 语义聚类
        </el-button>
        <el-divider direction="vertical" />
        <el-switch v-model="localView" active-text="局部视图" inactive-text="全局视图" @change="handleViewChange" />
        <el-divider direction="vertical" />
        <el-button size="small" :type="is3D ? 'success' : ''" @click="toggle3D">
          {{ is3D ? '2D 模式' : '3D 模式' }}
        </el-button>
        <el-divider direction="vertical" />
        <el-popover placement="bottom" :width="200" trigger="click">
          <template #reference>
            <el-button size="small">标签筛选</el-button>
          </template>
          <div class="tag-filter-list">
            <el-checkbox-group v-model="selectedTags" @change="handleFilterChange">
              <div v-for="tag in allTags" :key="tag" class="tag-filter-item">
                <el-checkbox :label="tag">{{ tag }}</el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
        </el-popover>
      </div>

      <el-button @click="loadData" :loading="loading">刷新</el-button>
    </div>
    <div class="graph-container" ref="canvasContainer" @wheel="onWheel">
      <canvas
        v-show="!is3D"
        ref="canvasRef"
        @mousedown="onMouseDown"
        @mousemove="onMouseMove"
        @mouseup="onMouseUp"
        @click="onClick"
        @dblclick="onDoubleClick"
      ></canvas>
      <div v-if="is3D" ref="graph3DRef" class="graph-3d-container"></div>
      <div v-if="loading" class="graph-loading">
        <el-icon class="is-loading" :size="32" /><span>加载中...</span>
      </div>
      <div v-if="!loading && graphData && graphData.nodes.length === 0" class="graph-empty">
        暂无图谱数据，请先创建带有标签或链接的笔记
      </div>

      <!-- Cluster Legend Panel -->
      <transition name="el-fade-in">
        <div v-if="showClusters && clusters.length > 0" class="cluster-panel">
          <div class="panel-header">
            <h3>AI 语义聚类结果</h3>
            <el-button circle size="small" @click="showClusters = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="cluster-list">
            <div 
              v-for="(cluster, idx) in clusters" 
              :key="idx" 
              class="cluster-item"
            >
              <div class="cluster-info">
                <span class="cluster-color" :style="{ backgroundColor: clusterColors[idx % clusterColors.length] }"></span>
                <span class="cluster-name">{{ cluster.clusterName }}</span>
              </div>
              <div class="cluster-keywords">
                <el-tag 
                  v-for="kw in cluster.keywords" 
                  :key="kw" 
                  size="small" 
                  effect="plain"
                  class="kw-tag"
                >
                  {{ kw }}
                </el-tag>
              </div>
            </div>
          </div>
          <div class="panel-footer">
            <p>基于笔记内容的语义相似度自动生成的聚类</p>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { Close } from '@element-plus/icons-vue';
import ForceGraph3D from '3d-force-graph';
import { getGraphData, type GraphNode, type GraphEdge } from '@/api/note';
import { getNoteClusters } from '@/api/ai';
import { useThemeStore } from '@/stores/theme';

const router = useRouter();
const themeStore = useThemeStore();

// 3D Graph states
const is3D = ref(false);
const graph3DRef = ref<HTMLElement | null>(null);
let graph3D: any = null;

async function toggle3D() {
  is3D.value = !is3D.value;
  if (is3D.value) {
    if (animFrame) cancelAnimationFrame(animFrame);
    nextTick(() => init3DGraph());
  } else {
    if (graph3D) {
      graph3D.pauseAnimation();
      graph3D = null;
    }
    nextTick(() => {
      draw();
      simulate();
    });
  }
}

function init3DGraph() {
  if (!graphData.value || !graph3DRef.value) return;

  const colors = getGraphColors();
  const visibleIds = new Set(nodes.value.filter(n => n.visible).map(n => n.id));
  
  const gData = {
    nodes: nodes.value.filter(n => n.visible).map(n => ({
      id: n.id,
      name: n.title,
      clusterId: n.clusterId
    })),
    links: edges.value.filter(e => visibleIds.has(e.source) && visibleIds.has(e.target))
  };

  graph3D = ForceGraph3D()(graph3DRef.value)
    .graphData(gData)
    .nodeLabel('name')
    .nodeColor(node => {
      if (showClusters.value && (node as any).clusterId !== undefined) {
        return clusterColors[(node as any).clusterId % clusterColors.length];
      }
      return colors.nodeFill;
    })
    .linkColor(link => (link as any).type === 'link' ? colors.linkEdge : colors.tagEdge)
    .linkDirectionalArrowLength(3.5)
    .linkDirectionalArrowRelPos(1)
    .onNodeClick(node => {
      router.push(`/notes/${(node as any).id}`);
    })
    .backgroundColor(colors.background)
    .width(graph3DRef.value.clientWidth)
    .height(graph3DRef.value.clientHeight);
}

// Clustering states
const clusters = ref<any[]>([]);
const clusterLoading = ref(false);
const showClusters = ref(false);

const clusterColors = [
  '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8',
  '#F06292', '#AED581', '#FFD54F', '#4DB6AC', '#7986CB'
];

function getGraphColors() {
  const isDark = document.documentElement.classList.contains('dark');
  return isDark
    ? {
        linkEdge: '#7994FF',
        tagEdge: '#73d13d',
        nodeFill: '#7994FF',
        nodeStroke: '#2A2A2A',
        text: '#FFFFFFD9',
        background: '#141414',
      }
    : {
        linkEdge: '#5B7FFF',
        tagEdge: '#52c41a',
        nodeFill: '#5B7FFF',
        nodeStroke: '#ffffff',
        text: '#1D2129',
        background: '#ffffff',
      };
}
const canvasRef = ref<HTMLCanvasElement | null>(null);
const canvasContainer = ref<HTMLElement | null>(null);
const loading = ref(false);
const graphData = ref<{ nodes: GraphNode[]; edges: GraphEdge[] } | null>(null);

// View states
const localView = ref(false);
const selectedNodeId = ref<number | null>(null);
const selectedTags = ref<string[]>([]);
const allTags = ref<string[]>([]);

// Pan & Zoom states
const scale = ref(1);
const offsetX = ref(0);
const offsetY = ref(0);
let isPanning = false;
let lastMouseX = 0;
let lastMouseY = 0;

interface SimNode {
  id: number;
  title: string;
  tags: string[];
  x: number;
  y: number;
  vx: number;
  vy: number;
  visible?: boolean;
  clusterId?: number;
}

const nodes = ref<SimNode[]>([]);
const edges = ref<GraphEdge[]>([]);
let animFrame = 0;
let dragging: SimNode | null = null;

async function loadData() {
  loading.value = true;
  try {
    const data = await getGraphData();
    graphData.value = data;
    
    // Extract all unique tags
    const tagSet = new Set<string>();
    data.nodes.forEach(n => n.tags?.forEach(t => tagSet.add(t)));
    allTags.value = Array.from(tagSet).sort();
    selectedTags.value = [...allTags.value];

    initSimulation(data);
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function handleRunClustering() {
  if (clusterLoading.value) return;
  clusterLoading.value = true;
  try {
    const data = await getNoteClusters(6);
    clusters.value = data;
    showClusters.value = true;
    
    // Assign cluster IDs to nodes
    nodes.value.forEach(node => {
      const clusterIdx = data.findIndex((c: any) => c.noteIds.includes(node.id));
      node.clusterId = clusterIdx !== -1 ? clusterIdx : undefined;
    });
    
    draw();
  } catch (e) {
    console.error('聚类失败', e);
  } finally {
    clusterLoading.value = false;
  }
}

function initSimulation(data: { nodes: GraphNode[]; edges: GraphEdge[] }) {
  const container = canvasContainer.value!;
  const w = container.clientWidth;
  const h = container.clientHeight;
  const cx = w / 2, cy = h / 2;

  nodes.value = data.nodes.map((n, i) => {
    const angle = (2 * Math.PI * i) / data.nodes.length;
    const r = Math.min(w, h) * 0.3;
    return {
      id: n.id, title: n.title, tags: n.tags,
      x: cx + Math.cos(angle) * r,
      y: cy + Math.sin(angle) * r,
      vx: 0, vy: 0,
      visible: true
    };
  });
  edges.value = data.edges;

  applyFilters();

  if (animFrame) cancelAnimationFrame(animFrame);
  simulate();
}

function applyFilters() {
  if (!graphData.value) return;

  const visibleNodeIds = new Set<number>();

  if (localView.value && selectedNodeId.value !== null) {
    // Local view: centered on selectedNodeId
    visibleNodeIds.add(selectedNodeId.value);
    graphData.value.edges.forEach(e => {
      if (e.source === selectedNodeId.value) visibleNodeIds.add(e.target);
      if (e.target === selectedNodeId.value) visibleNodeIds.add(e.source);
    });
  } else {
    // Global view + tag filter
    nodes.value.forEach(n => {
      const hasSelectedTag = n.tags && n.tags.some(t => selectedTags.value.includes(t));
      if (hasSelectedTag || (selectedTags.value.length === allTags.value.length)) {
        visibleNodeIds.add(n.id);
      }
    });
  }

  nodes.value.forEach(n => {
    n.visible = visibleNodeIds.has(n.id);
  });
}

const handleViewChange = () => {
  if (localView.value && !selectedNodeId.value && nodes.value.length > 0) {
    selectedNodeId.value = nodes.value[0].id;
  }
  applyFilters();
};

const handleFilterChange = () => {
  applyFilters();
};

function simulate() {
  const n = nodes.value.filter(node => node.visible);
  if (n.length === 0) {
    draw();
    animFrame = requestAnimationFrame(simulate);
    return;
  }

  const visibleIds = new Set(n.map(node => node.id));
  const activeEdges = edges.value.filter(e => visibleIds.has(e.source) && visibleIds.has(e.target));

  // Repulsion
  for (let i = 0; i < n.length; i++) {
    for (let j = i + 1; j < n.length; j++) {
      let dx = n[j].x - n[i].x;
      let dy = n[j].y - n[i].y;
      let distSq = dx * dx + dy * dy;
      if (distSq < 1) distSq = 1;
      
      // Increased repulsion between different clusters if showing clusters
      let repulsionK = 5000;
      if (showClusters.value && n[i].clusterId !== n[j].clusterId) {
        repulsionK = 8000;
      }

      const force = repulsionK / distSq;
      const dist = Math.sqrt(distSq);
      const fx = (dx / dist) * force;
      const fy = (dy / dist) * force;
      n[i].vx -= fx; n[i].vy -= fy;
      n[j].vx += fx; n[j].vy += fy;
    }
  }

  // Attraction along edges
  for (const edge of activeEdges) {
    const a = n.find(nd => nd.id === edge.source);
    const b = n.find(nd => nd.id === edge.target);
    if (!a || !b) continue;
    let dx = b.x - a.x;
    let dy = b.y - a.y;
    let dist = Math.sqrt(dx * dx + dy * dy);
    if (dist < 1) dist = 1;
    const force = (dist - 150) * 0.02;
    const fx = (dx / dist) * force;
    const fy = (dy / dist) * force;
    a.vx += fx; a.vy += fy;
    b.vx -= fx; b.vy -= fy;
  }

  // Cluster attraction: pull nodes in the same cluster together
  if (showClusters.value) {
    clusters.value.forEach((cluster, idx) => {
      const clusterNodes = n.filter(nd => nd.clusterId === idx);
      if (clusterNodes.length <= 1) return;
      
      let avgX = 0, avgY = 0;
      clusterNodes.forEach(nd => { avgX += nd.x; avgY += nd.y; });
      avgX /= clusterNodes.length;
      avgY /= clusterNodes.length;
      
      clusterNodes.forEach(nd => {
        nd.vx += (avgX - nd.x) * 0.01;
        nd.vy += (avgY - nd.y) * 0.01;
      });
    });
  }

  // Center gravity
  const container = canvasContainer.value!;
  const cx = container.clientWidth / 2, cy = container.clientHeight / 2;
  for (const nd of n) {
    nd.vx += (cx - nd.x) * 0.005;
    nd.vy += (cy - nd.y) * 0.005;
    nd.vx *= 0.85; // Damping
    nd.vy *= 0.85;
    if (nd !== dragging) {
      nd.x += nd.vx;
      nd.y += nd.vy;
    }
  }

  draw();
  animFrame = requestAnimationFrame(simulate);
}

function draw() {
  const canvas = canvasRef.value;
  const container = canvasContainer.value;
  if (!canvas || !container) return;

  const w = container.clientWidth;
  const h = container.clientHeight;
  canvas.width = w * devicePixelRatio;
  canvas.height = h * devicePixelRatio;
  const ctx = canvas.getContext('2d')!;
  ctx.scale(devicePixelRatio, devicePixelRatio);
  const colors = getGraphColors();

  // Apply Pan & Zoom
  ctx.save();
  ctx.translate(offsetX.value, offsetY.value);
  ctx.scale(scale.value, scale.value);

  // Background
  ctx.fillStyle = colors.background;
  ctx.fillRect(-offsetX.value / scale.value, -offsetY.value / scale.value, w / scale.value, h / scale.value);

  const visibleNodes = nodes.value.filter(n => n.visible);
  const visibleIds = new Set(visibleNodes.map(n => n.id));

  // Draw edges
  for (const edge of edges.value) {
    if (!visibleIds.has(edge.source) || !visibleIds.has(edge.target)) continue;
    const a = visibleNodes.find(nd => nd.id === edge.source);
    const b = visibleNodes.find(nd => nd.id === edge.target);
    if (!a || !b) continue;
    
    ctx.beginPath();
    ctx.moveTo(a.x, a.y);
    ctx.lineTo(b.x, b.y);
    ctx.strokeStyle = edge.type === 'link' ? colors.linkEdge : colors.tagEdge;
    ctx.lineWidth = (edge.type === 'link' ? 2 : 1.5) / scale.value;
    ctx.globalAlpha = showClusters.value ? 0.2 : 0.6;
    if (edge.type === 'tag') ctx.setLineDash([6 / scale.value, 4 / scale.value]);
    else ctx.setLineDash([]);
    ctx.stroke();
    ctx.globalAlpha = 1.0;
  }

  // Draw nodes
  for (const nd of visibleNodes) {
    const r = 24;
    const isSelected = nd.id === selectedNodeId.value;
    
    // Circle
    ctx.beginPath();
    ctx.arc(nd.x, nd.y, r, 0, Math.PI * 2);
    
    if (showClusters.value && nd.clusterId !== undefined) {
      ctx.fillStyle = clusterColors[nd.clusterId % clusterColors.length];
    } else {
      ctx.fillStyle = isSelected ? colors.linkEdge : colors.nodeFill;
    }
    
    ctx.fill();
    ctx.strokeStyle = isSelected ? (themeStore.isDark ? '#fff' : '#000') : colors.nodeStroke;
    ctx.lineWidth = (isSelected ? 4 : 2) / scale.value;
    ctx.stroke();

    // Title
    ctx.fillStyle = colors.text;
    ctx.font = `${13 / scale.value}px sans-serif`;
    ctx.textAlign = 'center';
    const title = nd.title.length > 10 ? nd.title.substring(0, 10) + '…' : nd.title;
    ctx.fillText(title, nd.x, nd.y + r + 16 / scale.value);
  }

  ctx.restore();
}

function getMousePos(e: MouseEvent | WheelEvent) {
  const rect = canvasRef.value!.getBoundingClientRect();
  return {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  };
}

function screenToWorld(x: number, y: number) {
  return {
    x: (x - offsetX.value) / scale.value,
    y: (y - offsetY.value) / scale.value
  };
}

function onWheel(e: WheelEvent) {
  e.preventDefault();
  const zoomSpeed = 0.001;
  const delta = -e.deltaY;
  const newScale = Math.min(Math.max(scale.value + delta * zoomSpeed, 0.1), 5);
  
  // Zoom towards mouse position
  const pos = getMousePos(e);
  const worldPos = screenToWorld(pos.x, pos.y);
  
  scale.value = newScale;
  offsetX.value = pos.x - worldPos.x * scale.value;
  offsetY.value = pos.y - worldPos.y * scale.value;
}

function findNode(x: number, y: number): SimNode | null {
  const worldPos = screenToWorld(x, y);
  for (const nd of nodes.value) {
    if (!nd.visible) continue;
    const dx = nd.x - worldPos.x, dy = nd.y - worldPos.y;
    if (dx * dx + dy * dy < 30 * 30) return nd;
  }
  return null;
}

function onMouseDown(e: MouseEvent) {
  const pos = getMousePos(e);
  const nd = findNode(pos.x, pos.y);
  if (nd) {
    dragging = nd;
  } else {
    isPanning = true;
    lastMouseX = pos.x;
    lastMouseY = pos.y;
  }
}

function onMouseMove(e: MouseEvent) {
  const pos = getMousePos(e);
  if (dragging) {
    const worldPos = screenToWorld(pos.x, pos.y);
    dragging.x = worldPos.x;
    dragging.y = worldPos.y;
    dragging.vx = 0;
    dragging.vy = 0;
  } else if (isPanning) {
    offsetX.value += pos.x - lastMouseX;
    offsetY.value += pos.y - lastMouseY;
    lastMouseX = pos.x;
    lastMouseY = pos.y;
  }
}

function onMouseUp() {
  dragging = null;
  isPanning = false;
}

function onClick(e: MouseEvent) {
  const pos = getMousePos(e);
  const nd = findNode(pos.x, pos.y);
  if (nd) {
    selectedNodeId.value = nd.id;
    if (localView.value) applyFilters();
  }
}

function onDoubleClick(e: MouseEvent) {
  const pos = getMousePos(e);
  const nd = findNode(pos.x, pos.y);
  if (nd) {
    router.push(`/notes/${nd.id}`);
  }
}

onMounted(() => {
  loadData();
  window.addEventListener('resize', () => draw());

  // Watch theme changes to redraw canvas
  const observer = new MutationObserver(() => {
    if (nodes.value.length > 0) draw();
  });
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class'],
  });
  themeObserver = observer;
});

let themeObserver: MutationObserver | null = null;

onBeforeUnmount(() => {
  if (animFrame) cancelAnimationFrame(animFrame);
  if (themeObserver) {
    themeObserver.disconnect();
    themeObserver = null;
  }
});
</script>

<style scoped lang="scss">
.graph-view {
  height: 100%;
  display: flex;
  flex-direction: column;

  .graph-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
    h2 { margin: 0; font-size: 20px; font-weight: 600; }

    .graph-controls {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-left: auto;
    }

    .graph-legend {
      display: flex;
      gap: 16px;
      .legend-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: var(--el-text-color-secondary);
        .legend-line {
          display: inline-block;
          width: 24px;
          height: 3px;
          border-radius: 2px;
          &.link-line { background-color: var(--brand-primary); }
          &.tag-line { background-color: var(--color-success); border-top: 2px dashed var(--color-success); height: 0; }
        }
      }
    }
  }

  .graph-container {
    flex: 1;
    position: relative;
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
    overflow: hidden;
    background-color: var(--el-bg-color);

    canvas {
      display: block;
      cursor: grab;
      &:active { cursor: grabbing; }
    }

    .graph-3d-container {
      width: 100%;
      height: 100%;
    }

    .graph-loading, .graph-empty {
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: var(--el-text-color-secondary);
      font-size: 14px;
    }

    .cluster-panel {
      position: absolute;
      top: 16px;
      right: 16px;
      width: 280px;
      max-height: calc(100% - 32px);
      background-color: var(--el-bg-color-overlay);
      border: 1px solid var(--el-border-color-light);
      border-radius: 12px;
      box-shadow: var(--el-box-shadow-light);
      display: flex;
      flex-direction: column;
      z-index: 100;
      overflow: hidden;

      .panel-header {
        padding: 12px 16px;
        border-bottom: 1px solid var(--el-border-color-lighter);
        display: flex;
        justify-content: space-between;
        align-items: center;
        h3 { margin: 0; font-size: 15px; font-weight: 600; }
      }

      .cluster-list {
        flex: 1;
        overflow-y: auto;
        padding: 8px 0;

        .cluster-item {
          padding: 10px 16px;
          border-bottom: 1px solid var(--el-border-color-extra-light);
          &:last-child { border-bottom: none; }

          .cluster-info {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 6px;

            .cluster-color {
              width: 12px;
              height: 12px;
              border-radius: 50%;
            }
            .cluster-name {
              font-size: 14px;
              font-weight: 500;
              color: var(--el-text-color-primary);
            }
          }

          .cluster-keywords {
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
            .kw-tag { border-radius: 4px; }
          }
        }
      }

      .panel-footer {
        padding: 10px 16px;
        background-color: var(--el-fill-color-extra-light);
        border-top: 1px solid var(--el-border-color-lighter);
        p { margin: 0; font-size: 11px; color: var(--el-text-color-placeholder); line-height: 1.4; }
      }
    }
  }
}
</style>
