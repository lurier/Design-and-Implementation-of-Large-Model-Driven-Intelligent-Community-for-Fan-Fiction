package com.fanfaction.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    /**
     * 角色：READER-读者 CREATOR-创作者 ADMIN-管理员
     */
    private String role;
    /**
     * 状态：ACTIVE-正常 BLOCKED-封禁
     */
    private String status;
    /**
     * 创作者状态：NONE-未申请 PENDING-审核中 APPROVED-已通过 REJECTED-已拒绝
     */
    private String creatorStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;
}
