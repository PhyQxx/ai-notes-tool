import { VueRenderer } from '@tiptap/vue-3';
import tippy from 'tippy.js';
import SlashCommandList from './SlashCommandList.vue';
import { searchTitles } from '@/api/noteLink';
import { Document } from '@element-plus/icons-vue';

export default {
  items: async ({ query }: { query: string }) => {
    try {
      const notes = await searchTitles(query);
      return notes.map(n => ({
        title: n.title,
        id: n.id,
        icon: Document,
        command: ({ editor, range }: any) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .setNode('noteLink', { title: n.title })
            .insertContent(' ')
            .run();
        },
      }));
    } catch (e) {
      return [];
    }
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
