package com.fanfaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InteractionDTO {
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
}
