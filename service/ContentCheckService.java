package com.fanfaction.service;

import com.fanfaction.util.SensitiveWordUtil;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容安全检查服务
 * 提供敏感词过滤和文本查重功能
 */
@Service
public class ContentCheckService {

    /**
     * 检测文本中是否包含敏感词
     *
     * @param text 待检测文本
     * @return true 表示包含敏感词，false 表示不包含
     */
    public boolean hasSensitiveWord(String text) {
        return SensitiveWordUtil.containsSensitiveWord(text);
    }

    /**
     * 获取文本中包含的所有敏感词
     *
     * @param text 待检测文本
     * @return 敏感词列表
     */
    public List<String> getSensitiveWords(String text) {
        return SensitiveWordUtil.getSensitiveWords(text);
    }

    // ==================== SimHash 文本查重 ====================

    /**
     * SimHash 指纹长度（64位）
     */
    private static final int HASH_BITS = 64;

    /**
     * 计算文本的 SimHash 指纹
     *
     * @param text 输入文本
     * @return 64位 SimHash 值
     */
    public long calculateSimHash(String text) {
        if (text == null || text.isEmpty()) {
            return 0L;
        }

        // 分词并计算词频
        Map<String, Integer> wordFreq = new HashMap<>();
        String[] words = text.split("\\s+|[\\uff0c\\u3002\\uff01\\uff1f\\uff1b\\uff1a\\u3001\u201c\u201d\u2018\u2019\\uff08\\uff09\\u3010\\u3011<>.,!?;:\"'()\\[\\]{}]+");
        for (String word : words) {
            if (!word.isEmpty() && word.length() >= 2) {
                wordFreq.merge(word, 1, Integer::sum);
            }
        }

        // 初始化64维向量
        int[] vector = new int[HASH_BITS];

        // 对每个词计算hash并加权
        for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
            String word = entry.getKey();
            int weight = entry.getValue();
            long hash = hashString(word);

            for (int i = 0; i < HASH_BITS; i++) {
                if (((hash >> (HASH_BITS - 1 - i)) & 1) == 1) {
                    vector[i] += weight;
                } else {
                    vector[i] -= weight;
                }
            }
        }

        // 降维，生成最终的SimHash值
        long simHash = 0;
        for (int i = 0; i < HASH_BITS; i++) {
            if (vector[i] > 0) {
                simHash |= (1L << (HASH_BITS - 1 - i));
            }
        }

        return simHash;
    }

    /**
     * 计算两个 SimHash 指纹之间的海明距离
     *
     * @param hash1 第一个 SimHash 值
     * @param hash2 第二个 SimHash 值
     * @return 海明距离（0表示完全相同，值越小越相似）
     */
    public int calculateHammingDistance(long hash1, long hash2) {
        long xor = hash1 ^ hash2;
        int distance = 0;
        while (xor != 0) {
            distance++;
            xor &= (xor - 1); // 清除最低位的1
        }
        return distance;
    }

    /**
     * 判断两段文本是否相似（海明距离 <= 阈值）
     *
     * @param hash1     第一个 SimHash 值
     * @param hash2     第二个 SimHash 值
     * @param threshold 海明距离阈值（通常取3）
     * @return true 表示相似
     */
    public boolean isSimilar(long hash1, long hash2, int threshold) {
        return calculateHammingDistance(hash1, hash2) <= threshold;
    }

    /**
     * 字符串 hash 函数（FNV-1a 变体）
     */
    private long hashString(String str) {
        long hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = hash * 31 + str.charAt(i);
        }
        // 使用位混合增强散列性
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdL;
        hash ^= (hash >>> 33);
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= (hash >>> 33);
        return hash;
    }
}
