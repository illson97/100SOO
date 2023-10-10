package spring.soo.dto.request;

import spring.soo.dto.ArticleDto;
import spring.soo.dto.HashtagDto;
import spring.soo.dto.UserAccountDto;

import java.time.LocalDateTime;
import java.util.Set;

public record ArticleRequest(
        String title,
        String content

) {
    public static ArticleRequest of(String title, String content) {
        return new ArticleRequest(title, content);
    }

    public ArticleDto toDto(UserAccountDto userAccountDto) {
        return toDto(userAccountDto, null);
    }

    public ArticleDto toDto(UserAccountDto userAccountDto, Set<HashtagDto> hashtagDtos) {
        return ArticleDto.of(
                userAccountDto,
                title,
                content,
                hashtagDtos
        );
    }
}
