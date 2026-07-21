package com.fanfaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fanfaction.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseEntity {
    private String title;
    private String content;
    private String summary;
    private String tags;
    private String coverImage;
    private Long authorId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer status;
    private String reviewComment;
    private Long reviewerId;
}
