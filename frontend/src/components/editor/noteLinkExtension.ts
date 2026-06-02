import { Node, mergeAttributes } from '@tiptap/core';
import Suggestion from '@tiptap/suggestion';
import noteLinkSuggestion from './noteLinkSuggestion';

export const NoteLink = Node.create({
  name: 'noteLink',
  group: 'inline',
  inline: true,
  selectable: true,
  atom: true,

  addAttributes() {
    return {
      title: {
        default: null,
      },
    };
  },

  parseHTML() {
    return [
      {
        tag: 'span[data-type="note-link"]',
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
    return ['span', mergeAttributes(HTMLAttributes, { 'data-type': 'note-link', class: 'note-link' }), `[[${HTMLAttributes.title}]]` ];
  },

  addOptions() {
    return {
      suggestion: {
        char: '[[',
        ...noteLinkSuggestion,
      },
    };
  },

  addProseMirrorPlugins() {
    return [
      Suggestion({
        editor: this.editor,
        ...this.options.suggestion,
      }),
    ];
  },
});
