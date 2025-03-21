package com.code.backend.task;

import com.code.backend.entity.Article;
import com.code.backend.entity.HotArticle;
import com.code.backend.repository.ArticleRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Component
public class DailyHotArticleTasks {

    private static final String REDIS_KEY = "hot-article:";

    private final ArticleRepository articleRepository;
    private RedisTemplate<String, Object> redisTemplate;

    public DailyHotArticleTasks(ArticleRepository articleRepository, RedisTemplate<String, Object> redisTemplate) {
        this.articleRepository = articleRepository;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "00 00 00 * * ?")
    public void pickYesterdayHotArticle() {
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Article article = articleRepository.findHotArticle(startDate, endDate);
        if (article == null) {
            return;
        }
        HotArticle hotArticle = new HotArticle();
        hotArticle.setId(article.getId());
        hotArticle.setTitle(article.getTitle());
        hotArticle.setContent(article.getContent());
        hotArticle.setAuthorName(article.getAuthor().getUsername());
        hotArticle.setCreatedDate(article.getCreatedDate());
        hotArticle.setUpdatedDate(article.getUpdatedDate());
        hotArticle.setViewCount(article.getViewCount());
        redisTemplate.opsForHash().put(REDIS_KEY + article.getId(), article.getId(), hotArticle);
    }
}
