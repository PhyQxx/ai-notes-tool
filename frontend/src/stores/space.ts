/**
 * 团队空间状态管理
 */
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  listSpaces,
  createSpace,
  deleteSpace,
  getMembers,
  inviteMember,
  removeMember,
  leaveSpace
} from '../api/space';
import type { Space, SpaceMember } from '../types';

export const useSpaceStore = defineStore('space', () => {
  // 状态
  const spaces = ref<Space[]>([]);
  const currentSpace = ref<Space | null>(null);
  const members = ref<SpaceMember[]>([]);
  const loading = ref(false);

  // 计算属性
  const spaceCount = computed(() => spaces.value.length);

  // Actions
  /**
   * 获取空间列表
   */
  async function fetchSpaces() {
    loading.value = true;
    try {
      const data = await listSpaces();
      spaces.value = data;
    } catch (error) {
      console.error('获取空间列表失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 创建空间
   */
  async function createNewSpace(data: { name: string; description: string }) {
    try {
      const newSpace = await createSpace(data);
      spaces.value.push(newSpace);
      return newSpace;
    } catch (error) {
      console.error('创建空间失败:', error);
      throw error;
    }
  }

  /**
   * 删除空间
   */
  async function deleteSpaceById(id: number) {
    try {
      await deleteSpace(id);
      spaces.value = spaces.value.filter(s => s.id !== id);
      if (currentSpace.value?.id === id) {
        currentSpace.value = null;
      }
    } catch (error) {
      console.error('删除空间失败:', error);
      throw error;
    }
  }

  /**
   * 获取成员列表
   */
  async function fetchMembers(spaceId: number) {
    loading.value = true;
    try {
      const data = await getMembers(spaceId);
      members.value = data;
    } catch (error) {
      console.error('获取成员列表失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 邀请成员
   */
  async function inviteNewMember(spaceId: number, data: { email: string; role: string }) {
    try {
      await inviteMember(spaceId, data);
      // 重新加载成员列表
      await fetchMembers(spaceId);
      // 更新成员数
      const space = spaces.value.find(s => s.id === spaceId);
      if (space) {
        space.memberCount += 1;
      }
      if (currentSpace.value?.id === spaceId) {
        currentSpace.value.memberCount += 1;
      }
    } catch (error) {
      console.error('邀请成员失败:', error);
      throw error;
    }
  }

  /**
   * 移除成员
   */
  async function removeMemberById(spaceId: number, userId: number) {
    try {
      await removeMember(spaceId, userId);
      members.value = members.value.filter(m => m.userId !== userId);
      // 更新成员数
      const space = spaces.value.find(s => s.id === spaceId);
      if (space) {
        space.memberCount -= 1;
      }
      if (currentSpace.value?.id === spaceId) {
        currentSpace.value.memberCount -= 1;
      }
    } catch (error) {
      console.error('移除成员失败:', error);
      throw error;
    }
  }

  /**
   * 退出空间
   */
  async function leaveSpaceById(spaceId: number) {
    try {
      await leaveSpace(spaceId);
      spaces.value = spaces.value.filter(s => s.id !== spaceId);
      if (currentSpace.value?.id === spaceId) {
        currentSpace.value = null;
      }
    } catch (error) {
      console.error('退出空间失败:', error);
      throw error;
    }
  }

  /**
   * 设置当前空间
   */
  function setCurrentSpace(space: Space | null) {
    currentSpace.value = space;
  }

  return {
    spaces,
    currentSpace,
    members,
    loading,
    spaceCount,
    fetchSpaces,
    createNewSpace,
    deleteSpaceById,
    fetchMembers,
    inviteNewMember,
    removeMemberById,
    leaveSpaceById,
    setCurrentSpace
  };
});
