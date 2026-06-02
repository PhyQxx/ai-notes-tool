package com.ainotes.config;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.TokenStream;

public interface KnowledgeAssistant {

    @SystemMessage({
        "你是一个专业的AI笔记助手。你的任务是根据提供的笔记内容回答用户的问题。",
        "如果提供的背景信息中没有答案，请委婉地告知用户你不知道，不要胡乱猜测。",
        "请保持回答简洁、专业，并尽可能使用Markdown格式。"
    })
    String answer(String query);

    @SystemMessage({
        "你是一个专业的AI笔记助手。你的任务是根据提供的笔记内容回答用户的问题。",
        "如果提供的背景信息中没有答案，请委婉地告知用户你不知道，不要胡乱猜测。",
        "请保持回答简洁、专业，并尽可能使用Markdown格式。"
    })
    TokenStream answerStream(String query);
}
