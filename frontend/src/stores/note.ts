/**
 * 笔记状态管理
 */
import { defineStore } from 'pinia';
import { ref } from 'vue';
import {
  listNotes,
  getNote as getNoteApi,
  createNote as createNoteApi,
  updateNote as updateNoteApi,
  deleteNote as deleteNoteApi,
  searchNotes,
  getFolderTree,
  toggleFavorite as toggleFavoriteApi,
  toggleTop as toggleTopApi,
  createFolder as createFolderApi,
  updateFolder as updateFolderApi,
  deleteFolder as deleteFolderApi,
  listVersions,
  createVersion as createVersionApi,
  restoreVersion as restoreVersionApi,
  deleteVersion as deleteVersionApi
} from '../api';
import type { Note, Folder, NoteVersion } from '../types';

// Normalize backend note data to frontend format
function normalizeNote(raw: any): Note {
  return {
    ...raw,
    isFavorite: !!raw.isFavorite,
    isTop: !!raw.isTop,
    tags: raw.tags || [],
    folderId: raw.folderId || 0,
  };
}

export const useNoteStore = defineStore('note', () => {
  // 状态
  const notes = ref<Note[]>([]);
  const currentNote = ref<Note | null>(null);
  const folders = ref<Folder[]>([]);
  const versions = ref<NoteVersion[]>([]);
  const loading = ref(false);
  const searchKeyword = ref('');
  const total = ref(0);
  const currentPage = ref(1);
  const pageSize = ref(20);

  // Actions
  /**
   * 获取笔记列表
   */
  async function fetchNotes(params?: {
    page?: number;
    size?: number;
    folderId?: number;
    sortBy?: string;
    isFavorite?: number;
    append?: boolean;
  }): Promise<void> {
    loading.value = true;
    try {
      const result = await listNotes({
        page: params?.page || currentPage.value,
        size: params?.size || pageSize.value,
        folderId: params?.folderId,
        sortBy: params?.sortBy,
        isFavorite: params?.isFavorite
      });
      const newNotes = (result.records || []).map(normalizeNote);
      if (params?.append) {
        notes.value = [...notes.value, ...newNotes];
      } else {
        notes.value = newNotes;
      }
      total.value = result.total || 0;
      currentPage.value = result.current || result.page || 1;
    } catch (error) {
      console.error('获取笔记列表失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 获取笔记详情
   */
  async function fetchNote(id: number): Promise<void> {
    loading.value = true;
    try {
      const note = await getNoteApi(id);
      currentNote.value = normalizeNote(note);
    } catch (error) {
      console.error('获取笔记详情失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 创建笔记
   */
  async function createNote(data: {
    title: string;
    content: string;
    contentType: string;
    folderId?: number;
  }): Promise<Note> {
    loading.value = true;
    try {
      const id = await createNoteApi(data);
      const note: Note = {
        id: id as any,
        title: data.title || '无标题',
        content: data.content,
        contentType: data.contentType,
        folderId: data.folderId || 0,
        tags: [],
        isFavorite: false,
        isTop: false,
        viewCount: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      notes.value.unshift(note);
      return note;
    } catch (error) {
      console.error('创建笔记失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 更新笔记
   */
  async function updateNote(id: number, data: Partial<Note>): Promise<void> {
    loading.value = true;
    try {
      const updatedNote = await updateNoteApi(id, data);
      // 更新列表中的笔记
      const index = notes.value.findIndex(n => n.id === id);
      if (index !== -1) {
        notes.value[index] = updatedNote;
      }
      // 更新当前笔记
      if (currentNote.value?.id === id) {
        currentNote.value = updatedNote;
      }
    } catch (error) {
      console.error('更新笔记失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 删除笔记
   */
  async function deleteNote(id: number): Promise<void> {
    loading.value = true;
    try {
      await deleteNoteApi(id);
      notes.value = notes.value.filter(n => n.id !== id);
      if (currentNote.value?.id === id) {
        currentNote.value = null;
      }
    } catch (error) {
      console.error('删除笔记失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 搜索笔记
   */
  async function search(keyword: string): Promise<void> {
    loading.value = true;
    searchKeyword.value = keyword;
    try {
      const result = await searchNotes(keyword, {
        page: 1,
        size: pageSize.value
      });
      notes.value = (result.records || []).map(normalizeNote);
      total.value = result.total || 0;
      currentPage.value = result.current || result.page || 1;
    } catch (error) {
      console.error('搜索笔记失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 获取文件夹树
   */
  async function fetchFolders(): Promise<void> {
    try {
      const tree = await getFolderTree();
      folders.value = tree;
    } catch (error) {
      console.error('获取文件夹树失败:', error);
      throw error;
    }
  }

  /**
   * 收藏/取消收藏笔记
   */
  async function toggleFavorite(id: number): Promise<void> {
    try {
      await toggleFavoriteApi(id);
      const note = notes.value.find(n => n.id === id);
      if (note) {
        note.isFavorite = !note.isFavorite;
      }
    } catch (error) {
      console.error('切换收藏状态失败:', error);
      throw error;
    }
  }

  /**
   * 置顶/取消置顶笔记
   */
  async function toggleTop(id: number): Promise<void> {
    try {
      await toggleTopApi(id);
      const note = notes.value.find(n => n.id === id);
      if (note) {
        note.isTop = !note.isTop;
      }
    } catch (error) {
      console.error('切换置顶状态失败:', error);
      throw error;
    }
  }

  /**
   * 创建文件夹
   */
  async function createFolder(data: {
    name: string;
    parentId?: number;
  }): Promise<void> {
    try {
      await createFolderApi(data);
      await fetchFolders();
    } catch (error) {
      console.error('创建文件夹失败:', error);
      throw error;
    }
  }

  /**
   * 更新文件夹
   */
  async function updateFolder(id: number, data: {
    name?: string;
    parentId?: number;
  }): Promise<void> {
    try {
      await updateFolderApi(id, data);
      await fetchFolders();
    } catch (error) {
      console.error('更新文件夹失败:', error);
      throw error;
    }
  }

  /**
   * 删除文件夹
   */
  async function deleteFolder(id: number): Promise<void> {
    try {
      await deleteFolderApi(id);
      await fetchFolders();
    } catch (error) {
      console.error('删除文件夹失败:', error);
      throw error;
    }
  }

  /**
   * 获取版本列表
   */
  async function fetchVersions(noteId: number): Promise<void> {
    try {
      const response = await listVersions(noteId);
      versions.value = response.data || [];
    } catch (error) {
      console.error('获取版本列表失败:', error);
      throw error;
    }
  }

  /**
   * 创建版本快照
   */
  async function createVersion(noteId: number, remark?: string): Promise<void> {
    try {
      await createVersionApi(noteId, remark);
      await fetchVersions(noteId);
    } catch (error) {
      console.error('创建版本失败:', error);
      throw error;
    }
  }

  /**
   * 恢复版本
   */
  async function restoreVersion(noteId: number, versionId: number): Promise<void> {
    try {
      await restoreVersionApi(noteId, versionId);
      await fetchNote(noteId);
      await fetchVersions(noteId);
    } catch (error) {
      console.error('恢复版本失败:', error);
      throw error;
    }
  }

  /**
   * 删除版本
   */
  async function deleteVersion(noteId: number, versionId: number): Promise<void> {
    try {
      await deleteVersionApi(noteId, versionId);
      await fetchVersions(noteId);
    } catch (error) {
      console.error('删除版本失败:', error);
      throw error;
    }
  }

  return {
    notes,
    currentNote,
    folders,
    versions,
    loading,
    searchKeyword,
    total,
    currentPage,
    pageSize,
    fetchNotes,
    fetchNote,
    createNote,
    updateNote,
    deleteNote,
    search,
    fetchFolders,
    toggleFavorite,
    toggleTop,
    createFolder,
    updateFolder,
    deleteFolder,
    fetchVersions,
    createVersion,
    restoreVersion,
    deleteVersion
  };
});
