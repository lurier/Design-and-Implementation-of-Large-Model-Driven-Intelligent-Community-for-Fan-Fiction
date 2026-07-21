package com.fanfaction.vo;

import com.fanfaction.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminCommentVO extends Comment {
    private String username;
    private String nickname;
    private String articleTitle;
    private String ipAddress;
}
