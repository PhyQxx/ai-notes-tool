import { openDB, type IDBPDatabase } from 'idb';

const DB_NAME = 'ai-notes-offline';
const STORE_NAME = 'drafts';
const DB_VERSION = 1;

export interface Draft {
  noteId: number | 'new';
  title: string;
  content: string;
  updatedAt: string;
  isSyncing?: boolean;
}

let dbPromise: Promise<IDBPDatabase> | null = null;

function getDB() {
  if (!dbPromise) {
    dbPromise = openDB(DB_NAME, DB_VERSION, {
      upgrade(db) {
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          db.createObjectStore(STORE_NAME, { keyPath: 'noteId' });
        }
      },
    });
  }
  return dbPromise;
}

export const offlineDB = {
  async saveDraft(draft: Draft) {
    const db = await getDB();
    return db.put(STORE_NAME, draft);
  },

  async getDraft(noteId: number | 'new'): Promise<Draft | undefined> {
    const db = await getDB();
    return db.get(STORE_NAME, noteId);
  },

  async getAllDrafts(): Promise<Draft[]> {
    const db = await getDB();
    return db.getAll(STORE_NAME);
  },

  async deleteDraft(noteId: number | 'new') {
    const db = await getDB();
    return db.delete(STORE_NAME, noteId);
  },

  async clearAll() {
    const db = await getDB();
    return db.clear(STORE_NAME);
  }
};
