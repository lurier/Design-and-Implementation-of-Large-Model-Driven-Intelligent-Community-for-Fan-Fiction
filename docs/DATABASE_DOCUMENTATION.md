# FanFaction 数据库文档

## 概述

- **数据库名称**: `fanfaction`
- **默认字符集**: `utf8mb4`
- **默认排序规则**: `utf8mb4_unicode_ci`
- **存储引擎**: `InnoDB`

---

## 一、数据库表信息

### 1.1 sys_user（用户表）

**用途**: 存储系统用户的基本信息，包括普通读者、创作者和系统管理员。

| 字段名 | 数据类型 | 长度 | 主键 | 允许为空 | 默认值 | 字段描述 |
|--------|----------|------|------|----------|--------|----------|
| id | BIGINT | - | 是 | 否 | AUTO_INCREMENT | 主键ID |
| username | VARCHAR | 50 | - | 否 | - | 用户名 |
| password | VARCHAR | 100 | - | 否 | - | 密码（BCrypt加密） |
| nickname | VARCHAR | 50 | - | 是 | NULL | 昵称 |
| email | VARCHAR | 100 | - | 是 | NULL | 邮箱 |
| phone | VARCHAR | 20 | - | 是 | NULL | 手机号 |
| avatar | TEXT | - | - | 是 | NULL | 头像URL/Base64 |
| status | TINYINT | - | - | 是 | 1 | 状态：0-禁用，1-启用 |
| roles | VARCHAR | 100 | - | 是 | ROLE_USER | Spring Security角色，多个角色逗号分隔 |
| creator_status | TINYINT | - | - | 是 | 0 | 创作者状态：0-未申请，1-审核中，2-已通过，3-已拒绝 |
| role | INT | - | - | 是 | 0 | 角色：0-普通读者，1-创作者，2-系统管理员 |
| deleted | TINYINT | - | - | 是 | 0 | 逻辑删除：0-未删除，1-已删除 |
| create_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

---

### 1.2 article（文章表）

**用途**: 存储创作者发布的文章/作品信息，支持草稿、发布、审核、下架等状态管理。

| 字段名 | 数据类型 | 长度 | 主键 | 允许为空 | 默认值 | 字段描述 |
|--------|----------|------|------|----------|--------|----------|
| id | BIGINT | - | 是 | 否 | AUTO_INCREMENT | 主键ID |
| title | VARCHAR | 200 | - | 否 | - | 文章标题 |
| content | LONGTEXT | - | - | 否 | - | 文章正文内容 |
| summary | VARCHAR | 500 | - | 是 | NULL | 文章摘要 |
| tags | VARCHAR | 255 | - | 是 | NULL | 标签，多个标签用逗号分隔 |
| cover_image | VARCHAR | 255 | - | 是 | NULL | 封面图片URL |
| author_id | BIGINT | - | - | 否 | - | 作者ID，关联 `sys_user.id` |
| view_count | INT | - | - | 是 | 0 | 浏览次数 |
| like_count | INT | - | - | 是 | 0 | 点赞数 |
| favorite_count | INT | - | - | 是 | 0 | 收藏数 |
| comment_count | INT | - | - | 是 | 0 | 评论数 |
| status | TINYINT | - | - | 是 | 1 | 状态：0-草稿，1-已发布，2-审核中，3-已驳回 |
| review_comment | TEXT | - | - | 是 | NULL | 审核意见 |
| reviewer_id | BIGINT | - | - | 是 | NULL | 审核人ID，关联 `sys_user.id` |
| deleted | TINYINT | - | - | 是 | 0 | 逻辑删除：0-未删除，1-已删除 |
| create_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

---

### 1.3 comment（评论表）

**用途**: 存储用户对文章的评论，支持多层级回复（楼中楼）和情感分析。

