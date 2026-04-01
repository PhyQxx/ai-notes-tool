#!/bin/bash

echo "======================================="
echo "AI笔记工具 - 前端依赖安装脚本"
echo "======================================="
echo ""

echo "正在安装Tiptap富文本编辑器依赖..."
npm install @tiptap/core @tiptap/starter-kit @tiptap/vue-3 \
  @tiptap/extension-underline @tiptap/extension-text-align \
  @tiptap/extension-image @tiptap/extension-link \
  @tiptap/extension-placeholder

echo ""
echo "安装完成！"
echo ""
echo "运行以下命令启动开发服务器："
echo "  npm run dev"
echo ""
