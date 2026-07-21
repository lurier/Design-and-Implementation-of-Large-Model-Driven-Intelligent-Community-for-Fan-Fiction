package com.fanfaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fanfaction.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tags")
public class Tag extends BaseEntity {
    private String name;
    private String category;
    private String color;
    private Integer sortOrder;
    private Integer status; // 0-禁用 1-启用
}
