/**
 * AI相关API
 */
import { http } from '../utils/request';
import type {
  AIChatRequest,
  AIGenerateRequest,
  AIConversation,
  AIConfig,
  AIProviderInfo,
  ApiResponse
} from '../types';

/**
 * AI对话
 */
export function chat(data: AIChatRequest): Promise<{ conversationId: number; message: string }> {
  return http.post('/ai/chat', data);
}

/**
 * 流式对话 - 使用fetch + ReadableStream
 */
export function chatStream(
  data: AIChatRequest,
  onMessage: (msg: string) => void,
  onDone: (conversationId?: number) => void,
  onError: (err: Error) => void,
  signal?: AbortSignal
): AbortController | null {
  const token = localStorage.getItem('token');
  const controller = new AbortController();
  const effectiveSignal = signal || controller.signal;

  fetch('/api/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(data),
    signal: effectiveSignal
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('无法读取响应流');
      }

      const decoder = new TextDecoder();
      let buffer = '';

      try {
        while (true) {
          const { done, value } = await reader.read();
          if (done) {
            break;
          }

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop() || '';

          for (const line of lines) {
            if (line.startsWith('data: ')) {
              const data = line.slice(6);
              if (data === '[DONE]') {
                onDone();
                return;
              }
              try {
                const parsed = JSON.parse(data);
                if (parsed.content) {
                  onMessage(parsed.content);
                }
                if (parsed.done && parsed.conversationId) {
                  onDone(parsed.conversationId);
                  return;
                }
              } catch (e) {
                console.warn('解析流数据失败:', e);
              }
            }
          }
        }
        onDone();
      } catch (error) {
        onError(error as Error);
      }
    })
    .catch((error) => {
      if ((error as Error).name !== 'AbortError') {
        onError(error);
      }
    });

  return controller;
}

/**
 * AI内容生成
 */
export function generate(data: AIGenerateRequest): Promise<{ content: string }> {
  return http.post('/ai/generate', data);
}

/**
 * 获取对话列表
 */
export function getConversations(): Promise<AIConversation[]> {
  return http.get('/ai/conversations');
}

/**
 * 获取对话详情
 */
export function getConversation(id: number): Promise<AIConversation> {
  return http.get(`/ai/conversations/${id}`);
}

/**
 * 删除对话
 */
export function deleteConversation(id: number): Promise<void> {
  return http.delete(`/ai/conversations/${id}`);
}

/**
 * 重命名对话
 */
export function renameConversation(id: number, title: string): Promise<void> {
  return http.put(`/ai/conversations/${id}/rename`, { title });
}

/**
 * 新建对话
 */
export function createConversation(data?: { noteId?: number; provider?: string; model?: string }): Promise<any> {
  return http.post('/ai/conversations', data || {});
}

/**
 * 获取AI配置
 */
export function getConfig(): Promise<AIConfig> {
  return http.get('/ai/config');
}

/**
 * 更新AI配置
 */
export function updateConfig(data: AIConfig): Promise<void> {
  return http.put('/ai/config', data);
}

/**
 * 获取可用提供商
 */
export function getProviders(): Promise<AIProviderInfo[]> {
  return http.get('/ai/providers');
}

/**
 * 获取对话消息列表（含上下文统计）
 */
export function getConversationMessages(id: number): Promise<{
  conversationId: number;
  messages: { id: number; role: string; content: string; tokenCount: number; createdAt: string }[];
  messageCount: number;
  totalTokens: number;
  maxRounds: number;
  usedRounds: number;
}> {
  return http.get(`/ai/conversations/${id}/messages`);
}

/**
 * 清除对话上下文
 */
export function clearConversationMessages(id: number): Promise<void> {
  return http.delete(`/ai/conversations/${id}/messages`);
}

// ==================== AI 辅助写作接口 ====================

/**
 * AI总结笔记
 */
export function aiSummarize(noteId?: number | null, content?: string): Promise<{ data: string }> {
  return http.post('/ai/assistant/summarize', { noteId, content });
}

/**
 * AI生成大纲
 */
export function aiOutline(noteId?: number | null, content?: string): Promise<{ data: string }> {
  return http.post('/ai/assistant/outline', { noteId, content });
}

/**
 * AI续写
 */
export function aiContinue(content: string): Promise<{ data: string }> {
  return http.post('/ai/assistant/continue', { content });
}

/**
 * AI翻译（中英互译）
 */
export function aiTranslate(content: string, targetLang: string): Promise<{ data: string }> {
  return http.post('/ai/assistant/translate', { content, targetLang });
}

/**
 * AI润色
 */
export function aiPolish(content: string): Promise<{ data: string }> {
  return http.post('/ai/assistant/polish', { content });
}
