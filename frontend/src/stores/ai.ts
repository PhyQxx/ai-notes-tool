/**
 * AI状态管理
 */
import { defineStore } from 'pinia';
import { ref } from 'vue';
import {
  getConversations,
  getConversation as getConversationApi,
  chat,
  chatStream,
  generate as generateApi,
  deleteConversation as deleteConversationApi,
  getConfig as getConfigApi,
  updateConfig as updateConfigApi,
  getProviders
} from '../api';
import type {
  AIConversation,
  AIConfig,
  AIProviderInfo,
  AIChatMessage
} from '../types';

export const useAIStore = defineStore('ai', () => {
  // 状态
  const conversations = ref<AIConversation[]>([]);
  const currentConversation = ref<AIConversation | null>(null);
  const config = ref<AIConfig>({
    provider: 'deepseek',
    model: 'deepseek-chat'
  });
  const providers = ref<AIProviderInfo[]>([]);
  const isStreaming = ref(false);
  const streamMessage = ref('');
  const loading = ref(false);

  // Actions
  /**
   * 获取对话列表
   */
  async function fetchConversations(): Promise<void> {
    loading.value = true;
    try {
      const list = await getConversations();
      conversations.value = list;
    } catch (error) {
      console.error('获取对话列表失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 获取对话详情
   */
  async function fetchConversation(id: number): Promise<void> {
    loading.value = true;
    try {
      const conv = await getConversationApi(id);
      currentConversation.value = conv;
    } catch (error) {
      console.error('获取对话详情失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 发送消息（支持流式）
   */
  async function sendMessage(content: string, noteId?: number): Promise<void> {
    const provider = config.value.provider;
    const model = config.value.model;

    // 添加用户消息到当前对话
    const userMessage: AIChatMessage = {
      role: 'user',
      content
    };

    if (!currentConversation.value) {
      // Create new conversation via API
      currentConversation.value = {
        id: 0, // Will be set by server
        noteId,
        aiProvider: provider,
        aiModel: model,
        title: content.substring(0, 50),
        messages: [userMessage],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
    } else {
      // 添加到现有对话
      currentConversation.value.messages.push(userMessage);
      currentConversation.value.updatedAt = new Date().toISOString();
    }

    // 添加AI消息占位符
    const assistantMessage: AIChatMessage = {
      role: 'assistant',
      content: ''
    };
    currentConversation.value.messages.push(assistantMessage);

    // 流式输出
    isStreaming.value = true;
    streamMessage.value = '';

    await new Promise<void>((resolve, reject) => {
      chatStream(
        {
          noteId,
          provider,
          model,
          message: content,
          conversationId: currentConversation.value?.id
        },
        // onMessage
        (chunk: string) => {
          streamMessage.value += chunk;
          assistantMessage.content = streamMessage.value;
        },
        // onDone
        () => {
          isStreaming.value = false;
          streamMessage.value = '';
          currentConversation.value!.updatedAt = new Date().toISOString();
          resolve();
        },
        // onError
        (err: Error) => {
          isStreaming.value = false;
          streamMessage.value = '';
          assistantMessage.content = '抱歉，生成内容时出错，请稍后重试。';
          reject(err);
        }
      );
    });
  }

  /**
   * AI内容生成
   */
  async function generateContent(
    type: 'summarize' | 'optimize' | 'expand' | 'rewrite' | 'continue',
    noteContent: string,
    noteId: number
  ): Promise<string> {
    const provider = config.value.provider;
    const model = config.value.model;

    let prompt = '';
    switch (type) {
      case 'summarize':
        prompt = '请为以下内容生成一个简洁的摘要：';
        break;
      case 'optimize':
        prompt = '请优化以下内容，使其更加流畅和专业：';
        break;
      case 'expand':
        prompt = '请扩写以下内容，使其更加详细和丰富：';
        break;
      case 'rewrite':
        prompt = '请改写以下内容，保持原意但使用不同的表达方式：';
        break;
      case 'continue':
        prompt = '请根据以下内容继续写下去：';
        break;
    }

    const result = await generateApi({
      noteId,
      provider,
      model,
      prompt: prompt + '\n\n' + noteContent,
      type
    });

    return result.content;
  }

  /**
   * 删除对话
   */
  async function deleteConversation(id: number): Promise<void> {
    loading.value = true;
    try {
      await deleteConversationApi(id);
      conversations.value = conversations.value.filter(c => c.id !== id);
      if (currentConversation.value?.id === id) {
        currentConversation.value = null;
      }
    } catch (error) {
      console.error('删除对话失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 获取配置
   */
  async function fetchConfig(): Promise<void> {
    try {
      const conf = await getConfigApi();
      config.value = conf;
    } catch (error) {
      console.error('获取AI配置失败:', error);
      throw error;
    }
  }

  /**
   * 更新配置
   */
  async function updateConfig(newConfig: AIConfig): Promise<void> {
    try {
      await updateConfigApi(newConfig);
      config.value = newConfig;
    } catch (error) {
      console.error('更新AI配置失败:', error);
      throw error;
    }
  }

  /**
   * 获取提供商列表
   */
  async function fetchProviders(): Promise<void> {
    try {
      const list = await getProviders();
      providers.value = list;
    } catch (error) {
      console.error('获取提供商列表失败:', error);
      throw error;
    }
  }

  /**
   * 新建对话
   */
  function newConversation(): void {
    currentConversation.value = null;
  }

  return {
    conversations,
    currentConversation,
    config,
    providers,
    isStreaming,
    streamMessage,
    loading,
    fetchConversations,
    fetchConversation,
    sendMessage,
    generateContent,
    deleteConversation,
    fetchConfig,
    updateConfig,
    fetchProviders,
    newConversation
  };
});
