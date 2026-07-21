package com.fanfaction.service;

public interface SentimentAnalysisService {
    
    /**
     * 分析文本的情感倾向
     * @param text 待分析的文本
     * @return 情感分值 (-1.0 到 1.0)，负值表示消极，正值表示积极，0表示中性
     */
    double analyzeSentiment(String text);
    
    /**
     * 判断情感类型
     * @param score 情感分值
     * @return 情感类型：positive/negative/neutral
     */
    String getSentimentType(double score);
}
