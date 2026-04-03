/**
 * WebSocket 工具类 - 统一处理实时协作和通知推送
 * 支持心跳保活、断线重连（指数退避）、消息队列缓冲
 */

export interface WSMessage {
  type: string;
  userId?: number;
  noteId?: number;
  data?: any;
  users?: number[];
}

type MessageHandler = (msg: WSMessage) => void;

class NoteWebSocket {
  private ws: WebSocket | null = null;
  private token: string = '';
  private handlers: Map<string, Set<MessageHandler>> = new Map();
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private heartbeatTimer: ReturnType<typeof setInterval> | null = null;
  private reconnectCount = 0;
  private maxReconnect = 5;
  private baseReconnectInterval = 2000; // 基础重连间隔 2s
  private noteId: number | null = null;
  private _isConnected = false;

  // 消息队列：断线时缓存消息，重连后批量发送
  private messageQueue: Array<{ type: string; data?: any }> = [];
  private maxQueueSize = 100;

  get isConnected(): boolean {
    return this._isConnected;
  }

  /**
   * 连接 WebSocket
   */
  connect(token: string): void {
    if (this.ws?.readyState === WebSocket.OPEN) return;

    this.token = token;
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const wsUrl = `${protocol}//${host}/ws?token=${encodeURIComponent(token)}`;

    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      this._isConnected = true;
      this.reconnectCount = 0;
      console.log('[WS] 连接成功');

      // 启动心跳（30秒间隔）
      this.startHeartbeat();

      // 重新加入笔记房间
      if (this.noteId) {
        this.send('join_note', { noteId: this.noteId });
      }

      // 发送消息队列中缓存的消息
      this.flushMessageQueue();

      this.emit('connected', {} as WSMessage);
    };

    this.ws.onmessage = (event) => {
      try {
        const msg: WSMessage = JSON.parse(event.data);

        // 心跳响应
        if (msg.type === 'pong') return;

        this.emit(msg.type, msg);
      } catch (e) {
        console.error('[WS] 解析消息失败', e);
      }
    };

    this.ws.onclose = () => {
      this._isConnected = false;
      this.stopHeartbeat();
      console.log('[WS] 连接关闭');
      this.emit('disconnected', {} as WSMessage);
      this.autoReconnect();
    };

    this.ws.onerror = (error) => {
      console.error('[WS] 连接错误', error);
    };
  }

  /**
   * 加入笔记编辑房间
   */
  joinNote(noteId: number): void {
    this.noteId = noteId;
    this.send('join_note', { noteId });
  }

  /**
   * 离开笔记编辑房间
   */
  leaveNote(noteId: number): void {
    this.send('leave_note', { noteId });
    if (this.noteId === noteId) {
      this.noteId = null;
    }
  }

  /**
   * 发送消息（断线时加入队列）
   */
  send(type: string, data?: any): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify({ type, ...data }));
    } else {
      // 断线时缓存消息到队列（排除心跳类型）
      if (type !== 'ping' && this.messageQueue.length < this.maxQueueSize) {
        this.messageQueue.push({ type, data });
        console.log(`[WS] 消息已缓存到队列 (${this.messageQueue.length}/${this.maxQueueSize})`);
      }
    }
  }

  /**
   * 注册消息处理器
   */
  on(type: string, handler: MessageHandler): void {
    if (!this.handlers.has(type)) {
      this.handlers.set(type, new Set());
    }
    this.handlers.get(type)!.add(handler);
  }

  /**
   * 移除消息处理器
   */
  off(type: string, handler?: MessageHandler): void {
    if (!handler) {
      this.handlers.delete(type);
    } else {
      this.handlers.get(type)?.delete(handler);
    }
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.stopHeartbeat();
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    this.reconnectCount = this.maxReconnect; // 阻止自动重连
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this._isConnected = false;
    this.handlers.clear();
    this.messageQueue = [];
  }

  private emit(type: string, msg: WSMessage): void {
    this.handlers.get(type)?.forEach(handler => {
      try {
        handler(msg);
      } catch (e) {
        console.error(`[WS] handler error for ${type}`, e);
      }
    });
  }

  /**
   * 指数退避自动重连
   */
  private autoReconnect(): void {
    if (this.reconnectCount >= this.maxReconnect) {
      console.log('[WS] 达到最大重连次数，停止重连');
      this.emit('reconnect_failed', {} as WSMessage);
      return;
    }
    // 指数退避：2s, 4s, 8s, 16s, 32s
    const delay = Math.min(
      this.baseReconnectInterval * Math.pow(2, this.reconnectCount),
      30000 // 最大30s
    );
    this.reconnectCount++;
    console.log(`[WS] ${delay / 1000}s 后重连 (${this.reconnectCount}/${this.maxReconnect})`);
    this.reconnectTimer = setTimeout(() => {
      this.connect(this.token);
    }, delay);
  }

  /**
   * 启动心跳（30秒间隔）
   */
  private startHeartbeat(): void {
    this.stopHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'ping' }));
      }
    }, 30000);
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 重连后批量发送队列中的消息
   */
  private flushMessageQueue(): void {
    if (this.messageQueue.length === 0) return;

    const messages = [...this.messageQueue];
    this.messageQueue = [];
    console.log(`[WS] 批量发送 ${messages.length} 条缓存消息`);

    for (const msg of messages) {
      try {
        if (this.ws?.readyState === WebSocket.OPEN) {
          this.ws.send(JSON.stringify(msg));
        }
      } catch (e) {
        console.error('[WS] 发送缓存消息失败', e);
      }
    }
  }
}

export const wsClient = new NoteWebSocket();
export default wsClient;
