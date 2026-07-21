package com.fanfaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticleDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 500, message = "摘要不能超过500字")
    private String summary;

    private String tags;
    private String coverImage;
    private String status;
    private String reviewComment;
    private Long categoryId;
}
