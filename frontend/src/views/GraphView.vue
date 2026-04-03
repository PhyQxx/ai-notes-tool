<template>
  <div class="graph-view">
    <div class="graph-header">
      <h2>知识图谱</h2>
      <div class="graph-legend">
        <span class="legend-item"><span class="legend-line link-line"></span> 双向链接</span>
        <span class="legend-item"><span class="legend-line tag-line"></span> 共同标签</span>
      </div>
      <el-button @click="loadData" :loading="loading">刷新</el-button>
    </div>
    <div class="graph-container" ref="canvasContainer">
      <canvas ref="canvasRef" @mousedown="onMouseDown" @mousemove="onMouseMove" @mouseup="onMouseUp" @click="onClick"></canvas>
      <div v-if="loading" class="graph-loading">
        <el-icon class="is-loading" :size="32" /><span>加载中...</span>
      </div>
      <div v-if="!loading && graphData && graphData.nodes.length === 0" class="graph-empty">
        暂无图谱数据，请先创建带有标签或链接的笔记
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { getGraphData, type GraphNode, type GraphEdge } from '@/api/note';
import { useThemeStore } from '@/stores/theme';

const router = useRouter();
const themeStore = useThemeStore();

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

interface SimNode {
  id: number;
  title: string;
  tags: string[];
  x: number;
  y: number;
  vx: number;
  vy: number;
}

const nodes = ref<SimNode[]>([]);
const edges = ref<GraphEdge[]>([]);
let animFrame = 0;
let dragging: SimNode | null = null;
let mouseX = 0, mouseY = 0;

async function loadData() {
  loading.value = true;
  try {
    const data = await getGraphData();
    graphData.value = data;
    initSimulation(data);
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
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
      vx: 0, vy: 0
    };
  });
  edges.value = data.edges;

  if (animFrame) cancelAnimationFrame(animFrame);
  simulate();
}

function simulate() {
  const n = nodes.value;
  if (n.length === 0) return;

  // Repulsion
  for (let i = 0; i < n.length; i++) {
    for (let j = i + 1; j < n.length; j++) {
      let dx = n[j].x - n[i].x;
      let dy = n[j].y - n[i].y;
      let dist = Math.sqrt(dx * dx + dy * dy);
      if (dist < 1) dist = 1;
      const force = 3000 / (dist * dist);
      const fx = (dx / dist) * force;
      const fy = (dy / dist) * force;
      n[i].vx -= fx; n[i].vy -= fy;
      n[j].vx += fx; n[j].vy += fy;
    }
  }

  // Attraction along edges
  for (const edge of edges.value) {
    const a = n.find(nd => nd.id === edge.source);
    const b = n.find(nd => nd.id === edge.target);
    if (!a || !b) continue;
    let dx = b.x - a.x;
    let dy = b.y - a.y;
    let dist = Math.sqrt(dx * dx + dy * dy);
    if (dist < 1) dist = 1;
    const force = (dist - 120) * 0.01;
    const fx = (dx / dist) * force;
    const fy = (dy / dist) * force;
    a.vx += fx; a.vy += fy;
    b.vx -= fx; b.vy -= fy;
  }

  // Center gravity
  const container = canvasContainer.value!;
  const cx = container.clientWidth / 2, cy = container.clientHeight / 2;
  for (const nd of n) {
    nd.vx += (cx - nd.x) * 0.001;
    nd.vy += (cy - nd.y) * 0.001;
    nd.vx *= 0.9;
    nd.vy *= 0.9;
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
  canvas.style.width = w + 'px';
  canvas.style.height = h + 'px';
  const ctx = canvas.getContext('2d')!;
  ctx.scale(devicePixelRatio, devicePixelRatio);
  const colors = getGraphColors();

  // Background
  ctx.fillStyle = colors.background;
  ctx.fillRect(0, 0, w, h);

  // Draw edges
  for (const edge of edges.value) {
    const a = nodes.value.find(nd => nd.id === edge.source);
    const b = nodes.value.find(nd => nd.id === edge.target);
    if (!a || !b) continue;
    ctx.beginPath();
    ctx.moveTo(a.x, a.y);
    ctx.lineTo(b.x, b.y);
    ctx.strokeStyle = edge.type === 'link' ? colors.linkEdge : colors.tagEdge;
    ctx.lineWidth = edge.type === 'link' ? 2 : 1.5;
    if (edge.type === 'tag') ctx.setLineDash([6, 4]);
    else ctx.setLineDash([]);
    ctx.stroke();
    ctx.setLineDash([]);
  }

  // Draw nodes
  for (const nd of nodes.value) {
    const r = 24;
    // Circle
    ctx.beginPath();
    ctx.arc(nd.x, nd.y, r, 0, Math.PI * 2);
    ctx.fillStyle = colors.nodeFill;
    ctx.fill();
    ctx.strokeStyle = colors.nodeStroke;
    ctx.lineWidth = 2;
    ctx.stroke();

    // Title
    ctx.fillStyle = colors.text;
    ctx.font = '13px sans-serif';
    ctx.textAlign = 'center';
    const title = nd.title.length > 8 ? nd.title.substring(0, 8) + '…' : nd.title;
    ctx.fillText(title, nd.x, nd.y + r + 16);
  }
}

function getMousePos(e: MouseEvent) {
  const canvas = canvasRef.value!;
  const rect = canvas.getBoundingClientRect();
  return { x: e.clientX - rect.left, y: e.clientY - rect.top };
}

function findNode(x: number, y: number): SimNode | null {
  for (const nd of nodes.value) {
    const dx = nd.x - x, dy = nd.y - y;
    if (dx * dx + dy * dy < 30 * 30) return nd;
  }
  return null;
}

function onMouseDown(e: MouseEvent) {
  const pos = getMousePos(e);
  dragging = findNode(pos.x, pos.y);
}

function onMouseMove(e: MouseEvent) {
  const pos = getMousePos(e);
  mouseX = pos.x; mouseY = pos.y;
  if (dragging) {
    dragging.x = pos.x;
    dragging.y = pos.y;
    dragging.vx = 0;
    dragging.vy = 0;
  }
}

function onMouseUp() {
  dragging = null;
}

function onClick(e: MouseEvent) {
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
  }
}
</style>
