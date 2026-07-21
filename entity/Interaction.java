package com.fanfaction.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interaction")
public class Interaction {
    @TableId
    private Long id;
    private Long userId;
    private Long articleId;
    private Integer type;
    private LocalDateTime createTime;
}
