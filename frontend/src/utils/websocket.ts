/**
 * WebSocket 工具类 - 统一处理实时协作和通知推送
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
  private reconnectCount = 0;
  private maxReconnect = 5;
  private reconnectInterval = 3000;
  private noteId: number | null = null;
  private _isConnected = false;

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

      // 重新加入笔记房间
      if (this.noteId) {
        this.send('join_note', { noteId: this.noteId });
      }

      this.emit('connected', {} as WSMessage);
    };

    this.ws.onmessage = (event) => {
      try {
        const msg: WSMessage = JSON.parse(event.data);
        this.emit(msg.type, msg);
      } catch (e) {
        console.error('[WS] 解析消息失败', e);
      }
    };

    this.ws.onclose = () => {
      this._isConnected = false;
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
   * 发送消息
   */
  send(type: string, data?: any): void {
    if (this.ws?.readyState !== WebSocket.OPEN) return;
    this.ws.send(JSON.stringify({ type, ...data }));
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

  private autoReconnect(): void {
    if (this.reconnectCount >= this.maxReconnect) {
      console.log('[WS] 达到最大重连次数，停止重连');
      return;
    }
    this.reconnectCount++;
    console.log(`[WS] ${this.reconnectInterval / 1000}s 后重连 (${this.reconnectCount}/${this.maxReconnect})`);
    this.reconnectTimer = setTimeout(() => {
      this.connect(this.token);
    }, this.reconnectInterval);
  }
}

export const wsClient = new NoteWebSocket();
export default wsClient;
