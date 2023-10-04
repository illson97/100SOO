package spring.soo.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link spring.soo.domain.Article}
 */
public record ArticleDto(LocalDateTime createdAt,
                         String createdBy,
                         String title,
                         String content,
                         String hashtag
) {
    public static ArticleDto of(LocalDateTime createdAt, String createdBy, String title, String content, String hashtag) {
        return new ArticleDto(createdAt, createdBy, title, content, hashtag);
    }
}