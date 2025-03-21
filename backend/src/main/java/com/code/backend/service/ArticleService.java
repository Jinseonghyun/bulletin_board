package com.code.backend.service;

import com.code.backend.dto.EditArticleDto;
import com.code.backend.dto.WriteArticleDto;
import com.code.backend.entity.Article;
import com.code.backend.pojo.WriteArticle;
import com.code.backend.entity.Board;
import com.code.backend.entity.User;
import com.code.backend.exception.ForbiddenException;
import com.code.backend.exception.RateLimitException;
import com.code.backend.exception.ResourceNotFoundException;
import com.code.backend.repository.ArticleRepository;
import com.code.backend.repository.BoardRepository;
import com.code.backend.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class ArticleService {

    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    private final ElasticSearchService elasticSearchService;
    private final ObjectMapper objectMapper;
    private final RabbitMQSender rabbitMQSender;

    @Autowired
    public ArticleService(BoardRepository boardRepository, ArticleRepository articleRepository, UserRepository userRepository,
                          ElasticSearchService elasticSearchService, ObjectMapper objectMapper, RabbitMQSender rabbitMQSender) {
        this.boardRepository = boardRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.elasticSearchService = elasticSearchService;
        this.objectMapper = objectMapper;
        this.rabbitMQSender = rabbitMQSender;
    }

    @Transactional
    public Article writeArticle(Long boardId, WriteArticleDto dto) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // rate limit 체크
        if (!this.isCanWriteArticle()) {
            throw new RateLimitException("article not written by rate limit");
        }

        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(boardId);

        if (author.isEmpty()) {
            throw new ResourceNotFoundException("author not found");
        }
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }

        Article article = new Article();
        article.setBoard(board.get());
        article.setAuthor(author.get());
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        articleRepository.save(article);
        this.indexArticle(article);
        WriteArticle writeArticle = new WriteArticle();
        writeArticle.setArticleId(article.getId());
        writeArticle.setUserId(author.get().getId());
        rabbitMQSender.send(writeArticle);
        return article;
    }

    public List<Article> firstGetArticle(Long boardId) {
        return articleRepository.findTop10ByBoardIdOrderByCreatedDateDesc(boardId);
    }

    public List<Article> getOldArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(boardId, articleId);
    }

    public List<Article> getNewArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(boardId, articleId);
    }

    @Transactional
    public Article editArticle(Long boardId, Long articleId, EditArticleDto dto) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(boardId);

        if (author.isEmpty()) {
            throw new ResourceNotFoundException("author not found");
        }
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }

        Optional<Article> article = articleRepository.findById(articleId);
        if (article.isEmpty()) {
            throw new ResourceNotFoundException("article not found");
        }

        // authentication check
        if (article.get().getAuthor() != author.get()) {
            throw new ForbiddenException("article author different");
        }

        // rate limit 체크
        if (!this.isCanEditArticle()) {
            throw new RateLimitException("article not edited by rate limit");
        }

        if (dto.getTitle() != null) {
            article.get().setTitle(dto.getTitle().get());
        }
        if (dto.getContent() != null) {
            article.get().setContent(dto.getContent().get());
        }
        articleRepository.save(article.get());
        this.indexArticle(article.get());
        return article.get();
    }

    @Transactional
    public boolean deleteArticle(Long boardId, Long articleId) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(boardId);

        if (author.isEmpty()) {
            throw new ResourceNotFoundException("author not found");
        }
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }
        Optional<Article> article = articleRepository.findById(articleId);
        if (article.isEmpty()) {
            throw new ResourceNotFoundException("article not found");
        }
        if (article.get().getAuthor() != author.get()) {
            throw new ForbiddenException("article author different");
        }
        if (!this.isCanEditArticle()) {
            throw new RateLimitException("article not edited by rate limit");
        }
        article.get().setIsDeleted(true);
        articleRepository.save(article.get());
        this.indexArticle(article.get());
        return true;
    }

    private boolean isCanWriteArticle() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Article latestArticle = articleRepository.findLatestArticleByAuthorUsernameOrderByCreatedDate(userDetails.getUsername());
        if (latestArticle == null) {
            return true;
        }
        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getCreatedDate());
    }

    private boolean isCanEditArticle() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Article latestArticle = articleRepository.findLatestArticleByAuthorUsernameOrderByUpdatedDate(userDetails.getUsername());
        if (latestArticle == null || latestArticle.getUpdatedDate() == null) {
            return true;
        }
        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getUpdatedDate());
    }

    private boolean isDifferenceMoreThanFiveMinutes(LocalDateTime localDateTime) {
        LocalDateTime dateAsLocalDateTime = new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Duration duration = Duration.between(localDateTime, dateAsLocalDateTime);

        return Math.abs(duration.toMinutes()) > 5;
    }

    public String indexArticle(Article article) throws JsonProcessingException {
        String articleJson = objectMapper.writeValueAsString(article);
        return elasticSearchService.indexArticleDocument(article.getId().toString(), articleJson).block();
    }

    public List<Article> searchArticle(String keyword) {
        Mono<List<Long>> articleIds = elasticSearchService.articleSearch(keyword);
        try {
            return articleRepository.findAllById(articleIds.toFuture().get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
