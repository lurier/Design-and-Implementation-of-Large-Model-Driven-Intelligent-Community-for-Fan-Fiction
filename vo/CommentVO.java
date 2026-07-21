package com.fanfaction.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long articleId;
    private Long parentId;
    private Long replyUserId;
    private String replyUserName;
    private String content;
    private Integer likeCount;
    private Long userId;
    private String userName;
    private String userNickname;
    private String userAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    // 嵌套的回复列表
    private List<CommentVO> replies;
}
