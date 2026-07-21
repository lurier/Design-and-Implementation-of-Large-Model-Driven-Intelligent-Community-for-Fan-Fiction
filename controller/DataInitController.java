package com.fanfaction.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.Interaction;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.mapper.InteractionMapper;
import com.fanfaction.mapper.UserMapper;
import com.fanfaction.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/data/init")
public class DataInitController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private InteractionMapper interactionMapper;

    @Autowired
    private TagService tagService;

    private static final String[] NICKNAMES = {
        "代码艺术家", "技术探索者", "前端达人", "后端高手", "全栈工程师",
        "数据科学家", "AI 爱好者", "开源贡献者", "技术博主", "编程爱好者"
    };

    private static final String[] USERNAMES = {
        "code_artist", "tech_explorer", "frontend_master", "backend_guru", "fullstack_dev",
        "data_scientist", "ai_enthusiast", "open_source", "tech_blogger", "programming_fan"
    };

    private static final String[] ARTICLE_TITLES = {
        "深入理解 Vue 3 组合式 API 的核心概念",
        "Spring Boot 3 实战：从零构建 RESTful API",
        "TypeScript 高级类型技巧与最佳实践",
        "MySQL 性能优化：索引设计与查询调优",
        "Redis 缓存策略：提升系统性能的秘诀",
        "微服务架构设计模式与实战经验",
        "Docker 容器化部署完整指南",
        "Kubernetes 入门到精通",
        "Git 工作流与团队协作最佳实践",
        "JavaScript 异步编程：从 Callback 到 Async/Await",
        "React Hooks 完全指南",
        "Node.js 高性能服务器开发",
        "Python 数据分析入门教程",
        "机器学习算法原理与实现",
        "网络安全基础与防护策略",
        "Linux 系统管理与运维技巧",
        "消息队列 RabbitMQ 实战应用",
        "Elasticsearch 全文检索引擎详解",
        "MongoDB 文档数据库使用指南",
        "GraphQL API 设计与实现"
    };

    private static final String[] SUMMARIES = {
        "本文详细介绍了核心概念和实际应用，帮助你快速掌握关键技术要点。",
        "通过本教程，你将学会如何从零开始构建一个完整的项目，包含完整的代码示例。",
        "总结了一系列实用技巧，让你的代码更加优雅和高效。",
        "深入探讨性能优化的各个方面，包括索引设计、查询优化和配置调优。",
        "分享缓存策略的最佳实践，帮助你大幅提升系统响应速度。",
        "结合实际项目经验，讲解微服务架构的设计模式和落地方法。",
        "完整的容器化部署指南，涵盖从开发到生产的全流程。",
        "系统讲解 Kubernetes 的核心概念和实际应用场景。",
        "介绍高效的 Git 工作流，提升团队协作效率。",
        "全面解析 JavaScript 异步编程的演进历程和现代写法。"
    };

    private static final String[][] TAGS_ARRAY = {
        {"Vue", "JavaScript", "前端"},
        {"Spring Boot", "Java", "后端"},
        {"TypeScript", "JavaScript"},
        {"MySQL", "数据库", "性能优化"},
        {"Redis", "缓存", "性能优化"},
        {"微服务", "架构设计"},
        {"Docker", "DevOps"},
        {"Kubernetes", "DevOps"},
        {"Git", "团队协作"},
        {"JavaScript", "异步编程"},
        {"React", "前端"},
        {"Node.js", "后端"},
        {"Python", "数据分析"},
        {"机器学习", "AI"},
        {"网络安全", "运维"},
        {"Linux", "运维"},
        {"RabbitMQ", "消息队列"},
        {"Elasticsearch", "搜索引擎"},
        {"MongoDB", "数据库"},
        {"GraphQL", "API"}
    };

    private static final String CONTENT_TEMPLATE = "# %s\n\n## 前言\n\n%s\n\n## 正文内容\n\n这是文章的详细内容部分。在实际应用中，这里会包含丰富的代码示例、图表说明和详细的技术讲解。\n\n### 核心要点\n\n1. **第一个要点**：详细说明第一个重要概念\n2. **第二个要点**：深入讲解第二个关键技术\n3. **第三个要点**：总结第三个实践方法\n\n## 代码示例\n\n```java\n// 示例代码\npublic class Example {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}\n```\n\n## 总结\n\n通过本文的学习，你应该已经掌握了相关的核心概念和实践方法。希望这些内容能够帮助你在实际项目中更好地应用这些技术。\n\n## 参考资料\n\n- 官方文档\n- 相关技术博客\n- 开源项目案例\n";

    @PostMapping("/users")
    public Map<String, Object> initUsers() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            List<User> users = new ArrayList<>();
            
            for (int i = 0; i < 5; i++) {
                User user = new User();
                user.setUsername(USERNAMES[i]);
                user.setPassword(encoder.encode("123456"));
                user.setNickname(NICKNAMES[i]);
                user.setEmail(String.format("user%d@example.com", i + 1));
                user.setPhone(String.format("1380013800%d", i));
                user.setStatus(1);
                user.setRoles("ROLE_USER");
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                users.add(user);
            }
            
            for (User user : users) {
                userMapper.insert(user);
            }
            
            result.put("success", true);
            result.put("message", "成功初始化 " + users.size() + " 个用户");
            result.put("data", Arrays.asList(
                "用户名：code_artist, 密码：123456",
                "用户名：tech_explorer, 密码：123456",
                "用户名：frontend_master, 密码：123456",
                "用户名：backend_guru, 密码：123456",
                "用户名：fullstack_dev, 密码：123456"
            ));
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "初始化用户失败：" + e.getMessage());
        }
        
        return result;
    }

    @PostMapping("/articles")
    public Map<String, Object> initArticles() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery()
                .eq(User::getStatus, 1));
            
            if (users.isEmpty()) {
                result.put("success", false);
                result.put("message", "请先初始化用户数据");
                return result;
            }
            
            Random random = new Random();
            List<Article> articles = new ArrayList<>();
            
            for (int i = 0; i < 20; i++) {
                Article article = new Article();
                article.setTitle(ARTICLE_TITLES[i]);
                article.setSummary(SUMMARIES[i % SUMMARIES.length]);
                article.setContent(String.format(CONTENT_TEMPLATE, ARTICLE_TITLES[i], SUMMARIES[i % SUMMARIES.length]));
                article.setTags(String.join(",", TAGS_ARRAY[i]));
                article.setAuthorId(users.get(random.nextInt(users.size())).getId());
                article.setViewCount(random.nextInt(5000) + 100);
                article.setLikeCount(random.nextInt(500) + 10);
                article.setFavoriteCount(random.nextInt(200) + 5);
                article.setCommentCount(random.nextInt(100) + 1);
                article.setStatus(1);
                article.setCreateTime(LocalDateTime.now().minusDays(random.nextInt(30)));
                article.setUpdateTime(LocalDateTime.now());
                articles.add(article);
            }
            
            for (Article article : articles) {
                articleMapper.insert(article);
            }
            
            result.put("success", true);
            result.put("message", "成功初始化 " + articles.size() + " 篇文章");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "初始化文章失败：" + e.getMessage());
        }
        
        return result;
    }

    @PostMapping("/sync-tags")
    public Map<String, Object> syncTags() {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = tagService.syncAllExistingTags();
            result.put("success", true);
            result.put("message", "标签同步完成，扫描到 " + count + " 个唯一标签");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "标签同步失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/all")
    public Map<String, Object> initAll() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> userResult = initUsers();
            if (!(Boolean) userResult.get("success")) {
                result.put("success", false);
                result.put("message", "初始化用户失败");
                return result;
            }
            
            Map<String, Object> articleResult = initArticles();
            if (!(Boolean) articleResult.get("success")) {
                result.put("success", false);
                result.put("message", "初始化文章失败");
                return result;
            }
            
            result.put("success", true);
            result.put("message", "数据初始化完成");
            result.put("users", userResult.get("data"));
            result.put("articlesCount", articleResult.get("message"));
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "初始化失败：" + e.getMessage());
        }
        
        return result;
    }
}