| 字段名 | 数据类型 | 长度 | 主键 | 允许为空 | 默认值 | 字段描述 |
|--------|----------|------|------|----------|--------|----------|
| id | BIGINT | - | 是 | 否 | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | - | - | 否 | - | 评论用户ID，关联 `sys_user.id` |
| article_id | BIGINT | - | - | 否 | - | 文章ID，关联 `article.id` |
| parent_id | BIGINT | - | - | 是 | 0 | 父评论ID，0表示顶级评论 |
| reply_user_id | BIGINT | - | - | 是 | NULL | 回复的目标用户ID，关联 `sys_user.id` |
| content | VARCHAR | 1000 | - | 否 | - | 评论内容 |
| like_count | INT | - | - | 是 | 0 | 点赞数 |
| status | TINYINT | - | - | 是 | 1 | 状态：0-隐藏，1-正常 |
| sentiment_score | DECIMAL | (5,2) | - | 是 | NULL | 情感分值：-1.0（消极）到 1.0（积极） |
| deleted | TINYINT | - | - | 是 | 0 | 逻辑删除：0-未删除，1-已删除 |
| create_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

---

### 1.4 interaction（互动表）

**用途**: 记录用户对文章的互动行为（点赞/收藏），每个用户对同一文章的同类型互动只能有一条记录。

| 字段名 | 数据类型 | 长度 | 主键 | 允许为空 | 默认值 | 字段描述 |
|--------|----------|------|------|----------|--------|----------|
| id | BIGINT | - | 是 | 否 | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | - | - | 否 | - | 用户ID，关联 `sys_user.id` |
| article_id | BIGINT | - | - | 否 | - | 文章ID，关联 `article.id` |
| type | TINYINT | - | - | 否 | - | 互动类型：1-点赞，2-收藏 |
| create_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |

---

### 1.5 creator_application（创作者申请表）

**用途**: 存储用户申请成为创作者的申请信息和审核记录，每个用户只能有一条有效申请。

| 字段名 | 数据类型 | 长度 | 主键 | 允许为空 | 默认值 | 字段描述 |
|--------|----------|------|------|----------|--------|----------|
| id | BIGINT | - | 是 | 否 | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | - | - | 否 | - | 申请人用户ID，关联 `sys_user.id` |
| pen_name | VARCHAR | 50 | - | 是 | NULL | 笔名 |
| email | VARCHAR | 100 | - | 是 | NULL | 申请人邮箱 |
| expertise | VARCHAR | 100 | - | 是 | NULL | 擅长领域（逗号分隔） |
| introduction | VARCHAR | 500 | - | 是 | NULL | 个人简介 |
| representative_work | VARCHAR | 500 | - | 是 | NULL | 代表作品 |
| contact | VARCHAR | 50 | - | 是 | NULL | 联系方式 |
| status | TINYINT | - | - | 是 | 0 | 审核状态：0-待审核，1-通过，2-驳回 |
| review_comment | VARCHAR | 500 | - | 是 | NULL | 审核意见 |
| reviewer_id | BIGINT | - | - | 是 | NULL | 审核人ID，关联 `sys_user.id` |
| review_time | DATETIME | - | - | 是 | NULL | 审核时间 |
| deleted | TINYINT | - | - | 是 | 0 | 逻辑删除：0-未删除，1-已删除 |
| create_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

---

### 1.6 reading_history（阅读历史记录表）

**用途**: 记录用户的阅读历史，追踪每篇文章的阅读时长、进度和最后阅读时间。用户与文章为一对一关系（同一用户对同一文章只有一条记录）。

| 字段名 | 数据类型 | 长度 | 主键 | 允许为空 | 默认值 | 字段描述 |
|--------|----------|------|------|----------|--------|----------|
| id | BIGINT | - | 是 | 否 | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | - | - | 否 | - | 用户ID，关联 `sys_user.id` |
| article_id | BIGINT | - | - | 否 | - | 文章ID，关联 `article.id` |
| read_duration | INT | - | - | 是 | 0 | 阅读时长（秒） |
| scroll_position | INT | - | - | 是 | 0 | 最后阅读位置（像素） |
| read_percentage | DECIMAL | (5,2) | - | 是 | 0.00 | 阅读进度百分比 |
| last_read_time | DATETIME | - | - | 否 | - | 最后阅读时间 |
| deleted | TINYINT | - | - | 是 | 0 | 逻辑删除：0-未删除，1-已删除 |
| create_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

