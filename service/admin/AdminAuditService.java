package com.fanfaction.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.Comment;
import com.fanfaction.entity.CreatorApplication;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.mapper.CommentMapper;
import com.fanfaction.mapper.CreatorApplicationMapper;
import com.fanfaction.mapper.UserMapper;
import com.fanfaction.vo.AuditItemVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端审核服务
 *
 * 各表 status 字段值：
 * Article:   0-DRAFT(草稿)  1-APPROVED(已发布)  2-PENDING(审核中)  3-REJECTED(已驳回)
 * Comment:   0-待审核        1-通过              2-驳回             3-已删除
 * CreatorApplication: 0-审核中  1-通过  2-拒绝
 */
@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuditService.class);

    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final CreatorApplicationMapper creatorApplicationMapper;
    private final UserMapper userMapper;
    private final AiAuditService aiAuditService;

    /**
     * 获取待审核列表（文章 + 评论 + 创作者申请）
     */
    public Page<AuditItemVO> getPendingList(int pageNum, int pageSize, String status, String keyword) {
        logger.info("审核列表查询 - pageNum: {}, pageSize: {}, status: {}, keyword: {}",
                pageNum, pageSize, status, keyword);

        List<AuditItemVO> allItems = new ArrayList<>();

        // -------------------------------------------------------
        // 1. 文章：PENDING=2, APPROVED=1, REJECTED=3
        // -------------------------------------------------------
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        Integer articleStatus = parseArticleStatus(status);
        if (articleStatus != null) {
            articleWrapper.eq(Article::getStatus, articleStatus);
        } else {
            // 只显示非草稿状态（排除 DRAFT=0）
            articleWrapper.gt(Article::getStatus, 0);
        }
        articleWrapper.orderByDesc(Article::getCreateTime);
        List<Article> articles = articleMapper.selectList(articleWrapper);

        if (!articles.isEmpty()) {
            List<Long> authorIds = articles.stream().map(Article::getAuthorId).distinct().collect(Collectors.toList());
            Map<Long, User> userMap = userMapper.selectBatchIds(authorIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            for (Article article : articles) {
                AuditItemVO vo = new AuditItemVO();
                vo.setId(article.getId());
                vo.setType("ARTICLE");
                vo.setTitle(article.getTitle());
                vo.setContent(article.getContent());
                vo.setAuthorId(article.getAuthorId());
                vo.setSubmitTime(article.getCreateTime());
                vo.setStatus(toArticleStatusStr(article.getStatus()));
                vo.setReviewComment(article.getReviewComment());

                User author = userMap.get(article.getAuthorId());
                if (author != null) {
                    vo.setAuthorNickname(author.getNickname());
                    vo.setAuthorUsername(author.getUsername());
                }
                allItems.add(vo);
            }
        }

        // -------------------------------------------------------
        // 2. 评论：待审核=0, 通过=1, 驳回=2, 已删除=3
        // -------------------------------------------------------
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        Integer commentStatus = parseCommentStatus(status);
        if (commentStatus != null) {
            commentWrapper.eq(Comment::getStatus, commentStatus);
        } else {
            // 显示所有未删除的评论（排除已删除=3）
            commentWrapper.ne(Comment::getStatus, 3);
        }
        commentWrapper.orderByDesc(Comment::getCreateTime);
        List<Comment> comments = commentMapper.selectList(commentWrapper);

        if (!comments.isEmpty()) {
            List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
            Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            for (Comment comment : comments) {
                AuditItemVO vo = new AuditItemVO();
                vo.setId(comment.getId());
                vo.setType("COMMENT");
                vo.setTitle("评论 #" + comment.getId());
                vo.setContent(comment.getContent());
                vo.setAuthorId(comment.getUserId());
                vo.setSubmitTime(comment.getCreateTime());
                vo.setStatus(toCommentStatusStr(comment.getStatus()));

                User author = userMap.get(comment.getUserId());
                if (author != null) {
                    vo.setAuthorNickname(author.getNickname());
                    vo.setAuthorUsername(author.getUsername());
                }
                allItems.add(vo);
            }
        }

        // -------------------------------------------------------
        // 3. 创作者申请：审核中=0, 通过=1, 拒绝=2
        // -------------------------------------------------------
        LambdaQueryWrapper<CreatorApplication> applyWrapper = new LambdaQueryWrapper<>();
        Integer applyStatus = parseApplyStatus(status);
        if (applyStatus != null) {
            applyWrapper.eq(CreatorApplication::getStatus, applyStatus);
        }
        applyWrapper.orderByDesc(CreatorApplication::getCreateTime);
        List<CreatorApplication> applications = creatorApplicationMapper.selectList(applyWrapper);

        if (!applications.isEmpty()) {
            List<Long> userIds = applications.stream().map(CreatorApplication::getUserId).distinct().collect(Collectors.toList());
            Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            for (CreatorApplication app : applications) {
                AuditItemVO vo = new AuditItemVO();
                vo.setId(app.getId());
                vo.setType("CREATOR_APPLY");
                vo.setTitle("创作者申请 - " + (app.getPenName() != null ? app.getPenName() : "用户#" + app.getUserId()));
                // 内容区组装申请摘要
                StringBuilder contentBuilder = new StringBuilder();
                if (app.getIntroduction() != null) {
                    contentBuilder.append("个人简介：").append(app.getIntroduction()).append("\n");
                }
                if (app.getExpertise() != null) {
                    contentBuilder.append("擅长领域：").append(app.getExpertise()).append("\n");
                }
                if (app.getRepresentativeWork() != null) {
                    contentBuilder.append("代表作品：").append(app.getRepresentativeWork());
                }
                vo.setContent(contentBuilder.toString());
                vo.setAuthorId(app.getUserId());
                vo.setSubmitTime(app.getCreateTime());
                vo.setStatus(toApplyStatusStr(app.getStatus()));
                vo.setReviewComment(app.getReviewComment());

                User author = userMap.get(app.getUserId());
                if (author != null) {
                    vo.setAuthorNickname(author.getNickname());
                    vo.setAuthorUsername(author.getUsername());
                }
                allItems.add(vo);
            }
        }

        // 按提交时间倒序
        allItems.sort((a, b) -> b.getSubmitTime().compareTo(a.getSubmitTime()));

        // 手动分页
        int total = allItems.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<AuditItemVO> pageItems = fromIndex < total ? allItems.subList(fromIndex, toIndex) : List.of();

        Page<AuditItemVO> voPage = new Page<>(pageNum, pageSize, total);
        voPage.setRecords(pageItems);

        logger.info("审核列表查询成功 - 总记录数: {}, 当前页记录数: {}", total, pageItems.size());
        return voPage;
    }

    /**
     * AI 预审指定内容
     */
    public String aiPreAudit(Long itemId, String type, String content) {
        logger.info("AI预审 - itemId: {}, type: {}", itemId, type);
        String result = aiAuditService.auditContent(content, type);
        logger.info("AI预审完成 - itemId: {}", itemId);
        return result;
    }

    /**
     * 执行审核操作。
     * 严格按 type 区分表结构和状态值，确保：
     *  - ARTICLE: 不误伤草稿（DRAFT=0），只处理 status >= 1 的记录
     *  - COMMENT: 使用评论专用的状态值
     *  - CREATOR_APPLY: 使用申请专用的状态值，通过时同步更新用户角色
     */
    @Transactional
    public boolean handleAudit(Long itemId, String type, String action, String reviewComment, Long reviewerId) {
        logger.info("审核操作 - itemId: {}, type: {}, action: {}, reviewerId: {}",
                itemId, type, action, reviewerId);

        if ("ARTICLE".equals(type)) {
            Article article = articleMapper.selectById(itemId);
            if (article == null) {
                logger.warn("文章不存在: {}", itemId);
                return false;
            }

            // Bug fix: 绝对不能修改草稿（DRAFT=0）
            if (article.getStatus() != null && article.getStatus() == 0) {
                logger.warn("拒绝操作草稿文章: itemId={}, 当前status=DRAFT", itemId);
                return false;
            }

            // Article status: 1=APPROVED, 3=REJECTED
            int newStatus = parseArticleAction(action);
            article.setStatus(newStatus);
            article.setReviewComment(reviewComment);
            article.setReviewerId(reviewerId);
            articleMapper.updateById(article);

            System.out.println("成功更新内容ID: " + itemId + ", 目标状态: " + newStatus
                    + " (" + action + "), 是否排除草稿: true, 原状态: " + article.getStatus());
            logger.info("文章审核完成 - itemId: {}, action: {}, newStatus: {}", itemId, action, newStatus);
        } else if ("COMMENT".equals(type)) {
            Comment comment = commentMapper.selectById(itemId);
            if (comment == null) {
                logger.warn("评论不存在: {}", itemId);
                return false;
            }

            // Comment status: 1=通过, 2=驳回
            int newStatus = parseCommentAction(action);
            comment.setStatus(newStatus);
            commentMapper.updateById(comment);

            System.out.println("成功更新内容ID: " + itemId + ", 目标状态: " + newStatus
                    + " (" + action + "), 类型: COMMENT");
            logger.info("评论审核完成 - itemId: {}, action: {}, newStatus: {}", itemId, action, newStatus);
        } else if ("CREATOR_APPLY".equals(type)) {
            CreatorApplication app = creatorApplicationMapper.selectById(itemId);
            if (app == null) {
                logger.warn("创作者申请不存在: {}", itemId);
                return false;
            }

            // CreatorApplication status: 1=通过, 2=拒绝
            int newStatus = parseApplyAction(action);
            app.setStatus(newStatus);
            app.setReviewComment(reviewComment);
            app.setReviewerId(reviewerId);
            app.setReviewTime(java.time.LocalDateTime.now());
            creatorApplicationMapper.updateById(app);

            // 通过时同步更新用户角色和 creatorStatus
            if (newStatus == 1) {
                User user = userMapper.selectById(app.getUserId());
                if (user != null) {
                    String roles = user.getRoles();
                    if (roles != null && !roles.contains("ROLE_CREATOR")) {
                        user.setRoles(roles + ",ROLE_CREATOR");
                    }
                    user.setCreatorStatus(2);  // 2-已通过
                    user.setRole(1);           // 1-创作者
                    userMapper.updateById(user);
                    logger.info("创作者申请通过后更新用户角色 - userId: {}, role=1, creatorStatus=2", app.getUserId());
                }
            } else if (newStatus == 2) {
                User user = userMapper.selectById(app.getUserId());
                if (user != null) {
                    user.setCreatorStatus(3);  // 3-已拒绝
                    userMapper.updateById(user);
                    logger.info("创作者申请被拒绝 - userId: {}, creatorStatus=3", app.getUserId());
                }
            }

            System.out.println("成功更新内容ID: " + itemId + ", 目标状态: " + newStatus
                    + " (" + action + "), 类型: CREATOR_APPLY");
            logger.info("创作者申请审核完成 - itemId: {}, action: {}, newStatus: {}", itemId, action, newStatus);
        } else {
            logger.warn("未知审核类型: {}", type);
            return false;
        }
        return true;
    }

    // ==================== Article status helpers ====================
    // Article: 0=DRAFT  1=APPROVED  2=PENDING  3=REJECTED

    private int parseArticleAction(String action) {
        switch (action.toUpperCase()) {
            case "APPROVED": return 1;
            case "REJECTED": return 3;
            default: return 3;
        }
    }

    private String toArticleStatusStr(Integer status) {
        if (status == null) return "PENDING";
        switch (status) {
            case 1: return "APPROVED";
            case 2: return "PENDING";
            case 3: return "REJECTED";
            default: return "PENDING";
        }
    }

    private Integer parseArticleStatus(String frontStatus) {
        if (frontStatus == null || frontStatus.isEmpty()) return null;
        switch (frontStatus.toUpperCase()) {
            case "PENDING":  return 2;
            case "APPROVED": return 1;
            case "REJECTED": return 3;
            default: return null;
        }
    }

    // ==================== Comment status helpers ====================
    // Comment: 0=待审核  1=通过  2=驳回  3=已删除

    private int parseCommentAction(String action) {
        switch (action.toUpperCase()) {
            case "APPROVED": return 1;
            case "REJECTED": return 2;
            default: return 2;
        }
    }

    private String toCommentStatusStr(Integer status) {
        if (status == null) return "PENDING";
        switch (status) {
            case 1: return "APPROVED";
            case 2: return "REJECTED";
            default: return "PENDING";
        }
    }

    private Integer parseCommentStatus(String frontStatus) {
        if (frontStatus == null || frontStatus.isEmpty()) return null;
        switch (frontStatus.toUpperCase()) {
            case "PENDING":  return 0;
            case "APPROVED": return 1;
            case "REJECTED": return 2;
            default: return null;
        }
    }

    // ==================== CreatorApplication status helpers ====================
    // CreatorApplication: 0=审核中  1=通过  2=拒绝

    private int parseApplyAction(String action) {
        switch (action.toUpperCase()) {
            case "APPROVED": return 1;
            case "REJECTED": return 2;
            default: return 2;
        }
    }

    private String toApplyStatusStr(Integer status) {
        if (status == null) return "PENDING";
        switch (status) {
            case 1: return "APPROVED";
            case 2: return "REJECTED";
            default: return "PENDING";
        }
    }

    private Integer parseApplyStatus(String frontStatus) {
        if (frontStatus == null || frontStatus.isEmpty()) return null;
        switch (frontStatus.toUpperCase()) {
            case "PENDING":  return 0;
            case "APPROVED": return 1;
            case "REJECTED": return 2;
            default: return null;
        }
    }
}
