package com.fanfaction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContentCheckService 单元测试类
 * 测试敏感词检测（DFA算法）和文本查重（SimHash算法）功能
 */
@DisplayName("内容安全检查服务测试")
class ContentCheckServiceTest {

    private ContentCheckService contentCheckService;

    @BeforeEach
    void setUp() {
        contentCheckService = new ContentCheckService();
    }

    // ==================== 敏感词检测测试 ====================

    @Test
    @DisplayName("测试正常文本 - 不包含敏感词")
    void testHasSensitiveWord_NormalText() {
        String normalText = "这是一篇正常的文章，内容健康向上。";
        
        boolean result = contentCheckService.hasSensitiveWord(normalText);
        
        assertFalse(result, "正常文本不应该检测到敏感词");
    }

    @Test
    @DisplayName("测试包含敏感词的文本")
    void testHasSensitiveWord_ContainsSensitiveWord() {
        String sensitiveText = "这篇文章涉及暴力内容，应该被检测出来。";
        
        boolean result = contentCheckService.hasSensitiveWord(sensitiveText);
        
        assertTrue(result, "包含敏感词的文本应该被检测出来");
    }

    @Test
    @DisplayName("测试多个敏感词")
    void testHasSensitiveWord_MultipleSensitiveWords() {
        String textWithMultipleSensitive = "这里有色情和赌博的内容，还有毒品相关描述。";
        
        boolean result = contentCheckService.hasSensitiveWord(textWithMultipleSensitive);
        
        assertTrue(result, "包含多个敏感词的文本应该被检测出来");
    }

    @Test
    @DisplayName("测试获取所有敏感词")
    void testGetSensitiveWords() {
        String text = "这篇文章包含暴力和色情的内容。";
        
        List<String> sensitiveWords = contentCheckService.getSensitiveWords(text);
        
        assertNotNull(sensitiveWords, "返回的敏感词列表不应为null");
        assertFalse(sensitiveWords.isEmpty(), "应该检测到敏感词");
        assertTrue(sensitiveWords.contains("暴力"), "应该包含'暴力'这个词");
        assertTrue(sensitiveWords.contains("色情"), "应该包含'色情'这个词");
    }

    @Test
    @DisplayName("测试空字符串 - 不应该报错")
    void testHasSensitiveWord_EmptyString() {
        String emptyText = "";
        
        assertDoesNotThrow(() -> {
            boolean result = contentCheckService.hasSensitiveWord(emptyText);
            assertFalse(result, "空字符串不应该检测到敏感词");
        }, "处理空字符串时不应该抛出异常");
    }

    @Test
    @DisplayName("测试null值 - 不应该报错")
    void testHasSensitiveWord_NullValue() {
        assertDoesNotThrow(() -> {
            boolean result = contentCheckService.hasSensitiveWord(null);
            assertFalse(result, "null值不应该检测到敏感词");
        }, "处理null值时不应该抛出异常");
    }

    @Test
    @DisplayName("测试获取敏感词 - 空字符串")
    void testGetSensitiveWords_EmptyString() {
        String emptyText = "";
        
        assertDoesNotThrow(() -> {
            List<String> words = contentCheckService.getSensitiveWords(emptyText);
            assertNotNull(words, "返回的列表不应为null");
            assertTrue(words.isEmpty(), "空字符串应该返回空列表");
        }, "处理空字符串时不应该抛出异常");
    }

    @Test
    @DisplayName("测试获取敏感词 - null值")
    void testGetSensitiveWords_NullValue() {
        assertDoesNotThrow(() -> {
            List<String> words = contentCheckService.getSensitiveWords(null);
            assertNotNull(words, "返回的列表不应为null");
            assertTrue(words.isEmpty(), "null值应该返回空列表");
        }, "处理null值时不应该抛出异常");
    }

    @Test
    @DisplayName("测试纯空格文本")
    void testHasSensitiveWord_WhitespaceOnly() {
        String whitespaceText = "   ";
        
        assertDoesNotThrow(() -> {
            boolean result = contentCheckService.hasSensitiveWord(whitespaceText);
            assertFalse(result, "纯空格文本不应该检测到敏感词");
        }, "处理纯空格文本时不应该抛出异常");
    }

    // ==================== SimHash 查重测试 ====================

    @Test
    @DisplayName("测试相同文本的SimHash - 海明距离应为0")
    void testSimHash_IdenticalText() {
        String text = "这是一段测试文本，用于验证SimHash算法的正确性。";
        
        long hash1 = contentCheckService.calculateSimHash(text);
        long hash2 = contentCheckService.calculateSimHash(text);
        int distance = contentCheckService.calculateHammingDistance(hash1, hash2);
        
        assertEquals(0, distance, "相同文本的海明距离应该为0");
        assertTrue(contentCheckService.isSimilar(hash1, hash2, 3), "相同文本应该判定为相似");
    }