---

### 1.7 reading_progress（阅读进度表）

**用途**: 记录用户阅读文章的实时进度，用于实现跨设备进度同步和"继续阅读"功能。用户与文章为一对一关系。

| 字段名 | 数据类型 | 长度 | 主键 | 允许为空 | 默认值 | 字段描述 |
|--------|----------|------|------|----------|--------|----------|
| id | BIGINT | - | 是 | 否 | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | - | - | 否 | - | 用户ID，关联 `sys_user.id` |
| article_id | BIGINT | - | - | 否 | - | 文章ID，关联 `article.id` |
| scroll_position | INT | - | - | 是 | 0 | 滚动位置（像素） |
| read_percentage | DECIMAL | (5,2) | - | 是 | 0.00 | 阅读进度百分比 |
| last_read_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 最后阅读时间 |
| deleted | TINYINT | - | - | 是 | 0 | 逻辑删除：0-未删除，1-已删除 |
| create_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

---

### 1.8 bookmark（书签表）

**用途**: 存储用户对文章添加的书签，记录书签位置和用户备注。

| 字段名 | 数据类型 | 长度 | 主键 | 允许为空 | 默认值 | 字段描述 |
|--------|----------|------|------|----------|--------|----------|
| id | BIGINT | - | 是 | 否 | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | - | - | 否 | - | 用户ID，关联 `sys_user.id` |
| article_id | BIGINT | - | - | 否 | - | 文章ID，关联 `article.id` |
| position | INT | - | - | 是 | 0 | 书签位置（像素） |
| note | VARCHAR | 500 | - | 是 | NULL | 用户备注 |
| deleted | TINYINT | - | - | 是 | 0 | 逻辑删除：0-未删除，1-已删除 |
| create_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

---

### 1.9 tags（标签管理表）

**用途**: 统一管理系统中的标签字典，支持标签分类、颜色和排序。

| 字段名 | 数据类型 | 长度 | 主键 | 允许为空 | 默认值 | 字段描述 |
|--------|----------|------|------|----------|--------|----------|
| id | BIGINT | - | 是 | 否 | AUTO_INCREMENT | 主键ID |
| name | VARCHAR | 50 | - | 否 | - | 标签名称 |
| category | VARCHAR | 50 | - | 是 | theme | 分类：work_type-作品类型，theme-主题，character-角色/CP，other-其他 |
| color | VARCHAR | 20 | - | 是 | #409eff | 标签颜色（十六进制色值） |
| sort_order | INT | - | - | 是 | 0 | 排序值（值越小越靠前） |
| status | TINYINT | - | - | 是 | 1 | 状态：0-禁用，1-启用 |
| deleted | TINYINT | - | - | 是 | 0 | 逻辑删除：0-未删除，1-已删除 |
| create_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

---

### 1.10 user_feedback（用户反馈表）

**用途**: 存储用户提交的反馈和意见，支持管理员回复处理。

| 字段名 | 数据类型 | 长度 | 主键 | 允许为空 | 默认值 | 字段描述 |
|--------|----------|------|------|----------|--------|----------|
| id | BIGINT | - | 是 | 否 | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | - | - | 否 | - | 反馈用户ID，关联 `sys_user.id` |
| type | TINYINT | - | - | 否 | - | 反馈类型 |
| content | VARCHAR | 1000 | - | 否 | - | 反馈内容 |
| contact | VARCHAR | 100 | - | 是 | NULL | 联系方式 |
| status | TINYINT | - | - | 是 | 0 | 处理状态：0-未处理，1-已处理 |
| reply | VARCHAR | 500 | - | 是 | NULL | 管理员回复内容 |
| deleted | TINYINT | - | - | 是 | 0 | 逻辑删除：0-未删除，1-已删除 |
| create_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | - | 是 | CURRENT_TIMESTAMP | 更新时间（自动更新） |

---

## 二、实体关系说明

