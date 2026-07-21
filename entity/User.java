package com.fanfaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fanfaction.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private String roles;
    /**
     * 创作者状态：0-未申请 1-审核中 2-已通过 3-已拒绝
     */
    private Integer creatorStatus;

    /**
     * 角色：0-普通读者 1-创作者 2-系统管理员（管理员不能在前台发文，创作者不能调用管理接口）
     */
    private Integer role;
}
