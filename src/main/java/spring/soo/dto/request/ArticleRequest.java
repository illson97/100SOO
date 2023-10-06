package spring.soo.dto.request;

import spring.soo.dto.ArticleDto;
import spring.soo.dto.UserAccountDto;

import java.time.LocalDateTime;

public record ArticleRequest(
        String title,
        String content,
        String hashtag
) {
    public static ArticleRequest of(String title, String content, String hashtag) {
        return new ArticleRequest(title, content, hashtag);
    }

    public ArticleDto toDto(UserAccountDto userAccountDto) {
        return ArticleDto.of(
                userAccountDto,
                title,
                content,
                hashtag
        );
    }
}
