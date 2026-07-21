package com.fanfaction.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 基于 DFA（确定有穷自动机）算法的敏感词过滤工具类
 */
public class SensitiveWordUtil {

    /**
     * DFA 节点
     */
    private static class DFANode {
        private final Map<Character, DFANode> children = new HashMap<>();
        private boolean isEnd = false;
    }

    private static final DFANode ROOT = new DFANode();

    /**
     * 从类路径加载敏感词词典并构建 DFA 树
     */
    static {
        loadSensitiveWords();
    }

    private static void loadSensitiveWords() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(
                                SensitiveWordUtil.class.getClassLoader()
                                        .getResourceAsStream("sensitive_words.txt")),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    addWord(word);
                }
            }
        } catch (Exception e) {
            // 如果词典文件不存在，使用默认敏感词
            String[] defaultWords = {
                    "暴力", "色情", "赌博", "毒品", "枪支",
                    "反动", "政治", "自杀", "爆炸", "恐怖"
            };
            for (String word : defaultWords) {
                addWord(word);
            }
        }
    }

    /**
     * 向 DFA 树中添加敏感词
     */
    private static void addWord(String word) {
        DFANode currentNode = ROOT;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            DFANode child = currentNode.children.get(c);
            if (child == null) {
                child = new DFANode();
                currentNode.children.put(c, child);
            }
            currentNode = child;
        }
        currentNode.isEnd = true;
    }

    /**
     * 检测文本中是否包含敏感词
     *
     * @param text 待检测文本
     * @return true 表示包含敏感词，false 表示不包含
     */
    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            DFANode currentNode = ROOT;
            for (int j = i; j < text.length(); j++) {
                char c = text.charAt(j);
                DFANode child = currentNode.children.get(c);
                if (child == null) {
                    break;
                }
                currentNode = child;
                if (currentNode.isEnd) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取文本中包含的所有敏感词
     *
     * @param text 待检测文本
     * @return 敏感词列表
     */
    public static List<String> getSensitiveWords(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        for (int i = 0; i < text.length(); i++) {
            DFANode currentNode = ROOT;
            for (int j = i; j < text.length(); j++) {
                char c = text.charAt(j);
                DFANode child = currentNode.children.get(c);
                if (child == null) {
                    break;
                }
                currentNode = child;
                if (currentNode.isEnd) {
                    String word = text.substring(i, j + 1);
                    if (!result.contains(word)) {
                        result.add(word);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 替换文本中的敏感词为指定字符
     *
     * @param text        待检测文本
     * @param replacement 替换字符
     * @return 替换后的文本
     */
    public static String replaceSensitiveWords(String text, String replacement) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);
        for (int i = 0; i < result.length(); i++) {
            DFANode currentNode = ROOT;
            for (int j = i; j < result.length(); j++) {
                char c = result.charAt(j);
                DFANode child = currentNode.children.get(c);
                if (child == null) {
                    break;
                }
                currentNode = child;
                if (currentNode.isEnd) {
                    for (int k = i; k <= j; k++) {
                        result.setCharAt(k, replacement.charAt(0));
                    }
                }
            }
        }
        return result.toString();
    }
}
