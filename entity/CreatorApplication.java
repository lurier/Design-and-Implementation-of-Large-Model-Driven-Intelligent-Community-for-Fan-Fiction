package com.fanfaction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创作者申请实体
 */
@Data
@TableName("creator_application")
public class CreatorApplication {
    
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 笔名
     */
    private String penName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 擅长领域（逗号分隔）
     */
    private String expertise;
    
    /**
     * 个人简介
     */
    private String introduction;
    
    /**
     * 代表作品
     */
    private String representativeWork;
    
    /**
     * 联系方式
     */
    private String contact;
    
    /**
     * 审核状态：0-审核中 1-通过 2-拒绝
     */
    private Integer status;
    
    /**
     * 审核意见
     */
    private String reviewComment;
    
    /**
     * 审核人 ID
     */
    private Long reviewerId;
    
    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