### 2.1 核心业务实体关系图

```
sys_user (用户)
    │
    ├── 1:N ──── article (文章)           — 用户作为作者发布多篇文章
    │                                     — 通过 author_id 外键关联
    │
    ├── 1:N ──── comment (评论)           — 用户发表多条评论
    │                                     — 通过 user_id 外键关联
    │
    ├── 1:N ──── interaction (互动)       — 用户对多篇文章产生互动
    │                                     — 通过 user_id 外键关联
    │
    ├── 1:N ──── reading_history (历史)   — 用户有多条阅读记录
    │                                     — 通过 user_id 外键关联
    │
    ├── 1:N ──── reading_progress (进度)  — 用户有多条阅读进度
    │                                     — 通过 user_id 外键关联
    │
    ├── 1:N ──── bookmark (书签)          — 用户有多个书签
    │                                     — 通过 user_id 外键关联
    │
    ├── 1:1 ──── creator_application (申请) — 用户只有一条创作者申请
    │                                     — 通过 user_id 唯一外键关联
    │
    └── 1:N ──── user_feedback (反馈)     — 用户可提交多条反馈
                                          — 通过 user_id 外键关联

article (文章)
    │
    ├── 1:N ──── comment (评论)           — 一篇文章有多条评论
    │                                     — 通过 article_id 外键关联
    │
    ├── 1:N ──── interaction (互动)       — 一篇文章被多次互动
    │                                     — 通过 article_id 外键关联
    │
    ├── 1:N ──── reading_history (历史)   — 文章被多人阅读
    │                                     — 通过 article_id 外键关联
    │
    ├── 1:N ──── reading_progress (进度)  — 文章被多人记录进度
    │                                     — 通过 article_id 外键关联
    │
    └── 1:N ──── bookmark (书签)          — 文章被多人添加书签
                                          — 通过 article_id 外键关联
```

### 2.2 各关系详细说明

#### 用户 ↔ 文章（1:N）
- **关系类型**: 一对多
- **实现方式**: `article.author_id` → `sys_user.id`
- **业务含义**: 一个创作者可以发布多篇文章；每篇文章只属于一个作者。

#### 用户 ↔ 评论（1:N）
- **关系类型**: 一对多
- **实现方式**: `comment.user_id` → `sys_user.id`（评论者）
- **附加关系**: `comment.reply_user_id` → `sys_user.id`（被回复者）
- **业务含义**: 一个用户可以发表多条评论；评论支持多层级回复（通过 `parent_id` 实现楼中楼）。

#### 用户 ↔ 互动（1:N）
- **关系类型**: 一对多
- **实现方式**: `interaction.user_id` → `sys_user.id`
- **业务含义**: 用户可以对多篇文章进行点赞或收藏，同一用户对同一文章的同类型互动唯一（联合唯一约束）。

#### 用户 ↔ 创作者申请（1:1）
- **关系类型**: 一对一
- **实现方式**: `creator_application.user_id` → `sys_user.id`，通过 `UNIQUE KEY uk_user_id` 保证唯一
- **业务含义**: 每个用户只能提交一条创作者申请记录。

#### 用户 ↔ 阅读历史（1:N）
- **关系类型**: 一对多
- **实现方式**: `reading_history.user_id` → `sys_user.id`
- **附加约束**: `(user_id, article_id)` 联合唯一索引，同一用户对同一文章只保留一条阅读历史记录
- **业务含义**: 追踪用户阅读行为，记录每篇文章的阅读时长和进度。

#### 用户 ↔ 阅读进度（1:N）
- **关系类型**: 一对多
- **实现方式**: `reading_progress.user_id` → `sys_user.id`
- **附加约束**: `(user_id, article_id)` 联合唯一索引
- **业务含义**: 实时追踪用户对每篇文章的阅读进度，支持跨设备同步。

#### 用户 ↔ 书签（1:N）
- **关系类型**: 一对多
- **实现方式**: `bookmark.user_id` → `sys_user.id`
- **业务含义**: 用户可以在多篇文章中添加书签，方便标记重要阅读位置。

