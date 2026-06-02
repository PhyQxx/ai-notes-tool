import * as Y from 'yjs';
import * as encoding from 'lib0/encoding';
import * as decoding from 'lib0/decoding';
import { wsClient, type WSMessage } from './websocket';
import { Observable } from 'lib0/observable';
import * as awarenessProtocol from 'y-protocols/awareness';

export class YjsProvider extends Observable<any> {
  public doc: Y.Doc;
  public noteId: number;
  public awareness: awarenessProtocol.Awareness;

  constructor(noteId: number, doc: Y.Doc) {
    super();
    this.noteId = noteId;
    this.doc = doc;
    this.awareness = new awarenessProtocol.Awareness(doc);

    // Listen for updates from the server
    wsClient.on('yjs_update', this.onUpdate);
    wsClient.on('yjs_sync', this.onSync);
    wsClient.on('yjs_awareness', this.onAwarenessUpdate);

    // Listen for local changes to broadcast
    this.doc.on('update', this.onDocUpdate);
    this.awareness.on('update', this.onLocalAwarenessUpdate);

    console.log(`[Yjs] Provider initialized for note ${noteId}`);
  }

  private onUpdate = (msg: WSMessage) => {
    if (msg.noteId !== this.noteId || !msg.data) return;
    
    try {
      const update = Uint8Array.from(atob(msg.data), c => c.charCodeAt(0));
      Y.applyUpdate(this.doc, update, this);
    } catch (e) {
      console.error('[Yjs] Failed to apply update', e);
    }
  };

  private onSync = (msg: WSMessage) => {
    if (msg.noteId !== this.noteId || !msg.data || !Array.isArray(msg.data)) return;

    console.log(`[Yjs] Received ${msg.data.length} sync updates`);
    try {
      msg.data.forEach((base64Update: string) => {
        const update = Uint8Array.from(atob(base64Update), c => c.charCodeAt(0));
        Y.applyUpdate(this.doc, update, this);
      });
    } catch (e) {
      console.error('[Yjs] Failed to apply sync updates', e);
    }
  };

  private onAwarenessUpdate = (msg: WSMessage) => {
    if (msg.noteId !== this.noteId || !msg.data) return;
    try {
      const update = Uint8Array.from(atob(msg.data), c => c.charCodeAt(0));
      awarenessProtocol.applyAwarenessUpdate(this.awareness, update, this);
    } catch (e) {
      console.error('[Yjs] Failed to apply awareness update', e);
    }
  };

  private onDocUpdate = (update: Uint8Array, origin: any) => {
    if (origin === this) return;
    const base64Update = btoa(String.fromCharCode(...update));
    wsClient.send('yjs_update', {
      noteId: this.noteId,
      data: base64Update
    });
  };

  private onLocalAwarenessUpdate = ({ added, updated, removed }: any, origin: any) => {
    if (origin === this) return;
    const changedClients = added.concat(updated).concat(removed);
    const update = awarenessProtocol.encodeAwarenessUpdate(this.awareness, changedClients);
    const base64Update = btoa(String.fromCharCode(...update));
    wsClient.send('yjs_awareness', {
      noteId: this.noteId,
      data: base64Update
    });
  };

  destroy() {
    wsClient.off('yjs_update', this.onUpdate);
    wsClient.off('yjs_sync', this.onSync);
    wsClient.off('yjs_awareness', this.onAwarenessUpdate);
    this.doc.off('update', this.onDocUpdate);
    this.awareness.off('update', this.onLocalAwarenessUpdate);
    super.destroy();
  }
}
