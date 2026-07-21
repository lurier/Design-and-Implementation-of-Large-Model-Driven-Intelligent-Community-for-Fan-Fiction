package com.fanfaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fanfaction.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bookmark")
public class Bookmark extends BaseEntity {
    private Long userId;
    private Long articleId;
    private Integer position;
    private String note;
}