#### 用户 ↔ 反馈（1:N）
- **关系类型**: 一对多
- **实现方式**: `user_feedback.user_id` → `sys_user.id`
- **业务含义**: 用户可以向平台提交多条反馈或意见。

#### 文章 ↔ 评论（1:N）
- **关系类型**: 一对多
- **实现方式**: `comment.article_id` → `article.id`
- **业务含义**: 一篇文章可以有多条评论，支持多层级回复。

#### 文章 ↔ 互动（1:N）
- **关系类型**: 一对多
- **实现方式**: `interaction.article_id` → `article.id`
- **业务含义**: 一篇文章可以被多个用户点赞或收藏。

#### 文章 ↔ 标签（N:N，非严格外键）
- **关系类型**: 多对多（松散关联）
- **实现方式**: 通过 `article.tags` 字段存储逗号分隔的标签名称，标签名称对应 `tags.name`。无外键约束，属弹性设计。
- **业务含义**: 一篇文章可以有多个标签，一个标签可被多篇文章使用。

---

## 三、表索引信息

### 3.1 sys_user（用户表）

| 索引名称 | 索引类型 | 涉及字段 | 用途说明 |
|----------|----------|----------|----------|
| PRIMARY | 主键索引 | id | 聚簇索引，唯一标识每行记录 |
| uk_username | 唯一索引 | username | 保证用户名唯一，加速登录查询 |
| idx_status | 普通索引 | status | 加速按用户状态筛选查询 |
| idx_roles | 普通索引 | roles | 加速按角色查询 |
| idx_creator_status | 普通索引 | creator_status | 加速按创作者状态筛选 |

### 3.2 article（文章表）

| 索引名称 | 索引类型 | 涉及字段 | 用途说明 |
|----------|----------|----------|----------|
| PRIMARY | 主键索引 | id | 聚簇索引，唯一标识每行记录 |
| idx_author_id | 普通索引 | author_id | 加速按作者查询文章列表 |
| idx_status | 普通索引 | status | 加速按文章状态筛选（草稿/已发布/审核中） |
| idx_create_time | 普通索引 | create_time | 加速按发布时间排序和范围查询 |
| idx_like_count | 普通索引 | like_count | 加速按热度排序（热门文章排行） |
| idx_reviewer_id | 普通索引 | reviewer_id | 加速按审核人查询审核记录 |
| ft_title_content | 全文索引 | title, content | 支持文章标题和内容的全文搜索（MATCH...AGAINST） |

### 3.3 comment（评论表）

| 索引名称 | 索引类型 | 涉及字段 | 用途说明 |
|----------|----------|----------|----------|
| PRIMARY | 主键索引 | id | 聚簇索引，唯一标识每行记录 |
| idx_article_id | 普通索引 | article_id | 加速按文章查询评论列表 |
| idx_user_id | 普通索引 | user_id | 加速按用户查询评论记录 |
| idx_parent_id | 普通索引 | parent_id | 加速查询楼中楼回复（子评论查询） |
| idx_create_time | 普通索引 | create_time | 加速按评论时间排序 |

### 3.4 interaction（互动表）

| 索引名称 | 索引类型 | 涉及字段 | 用途说明 |
|----------|----------|----------|----------|
| PRIMARY | 主键索引 | id | 聚簇索引，唯一标识每行记录 |
| uk_user_article_type | 唯一索引 | user_id, article_id, type | 防止重复互动（同一用户对同一文章的同一类型互动唯一） |
| idx_user_id | 普通索引 | user_id | 加速按用户查询互动记录 |
| idx_article_id | 普通索引 | article_id | 加速按文章查询互动统计 |
| idx_type | 普通索引 | type | 加速按互动类型筛选（点赞/收藏） |

### 3.5 creator_application（创作者申请表）

