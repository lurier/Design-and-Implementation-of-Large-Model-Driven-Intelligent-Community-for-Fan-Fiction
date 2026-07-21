package com.fanfaction.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AuditItemVO {
    private Long id;
    /**
     * 类型: ARTICLE / COMMENT
     */
    private String type;
    private String title;
    private String content;
    private String authorNickname;
    private String authorUsername;
    private Long authorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    /**
     * 审核状态: PENDING-待审核 APPROVED-已通过 REJECTED-已驳回
     */
    private String status;
    private String reviewComment;

    /** AI预审结果 */
    private String aiRiskLevel;       // LOW / MEDIUM / HIGH
    private String aiReason;          // AI判定理由
    private String aiSentiment;       // 情感倾向
    private Double aiSentimentScore;  // 情感分值
    private List<Violation> aiViolations; // 违规点

    @Data
    public static class Violation {
        private String type;
        private String description;
    }
}
