package com.fanfaction.service.impl;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.fanfaction.service.SentimentAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SentimentAnalysisServiceImpl implements SentimentAnalysisService {

    // 积极情感词库（示例，实际应用中应该使用更完整的词库）
    private static final String[] POSITIVE_WORDS = {
        "好", "棒", "优秀", "喜欢", "爱", "赞", "精彩", "完美", "满意", "开心",
        "高兴", "快乐", "幸福", "美好", "赞扬", "崇拜", "支持", "推荐", "值得",
        "不错", "厉害", "强大", "实用", "方便", "简单", "清晰", "准确", "专业"
    };

    // 消极情感词库（示例）
    private static final String[] NEGATIVE_WORDS = {
        "差", "坏", "糟糕", "讨厌", "恨", "烂", "失望", "不满", "生气", "难过",
        "伤心", "痛苦", "悲哀", "遗憾", "批评", "反对", "抵制", "垃圾", "无用",
        "复杂", "困难", "模糊", "错误", "问题", "bug", "故障", "慢", "卡"
    };

    // 程度副词及其权重
    private static final java.util.Map<String, Double> DEGREE_ADVERBS = new java.util.HashMap<>();
    static {
        DEGREE_ADVERBS.put("非常", 2.0);
        DEGREE_ADVERBS.put("特别", 1.8);
        DEGREE_ADVERBS.put("很", 1.5);
        DEGREE_ADVERBS.put("太", 1.5);
        DEGREE_ADVERBS.put("比较", 0.8);
        DEGREE_ADVERBS.put("有点", 0.5);
        DEGREE_ADVERBS.put("稍微", 0.3);
    }

    @Override
    public double analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        try {
            // 使用HanLP进行分词
            List<Term> terms = HanLP.segment(text);
            
            double sentimentScore = 0.0;
            int validWordCount = 0;
            double degreeWeight = 1.0; // 当前程度副词权重

            for (Term term : terms) {
                String word = term.word;
                
                // 检查是否为程度副词
                if (DEGREE_ADVERBS.containsKey(word)) {
                    degreeWeight = DEGREE_ADVERBS.get(word);
                    continue;
                }

                // 检查是否为积极词
                if (isPositiveWord(word)) {
                    sentimentScore += degreeWeight;
                    validWordCount++;
                    degreeWeight = 1.0; // 重置权重
                }
                // 检查是否为消极词
                else if (isNegativeWord(word)) {
                    sentimentScore -= degreeWeight;
                    validWordCount++;
                    degreeWeight = 1.0; // 重置权重
                }
            }

            // 归一化到 [-1, 1] 区间
            if (validWordCount > 0) {
                // 使用tanh函数将分数映射到[-1, 1]
                sentimentScore = Math.tanh(sentimentScore / Math.max(validWordCount, 5));
            }

            log.debug("文本情感分析结果: score={}, text={}", sentimentScore, 
                     text.length() > 50 ? text.substring(0, 50) + "..." : text);
            
            return sentimentScore;

        } catch (Exception e) {
            log.error("情感分析失败，使用默认值0.0", e);
            return 0.0;
        }
    }

    @Override
    public String getSentimentType(double score) {
        if (score > 0.1) {
            return "positive";
        } else if (score < -0.1) {
            return "negative";
        } else {
            return "neutral";
        }
    }

    /**
     * 判断是否为积极情感词
     */
    private boolean isPositiveWord(String word) {
        for (String posWord : POSITIVE_WORDS) {
            if (word.contains(posWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为消极情感词
     */
    private boolean isNegativeWord(String word) {
        for (String negWord : NEGATIVE_WORDS) {
            if (word.contains(negWord)) {
                return true;
            }
        }
        return false;
    }
}
