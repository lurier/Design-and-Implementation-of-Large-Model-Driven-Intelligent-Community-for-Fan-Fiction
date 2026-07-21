package com.fanfaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fanfaction.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class Comment extends BaseEntity {
    private Long userId;
    private Long articleId;
    private Long parentId;
    private Long replyUserId;
    private String content;
    private Integer likeCount;
    private Integer status;
    private Double sentimentScore; // 情感分值：-1.0(消极) 到 1.0(积极)
}
