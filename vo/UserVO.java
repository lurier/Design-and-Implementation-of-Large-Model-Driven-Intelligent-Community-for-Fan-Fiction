package com.fanfaction.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String roles;
    private Integer status;
    /**
     * 创作者状态：0-未申请 1-审核中 2-已通过 3-已拒绝
     */
    private Integer creatorStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
