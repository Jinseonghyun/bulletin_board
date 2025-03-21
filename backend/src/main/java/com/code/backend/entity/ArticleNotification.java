package com.code.backend.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ArticleNotification {
    private String type = "write_article";
    private Long articleId;
    private Long userId;
}
