import { VueRenderer } from '@tiptap/vue-3';
import tippy from 'tippy.js';
import SlashCommandList from './SlashCommandList.vue';
import {
  Memo,
  List,
  Calendar,
  Document,
  Picture,
  Link,
  Operation,
  Cpu,
  EditPen,
  MagicStick
} from '@element-plus/icons-vue';

export default {
  items: ({ query }: { query: string }) => {
    return [
      {
        title: '一级标题',
        icon: Memo,
        command: ({ editor, range }: any) => {
          editor.chain().focus().deleteRange(range).setNode('heading', { level: 1 }).run();
        },
      },
      {
        title: '二级标题',
        icon: Memo,
        command: ({ editor, range }: any) => {
          editor.chain().focus().deleteRange(range).setNode('heading', { level: 2 }).run();
        },
      },
      {
        title: '三级标题',
        icon: Memo,
        command: ({ editor, range }: any) => {
          editor.chain().focus().deleteRange(range).setNode('heading', { level: 3 }).run();
        },
      },
      {
        title: '无序列表',
        icon: List,
        command: ({ editor, range }: any) => {
          editor.chain().focus().deleteRange(range).toggleBulletList().run();
        },
      },
      {
        title: '有序列表',
        icon: Operation,
        command: ({ editor, range }: any) => {
          editor.chain().focus().deleteRange(range).toggleOrderedList().run();
        },
      },
      {
        title: '加粗',
        icon: EditPen,
        command: ({ editor, range }: any) => {
          editor.chain().focus().deleteRange(range).setMark('bold').run();
        },
      },
      {
        title: '插入图片',
        icon: Picture,
        command: ({ editor, range }: any) => {
          editor.chain().focus().deleteRange(range).run();
          // This will be handled by the editor component to show dialog
          window.dispatchEvent(new CustomEvent('tiptap-insert-image'));
        },
      },
      {
        title: 'AI 总结',
        icon: Cpu,
        command: ({ editor, range }: any) => {
          editor.chain().focus().deleteRange(range).run();
          window.dispatchEvent(new CustomEvent('tiptap-ai-summarize'));
        },
      },
      {
        title: 'AI 提取待办',
        icon: MagicStick,
        command: ({ editor, range }: any) => {
          editor.chain().focus().deleteRange(range).run();
          window.dispatchEvent(new CustomEvent('tiptap-ai-extract-tasks'));
        },
      },
      {
        title: 'AI OCR 识图',
        icon: Picture,
        command: ({ editor, range }: any) => {
          editor.chain().focus().deleteRange(range).run();
          window.dispatchEvent(new CustomEvent('tiptap-ai-ocr'));
        },
      }
    ].filter(item => item.title.toLowerCase().startsWith(query.toLowerCase()));
  },

  render: () => {
    let component: VueRenderer;
    let popup: any;

    return {
      onStart: (props: any) => {
        component = new VueRenderer(SlashCommandList, {
          props,
          editor: props.editor,
        });

        if (!props.clientRect) {
          return;
        }

        popup = tippy('body', {
          getReferenceClientRect: props.clientRect,
          appendTo: () => document.body,
          content: component.element,
          showOnCreate: true,
          interactive: true,
          trigger: 'manual',
          placement: 'bottom-start',
        });
      },

      onUpdate(props: any) {
        component.updateProps(props);

        if (!props.clientRect) {
          return;
        }

        popup[0].setProps({
          getReferenceClientRect: props.clientRect,
        });
      },

      onKeyDown(props: any) {
        if (props.event.key === 'Escape') {
          popup[0].hide();
          return true;
        }

        return (component.ref as any)?.onKeyDown(props);
      },

      onExit() {
        popup[0].destroy();
        component.destroy();
      },
    };
  },
};
