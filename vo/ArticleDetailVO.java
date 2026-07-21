package com.fanfaction.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleDetailVO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String tags;
    private String coverImage;
    private Long authorId;
    private String authorName;
    private String authorNickname;
    private String authorAvatar;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer status;
    private Boolean isLiked;
    private Boolean isFavorited;
    private Integer readTime; // 阅读时长（分钟）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