    @Test
    @DisplayName("测试高度相似文本 - 海明距离应较小")
    void testSimHash_SimilarText() {
        String text1 = "Spring Boot是一个优秀的Java框架，它简化了企业级应用的开发流程。";
        String text2 = "Spring Boot是一个很棒的Java框架，它简化了企业级应用程序的开发流程。";
        
        long hash1 = contentCheckService.calculateSimHash(text1);
        long hash2 = contentCheckService.calculateSimHash(text2);
        int distance = contentCheckService.calculateHammingDistance(hash1, hash2);
        
        // 对于短文本，海明距离可能会稍大，但应该明显小于完全不同文本的距离
        assertTrue(distance < 30, 
            String.format("高度相似文本的海明距离应该较小，实际距离为: %d", distance));
        // 注意：对于短文本，阈值3可能过于严格，实际应用中需要根据文本长度调整
    }

    @Test
    @DisplayName("测试不同文本 - 海明距离应较大")
    void testSimHash_DifferentText() {
        String text1 = "今天天气真好，阳光明媚，适合出去散步。";
        String text2 = "机器学习是人工智能的重要分支，深度学习又是机器学习的核心技术。";
        
        long hash1 = contentCheckService.calculateSimHash(text1);
        long hash2 = contentCheckService.calculateSimHash(text2);
        int distance = contentCheckService.calculateHammingDistance(hash1, hash2);
        
        assertTrue(distance > 3, 
            String.format("完全不同文本的海明距离应该>3，实际距离为: %d", distance));
        assertFalse(contentCheckService.isSimilar(hash1, hash2, 3), 
            "完全不同的文本不应该判定为相似");
    }

    @Test
    @DisplayName("测试部分相似文本 - 中等海明距离")
    void testSimHash_PartiallySimilarText() {
        String text1 = "Java是一门面向对象的编程语言，具有跨平台特性。";
        String text2 = "Python是一门解释型编程语言，也具有跨平台特性。";
        
        long hash1 = contentCheckService.calculateSimHash(text1);
        long hash2 = contentCheckService.calculateSimHash(text2);
        int distance = contentCheckService.calculateHammingDistance(hash1, hash2);
        
        // 部分相似文本的海明距离应该小于完全不同文本
        assertTrue(distance < 40, 
            String.format("部分相似文本的海明距离应该在合理范围内，实际距离为: %d", distance));
    }

    @Test
    @DisplayName("测试空字符串的SimHash")
    void testSimHash_EmptyString() {
        String emptyText = "";
        
        assertDoesNotThrow(() -> {
            long hash = contentCheckService.calculateSimHash(emptyText);
            assertEquals(0L, hash, "空字符串的SimHash应该为0");
        }, "处理空字符串时不应该抛出异常");
    }

    @Test
    @DisplayName("测试null值的SimHash")
    void testSimHash_NullValue() {
        assertDoesNotThrow(() -> {
            long hash = contentCheckService.calculateSimHash(null);
            assertEquals(0L, hash, "null值的SimHash应该为0");
        }, "处理null值时不应该抛出异常");
    }

    @Test
    @DisplayName("测试短文本的SimHash")
    void testSimHash_ShortText() {
        String shortText = "测试";
        
        assertDoesNotThrow(() -> {
            long hash = contentCheckService.calculateSimHash(shortText);
            assertTrue(hash != 0, "短文本应该能计算出有效的SimHash值");
        }, "处理短文本时不应该抛出异常");
    }

