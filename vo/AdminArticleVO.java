package com.fanfaction.vo;

import com.fanfaction.entity.Article;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminArticleVO extends Article {
    private String authorUsername;
    private String authorNickname;
    private String reviewerUsername;
}
