package com.fanfaction.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileDTO {

    @Size(max = 50, message = "昵称长度不能超过 50 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过 20 个字符")
    private String phone;

    @Size(max = 5242880, message = "头像数据不能超过 5MB")
    private String avatar;
}
