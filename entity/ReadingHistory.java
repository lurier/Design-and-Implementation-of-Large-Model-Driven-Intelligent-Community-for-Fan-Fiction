package com.fanfaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fanfaction.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reading_history")
public class ReadingHistory extends BaseEntity {
    private Long userId;
    private Long articleId;
    private Integer readDuration;
    private Integer scrollPosition;
    private Double readPercentage;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReadTime;
}
