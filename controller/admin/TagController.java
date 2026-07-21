package com.fanfaction.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fanfaction.common.Result;
import com.fanfaction.entity.Tag;
import com.fanfaction.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理")
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "分页查询标签列表")
    @GetMapping
    public Result<IPage<Tag>> getTagPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        IPage<Tag> page = tagService.getTagPage(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @Operation(summary = "添加标签")
    @PostMapping
    public Result<Void> addTag(@RequestBody Tag tag) {
        tagService.addTag(tag);
        return Result.success();
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setId(id);
        tagService.updateTag(tag);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }

    @Operation(summary = "切换标签启用/禁用状态")
    @PutMapping("/{id}/toggle")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        tagService.toggleStatus(id);
        return Result.success();
    }
}
