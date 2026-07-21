package com.fanfaction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleUpdateDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 角色：0-普通读者 1-创作者 2-系统管理员
     */
    private Integer role;

    /**
     * 兼容旧的角色字符串字段
     */
    private String roles;
}
