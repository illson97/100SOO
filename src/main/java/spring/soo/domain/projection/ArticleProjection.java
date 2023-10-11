package spring.soo.domain.projection;

import org.springframework.data.rest.core.config.Projection;
import spring.soo.domain.Article;
import spring.soo.domain.UserAccount;

import java.time.LocalDateTime;

@Projection(name ="withUserAccount", types = Article.class)
public interface ArticleProjection {
    Long getId();
    UserAccount getUserAccount();
    String getTitle();
    String getContent();
    LocalDateTime getCreatedAt();
    String getCreatedBy();
    LocalDateTime getModifiedAt();
    String getModifiedBy();
}
