package com.fanfaction.service;

import java.util.List;
import java.util.Map;

public interface AiService {
    
    /**
     * 生成文章摘要
     * @param content 文章内容
     * @return 200字左右的摘要
     */
    String generateSummary(String content);
    
    /**
     * 提取文章标签
     * @param content 文章内容
     * @return 3-5个核心标签列表
     */
    List<String> generateTags(String content);

    /**
     * 评论情绪检测
     * @param content 评论内容
     * @return Map 包含 is_negative (Boolean) 和 emotion_type (String)
     */
    Map<String, Object> detectEmotion(String content);
}