| 索引名称 | 索引类型 | 涉及字段 | 用途说明 |
|----------|----------|----------|----------|
| PRIMARY | 主键索引 | id | 聚簇索引，唯一标识每行记录 |
| uk_user_id | 唯一索引 | user_id | 保证一个用户只有一条申请记录 |
| idx_status | 普通索引 | status | 加速按审核状态筛选 |
| idx_create_time | 普通索引 | create_time | 加速按申请时间排序 |

### 3.6 reading_history（阅读历史记录表）

| 索引名称 | 索引类型 | 涉及字段 | 用途说明 |
|----------|----------|----------|----------|
| PRIMARY | 主键索引 | id | 聚簇索引，唯一标识每行记录 |
| idx_user_id | 普通索引 | user_id | 加速按用户查询阅读历史 |
| idx_article_id | 普通索引 | article_id | 加速按文章查询被阅读情况 |
| idx_last_read_time | 普通索引 | last_read_time | 加速按阅读时间排序 |
| uk_user_article | 唯一索引 | user_id, article_id | 同一用户对同一文章保留一条记录，支持 UPSERT 更新 |

### 3.7 reading_progress（阅读进度表）

| 索引名称 | 索引类型 | 涉及字段 | 用途说明 |
|----------|----------|----------|----------|
| PRIMARY | 主键索引 | id | 聚簇索引，唯一标识每行记录 |
| uk_user_article | 唯一索引 | user_id, article_id | 同一用户对同一文章只有一条进度记录 |
| idx_user_id | 普通索引 | user_id | 加速按用户查询阅读进度 |
| idx_article_id | 普通索引 | article_id | 加速按文章查询阅读统计 |

### 3.8 bookmark（书签表）

| 索引名称 | 索引类型 | 涉及字段 | 用途说明 |
|----------|----------|----------|----------|
| PRIMARY | 主键索引 | id | 聚簇索引，唯一标识每行记录 |
| uk_user_article | 唯一索引 | user_id, article_id | 同一用户对同一文章只有一条书签记录 |
| idx_user_id | 普通索引 | user_id | 加速按用户查询书签列表 |
| idx_article_id | 普通索引 | article_id | 加速按文章查询书签统计 |

### 3.9 tags（标签管理表）

| 索引名称 | 索引类型 | 涉及字段 | 用途说明 |
|----------|----------|----------|----------|
| PRIMARY | 主键索引 | id | 聚簇索引，唯一标识每行记录 |
| uk_name | 唯一索引 | name | 保证标签名称唯一 |
| idx_status | 普通索引 | status | 加速按启用状态筛选 |
| idx_category | 普通索引 | category | 加速按标签分类筛选 |

### 3.10 user_feedback（用户反馈表）

| 索引名称 | 索引类型 | 涉及字段 | 用途说明 |
|----------|----------|----------|----------|
| PRIMARY | 主键索引 | id | 聚簇索引，唯一标识每行记录 |
| idx_user_id | 普通索引 | user_id | 加速按用户查询反馈记录 |
| idx_status | 普通索引 | status | 加速按处理状态筛选 |
| idx_create_time | 普通索引 | create_time | 加速按提交时间排序 |

---

## 附录：通用约定

1. **逻辑删除**: 所有表均使用 `deleted` 字段（0-未删除，1-已删除）实现逻辑删除，MyBatis-Plus 的 `@TableLogic` 注解自动处理查询和删除逻辑。
2. **时间字段**: `create_time` 和 `update_time` 由数据库自动维护（`DEFAULT CURRENT_TIMESTAMP` / `ON UPDATE CURRENT_TIMESTAMP`），部分表通过 MyBatis-Plus 的 `FieldFill` 策略在应用层填充。
3. **主键策略**: 所有表主键均使用 `BIGINT AUTO_INCREMENT`，对应 MyBatis-Plus 的 `IdType.AUTO` 策略。
4. **字符集**: 所有表统一使用 `utf8mb4` 字符集，支持 emoji 和特殊 Unicode 字符。
5. **存储引擎**: 所有表统一使用 `InnoDB`，支持事务和外键约束。
6. **审计字段**: `reviewer_id` 和 `review_comment` 用于审核场景（文章审核、创作者申请审核），记录审核人和审核意见。