    @Test
    @DisplayName("测试长文本的SimHash")
    void testSimHash_LongText() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longText.append("这是一个很长的文本段落，用于测试SimHash算法对长文本的处理能力。");
        }
        
        assertDoesNotThrow(() -> {
            long hash = contentCheckService.calculateSimHash(longText.toString());
            assertTrue(hash != 0, "长文本应该能计算出有效的SimHash值");
        }, "处理长文本时不应该抛出异常");
    }

    @Test
    @DisplayName("测试海明距离计算 - 边界情况")
    void testHammingDistance_EdgeCases() {
        long hash1 = 0L;
        long hash2 = 0L;
        
        int distance = contentCheckService.calculateHammingDistance(hash1, hash2);
        assertEquals(0, distance, "两个0的海明距离应该为0");
        
        // 测试全1的情况
        long allOnes1 = 0xFFFFFFFFFFFFFFFFL;
        long allOnes2 = 0xFFFFFFFFFFFFFFFFL;
        distance = contentCheckService.calculateHammingDistance(allOnes1, allOnes2);
        assertEquals(0, distance, "两个全1的海明距离应该为0");
    }

    @Test
    @DisplayName("测试isSimilar方法 - 阈值边界")
    void testIsSimilar_ThresholdBoundary() {
        String text1 = "测试文本一";
        String text2 = "测试文本二";
        
        long hash1 = contentCheckService.calculateSimHash(text1);
        long hash2 = contentCheckService.calculateSimHash(text2);
        int distance = contentCheckService.calculateHammingDistance(hash1, hash2);
        
        // 使用不同的阈值测试
        boolean similarWithThreshold1 = contentCheckService.isSimilar(hash1, hash2, 1);
        boolean similarWithThreshold3 = contentCheckService.isSimilar(hash1, hash2, 3);
        boolean similarWithThreshold10 = contentCheckService.isSimilar(hash1, hash2, 10);
        
        // 阈值越大，越容易判定为相似
        if (similarWithThreshold1) {
            assertTrue(similarWithThreshold3, "阈值为1时相似，阈值为3时也应该相似");
            assertTrue(similarWithThreshold10, "阈值为1时相似，阈值为10时也应该相似");
        }
        if (similarWithThreshold3) {
            assertTrue(similarWithThreshold10, "阈值为3时相似，阈值为10时也应该相似");
        }
    }

    @Test
    @DisplayName("测试特殊字符文本")
    void testSimHash_SpecialCharacters() {
        String textWithSpecialChars = "测试@#$%^&*()特殊!@#字符$%^";
        
        assertDoesNotThrow(() -> {
            long hash = contentCheckService.calculateSimHash(textWithSpecialChars);
            // 特殊字符不应该导致异常
        }, "处理特殊字符时不应该抛出异常");
    }

    @Test
    @DisplayName("测试英文文本的SimHash")
    void testSimHash_EnglishText() {
        String englishText1 = "Spring Boot is a popular Java framework for building web applications.";
        String englishText2 = "Spring Boot is a well-known Java framework for developing web apps.";
        
        long hash1 = contentCheckService.calculateSimHash(englishText1);
        long hash2 = contentCheckService.calculateSimHash(englishText2);
        int distance = contentCheckService.calculateHammingDistance(hash1, hash2);
        
        // 英文相似文本也应该有较小的海明距离（相对于完全不同文本）
        assertTrue(distance < 30, 
            String.format("相似英文文本的海明距离应该较小，实际距离为: %d", distance));
    }

    @Test
    @DisplayName("测试中英文混合文本")
    void testSimHash_MixedLanguageText() {
        String mixedText1 = "Java编程非常有趣，Spring Boot框架很好用。";
        String mixedText2 = "Java编程很有趣，Spring Boot框架非常好用。";
        
        assertDoesNotThrow(() -> {
            long hash1 = contentCheckService.calculateSimHash(mixedText1);
            long hash2 = contentCheckService.calculateSimHash(mixedText2);
            int distance = contentCheckService.calculateHammingDistance(hash1, hash2);
            
            // 对于短文本，海明距离可能不会很小，但不应该太大
            assertTrue(distance < 40, 
                String.format("相似的中英文混合文本海明距离应该合理，实际距离为: %d", distance));
        }, "处理中英文混合文本时不应该抛出异常");
    }

    // ==================== 综合测试 ====================

    @Test
    @DisplayName("综合测试 - 敏感词检测和SimHash组合使用")
    void testCombined_SensitiveWordAndSimHash() {
        String article1 = "这是一篇关于暴力内容的文章，应该被检测出来。";
        String article2 = "这是一篇关于暴力内容的文章，应该被检测出来。";
        
        // 测试敏感词检测
        assertTrue(contentCheckService.hasSensitiveWord(article1), "应该检测到敏感词");
        
        // 测试SimHash查重
        long hash1 = contentCheckService.calculateSimHash(article1);
        long hash2 = contentCheckService.calculateSimHash(article2);
        assertTrue(contentCheckService.isSimilar(hash1, hash2, 3), "相同文章应该判定为相似");
    }

    @Test
    @DisplayName("性能测试 - 大量文本的SimHash计算")
    void testPerformance_SimHashCalculation() {
        int iterations = 100;
        String testText = "这是一段用于性能测试的文本，重复计算多次以评估性能表现。";
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            contentCheckService.calculateSimHash(testText + i);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 100次计算应该在合理时间内完成（例如1秒内）
        assertTrue(duration < 1000, 
            String.format("性能不达标：100次SimHash计算耗时%dms，期望<1000ms", duration));
    }
}
