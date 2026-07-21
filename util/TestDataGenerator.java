package com.fanfaction.util;

import com.fanfaction.entity.Article;
import com.fanfaction.entity.Comment;
import com.fanfaction.entity.Interaction;
import com.fanfaction.entity.ReadingHistory;
import com.fanfaction.entity.Bookmark;
import com.fanfaction.entity.ReadingProgress;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.mapper.CommentMapper;
import com.fanfaction.mapper.InteractionMapper;
import com.fanfaction.mapper.ReadingHistoryMapper;
import com.fanfaction.mapper.BookmarkMapper;
import com.fanfaction.mapper.ReadingProgressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 测试数据生成工具类
 * 可以自动生成各种类型的测试数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataGenerator {

    public final ArticleMapper articleMapper;
    public final CommentMapper commentMapper;
    public final InteractionMapper interactionMapper;
    public final ReadingHistoryMapper readingHistoryMapper;
    public final BookmarkMapper bookmarkMapper;
    public final ReadingProgressMapper readingProgressMapper;

    private final Random random = new Random();

    // 预定义的中文文章标题
    private static final String[] ARTICLE_TITLES = {
        "火影同人 - 七代目的日常",
        "火影同人 - 佐助的归途",
        "火影同人 - 卡卡西的秘密",
        "火影同人 - 雏田的勇气",
        "火影同人 - 晓组织的日常",
        "海贼同人 - 路飞的梦想",
        "海贼同人 - 索隆的修行",
        "海贼同人 - 娜美的宝藏",
        "海贼同人 - 山治的爱情",
        "海贼同人 - 乔巴的成长",
        "鬼灭同人 - 炭治郎的温柔",
        "鬼灭同人 - 善逸的勇气",
        "鬼灭同人 - 伊之助的野性",
        "鬼灭同人 - 祢豆子的秘密",
        "鬼灭同人 - 柱的集结",
        "咒术同人 - 虎杖的觉悟",
        "咒术同人 - 五条悟的日常",
        "咒术同人 - 伏黑的成长",
        "同人创作心得 - 如何写好同人小说",
        "2026 年同人作品推荐 TOP10"
    };

    // 预定义的中文文章内容
    private static final String[] ARTICLE_CONTENTS = {
        "鸣人成为七代目火影已经三年了。每天清晨，他都会站在火影大楼的窗前，俯瞰着整个木叶村。今天的阳光格外温暖，就像雏田的笑容一样。火影大人，今天的文件已经批完了。鹿丸抱着一叠文件走了进来。辛苦了鹿丸！鸣人伸了个懒腰，对了，博人和向日葵呢？听说他们在忍者学校惹了些麻烦...鹿丸无奈地笑了笑。",
        "离开木叶多年，佐助终于踏上了归途。站在村口，他看到了熟悉的身影——那个总是喊着要超越他的吊车尾。佐助！你终于回来了！鸣人兴奋地冲了过来。佐助微微一笑：我回来了，鸣人。",
        "旗木卡卡西，这个总是戴着面罩、拿着亲热天堂的神秘男人，他的面罩下究竟隐藏着什么秘密？这一天，第七班决定要揭开老师的真面目...",
        "日向雏田一直是个害羞的女孩，但在鸣人面前，她决定鼓起勇气表白。鸣人君...我...我喜欢你！",
        "如果晓组织没有毁灭，他们的日常会是什么样的？鼬，别总是吃三色丸子了，偶尔也吃点别的吧！迪达拉抱怨道。",
        "我要成为海贼王！路飞站在桑尼号的船头，大声宣告着自己的梦想。草帽团的伙伴们都笑了，因为他们知道，这个男人一定会实现自己的诺言。",
        "为了变得更强，索隆每天都在进行地狱式的修行。鹰眼先生，请继续指导我！",
        "传说中的宝藏到底是什么？娜美看着古老的地图，眼中闪烁着光芒。",
        "海上餐厅巴拉蒂，山治遇到了命中注定的那个人...美丽的小姐，请品尝我为您准备的料理！",
        "从小被遗弃的驯鹿乔巴，在遇到路飞后，终于找到了属于自己的家。",
        "无论面对什么样的鬼，我都不会放弃拯救他们的心。炭治郎温柔地说道。",
        "平时胆小怕事的善逸，在关键时刻却总能爆发出惊人的力量。我...我要保护祢豆子！",
        "哇啊啊！我要成为最强的鬼杀队队员！伊之助挥舞着双刀，充满了野性的力量。",
        "变成鬼的祢豆子，依然保持着人类的感情。她心中的秘密，只有炭治郎知道...",
        "九位柱，鬼杀队最强的剑士们，为了消灭鬼舞辻无惨而集结。",
        "我会拯救所有人！虎杖悠仁坚定地说道，即使面对强大的咒灵，他也毫不退缩。",
        "作为最强的咒术师，五条悟的日常却意外地轻松。今天也要好好享受甜食呢~",
        "伏黑惠一直活在自己的阴影中，但在虎杖的影响下，他逐渐打开了心扉。",
        "作为一名资深同人作者，我经常被问到如何写好同人小说。今天我就来分享一些经验...首先，要尊重原著设定。其次，要塑造鲜明的人物性格。最后，要有创新的情节。",
        "作为一名老书虫，今天给大家推荐 2026 年我最喜欢的十部同人作品...第一名：火影 - 木叶的日常，第二名：海贼 - 新世界的冒险，第三名：鬼灭 - 柱的故事"
    };

    // 预定义的中文摘要
    private static final String[] ARTICLE_SUMMARIES = {
        "七代目火影鸣人的温馨日常故事，与家人、伙伴们的平凡生活。",
        "佐助结束流浪回到木叶，与鸣人重逢的感人故事。",
        "第七班试图揭开卡卡西老师面罩下的秘密。",
        "雏田向鸣人表白的甜蜜故事。",
        "晓组织成员们的搞笑日常。",
        "路飞与草帽团的冒险故事，关于梦想与友情。",
        "索隆跟随鹰眼修行的热血故事。",
        "娜美寻找传说宝藏的冒险故事。",
        "山治的浪漫爱情故事。",
        "乔巴从被遗弃到加入草帽团的感人故事。",
        "炭治郎用温柔感化鬼的故事。",
        "善逸克服恐惧展现勇气的故事。",
        "伊之助的热血战斗故事。",
        "祢豆子作为鬼却保持人性的秘密。",
        "九柱集结准备最终决战的热血故事。",
        "虎杖悠仁为拯救他人而战斗的故事。",
        "五条悟的轻松日常。",
        "伏黑惠打开心扉的成长故事。",
        "同人小说写作技巧分享，帮助新手作者入门。",
        "2026 年优秀同人作品推荐榜单。"
    };

    // 预定义的中文标签
    private static final String[] ARTICLE_TAGS = {
        "火影忍者，同人，日常，温馨",
        "火影忍者，同人，佐鸣，重逢",
        "火影忍者，同人，卡卡西，搞笑",
        "火影忍者，同人，鸣雏，恋爱",
        "火影忍者，同人，晓组织，搞笑",
        "海贼王，同人，冒险，友情",
        "海贼王，同人，索隆，修行",
        "海贼王，同人，娜美，冒险",
        "海贼王，同人，山治，恋爱",
        "海贼王，同人，乔巴，成长",
        "鬼灭之刃，同人，炭治郎，治愈",
        "鬼灭之刃，同人，善逸，勇气",
        "鬼灭之刃，同人，伊之助，热血",
        "鬼灭之刃，同人，祢豆子，秘密",
        "鬼灭之刃，同人，柱，热血",
        "咒术回战，同人，虎杖，热血",
        "咒术回战，同人，五条悟，日常",
        "咒术回战，同人，伏黑，成长",
        "写作，同人，技巧，心得",
        "推荐，同人，书单，盘点"
    };

    // 预定义的中文评论
    private static final String[] COMMENT_CONTENTS = {
        "七代目的日常好温馨啊！鸣人终于实现了自己的梦想！",
        "看到鸣人和雏田的婚后生活，太幸福了！",
        "佐鸣党狂喜！佐助终于回来了！",
        "这两个人的羁绊真的太深了。",
        "卡卡西老师的面罩下到底是什么？好想知道！",
        "第七班太可爱了，哈哈哈！",
        "鸣雏党在此！雏田终于表白了！",
        "好甜好甜，我要被甜死了！",
        "晓组织的日常居然这么搞笑！",
        "鼬神吃三色丸子的样子太可爱了！",
        "路飞一定会成为海贼王的！我们的王！",
        "草帽团的友情太感人了！",
        "索隆大大好帅！我也要像他一样努力！",
        "鹰眼和索隆的师徒情也不错。",
        "娜美姐姐最可爱了！",
        "宝藏到底是什么呢？好期待后续！",
        "山治的料理看起来好好吃！",
        "厨师的爱情故事也很浪漫呢。",
        "乔巴小可爱！我要抱抱！",
        "乔巴能找到家真是太好了。",
        "炭治郎真的太温柔了，好喜欢他！",
        "这样的温柔一定能感化所有人！",
        "善逸虽然胆小，但关键时刻很可靠！",
        "雷之呼吸帅炸了！",
        "猪突猛进！伊之助最帅！",
        "野猪头套下的脸一定很帅！",
        "祢豆子小天使！",
        "虽然是鬼但依然保持着人性，好感人。",
        "九柱集结！燃起来了！",
        "最终决战一定要赢啊！",
        "虎杖悠仁真的是个好人！",
        "这样的主角才值得尊敬！",
        "五条老师最帅！最强咒术师！",
        "甜食控的五条老师好可爱！",
        "伏黑终于打开了心扉！",
        "朋友的力量是无穷的！",
        "写得很实用！收藏了！",
        "感谢分享，我也要开始写同人！",
        "推荐的都是好作品！",
        "已加入书单，慢慢看！"
    };

    // 预定义的书签笔记
    private static final String[] BOOKMARK_NOTES = {
        "鸣人成为火影的场景，好感动！",
        "佐助回来了！佐鸣党胜利！",
        "雏田表白的名场面！",
        "路飞宣告梦想，超热血！",
        "乔巴找到家了，好感人！",
        "炭治郎的温柔打动了我！",
        "九柱集结，燃起来了！",
        "五条老师吃甜品的样子好可爱！",
        "这里写得很精彩！",
        "名场面，标记一下。"
    };

    /**
     * 生成指定数量的文章
     */
    public List<Article> generateArticles(Long authorId, int count) {
        List<Article> articles = new ArrayList<>();
        
        for (int i = 0; i < count && i < ARTICLE_TITLES.length; i++) {
            Article article = new Article();
            article.setAuthorId(authorId);
            article.setTitle(ARTICLE_TITLES[i]);
            article.setContent(ARTICLE_CONTENTS[i]);
            article.setSummary(ARTICLE_SUMMARIES[i]);
            article.setTags(ARTICLE_TAGS[i]);
            article.setCoverImage("https://picsum.photos/seed/article" + i + "/800/600");
            article.setViewCount(random.nextInt(5000) + 100);
            article.setLikeCount(random.nextInt(500) + 10);
            article.setFavoriteCount(random.nextInt(200) + 5);
            article.setCommentCount(random.nextInt(100) + 1);
            article.setStatus(1);
            article.setDeleted(0);
            
            articles.add(article);
        }
        
        return articles;
    }

    /**
     * 生成评论
     */
    public List<Comment> generateComments(Long userId, List<Long> articleIds, int count) {
        List<Comment> comments = new ArrayList<>();
        
        for (int i = 0; i < count && i < COMMENT_CONTENTS.length; i++) {
            Comment comment = new Comment();
            comment.setUserId(userId);
            comment.setArticleId(articleIds.get(random.nextInt(articleIds.size())));
            comment.setParentId(0L);
            comment.setContent(COMMENT_CONTENTS[i]);
            comment.setLikeCount(random.nextInt(50) + 1);
            comment.setStatus(1);
            comment.setDeleted(0);
            comment.setCreateTime(LocalDateTime.now());
            comment.setUpdateTime(LocalDateTime.now());
            
            comments.add(comment);
        }
        
        return comments;
    }

    /**
     * 生成互动数据（点赞、收藏）
     */
    public List<Interaction> generateInteractions(Long userId, List<Long> articleIds) {
        List<Interaction> interactions = new ArrayList<>();
        
        // 点赞所有文章
        for (Long articleId : articleIds) {
            Interaction like = new Interaction();
            like.setUserId(userId);
            like.setArticleId(articleId);
            like.setType(1); // 1-点赞
            like.setCreateTime(LocalDateTime.now());
            interactions.add(like);
        }
        
        // 收藏部分文章
        int favoriteCount = Math.min(articleIds.size() / 2, 10);
        for (int i = 0; i < favoriteCount; i++) {
            Interaction favorite = new Interaction();
            favorite.setUserId(userId);
            favorite.setArticleId(articleIds.get(i));
            favorite.setType(2); // 2-收藏
            favorite.setCreateTime(LocalDateTime.now());
            interactions.add(favorite);
        }
        
        return interactions;
    }

    /**
     * 生成阅读历史
     */
    public List<ReadingHistory> generateReadingHistory(Long userId, List<Long> articleIds) {
        List<ReadingHistory> histories = new ArrayList<>();
        
        for (Long articleId : articleIds) {
            ReadingHistory history = new ReadingHistory();
            history.setUserId(userId);
            history.setArticleId(articleId);
            history.setLastReadTime(LocalDateTime.now().minusDays(random.nextInt(30)));
            history.setReadDuration(random.nextInt(600) + 120); // 2-12 分钟
            history.setDeleted(0);
            histories.add(history);
        }
        
        return histories;
    }

    /**
     * 生成书签
     */
    public List<Bookmark> generateBookmarks(Long userId, List<Long> articleIds) {
        List<Bookmark> bookmarks = new ArrayList<>();
        
        int bookmarkCount = Math.min(BOOKMARK_NOTES.length, Math.min(articleIds.size() / 3, 10));
        for (int i = 0; i < bookmarkCount; i++) {
            Bookmark bookmark = new Bookmark();
            bookmark.setUserId(userId);
            bookmark.setArticleId(articleIds.get(i));
            bookmark.setPosition(random.nextInt(1000) + 100);
            bookmark.setNote(BOOKMARK_NOTES[i]);
            bookmark.setCreateTime(LocalDateTime.now());
            bookmark.setUpdateTime(LocalDateTime.now());
            bookmark.setDeleted(0);
            bookmarks.add(bookmark);
        }
        
        return bookmarks;
    }

    /**
     * 生成阅读进度
     */
    public List<ReadingProgress> generateReadingProgress(Long userId, List<Long> articleIds) {
        List<ReadingProgress> progresses = new ArrayList<>();
        
        for (Long articleId : articleIds) {
            ReadingProgress progress = new ReadingProgress();
            progress.setUserId(userId);
            progress.setArticleId(articleId);
            progress.setScrollPosition(random.nextInt(1000) + 100);
            progress.setReadPercentage((double) (random.nextInt(100) + 1));
            progress.setLastReadTime(LocalDateTime.now().minusHours(random.nextInt(48)));
            progress.setCreateTime(LocalDateTime.now());
            progress.setUpdateTime(LocalDateTime.now());
            progress.setDeleted(0);
            progresses.add(progress);
        }
        
        return progresses;
    }

    /**
     * 保存所有生成的数据
     */
    public void saveAllData(List<Article> articles, List<Comment> comments, 
                           List<Interaction> interactions, List<ReadingHistory> histories,
                           List<Bookmark> bookmarks, List<ReadingProgress> progresses) {
        
        log.info("开始保存测试数据...");
        
        // 保存文章
        for (Article article : articles) {
            articleMapper.insert(article);
        }
        log.info("已保存 {} 篇文章", articles.size());
        
        // 保存评论
        for (Comment comment : comments) {
            commentMapper.insert(comment);
        }
        log.info("已保存 {} 条评论", comments.size());
        
        // 保存互动
        for (Interaction interaction : interactions) {
            interactionMapper.insert(interaction);
        }
        log.info("已保存 {} 条互动数据", interactions.size());
        
        // 保存阅读历史
        for (ReadingHistory history : histories) {
            readingHistoryMapper.insert(history);
        }
        log.info("已保存 {} 条阅读历史", histories.size());
        
        // 保存书签
        for (Bookmark bookmark : bookmarks) {
            bookmarkMapper.insert(bookmark);
        }
        log.info("已保存 {} 个书签", bookmarks.size());
        
        // 保存阅读进度
        for (ReadingProgress progress : progresses) {
            readingProgressMapper.insert(progress);
        }
        log.info("已保存 {} 条阅读进度", progresses.size());
        
        log.info("所有测试数据保存完成！");
    }
}
