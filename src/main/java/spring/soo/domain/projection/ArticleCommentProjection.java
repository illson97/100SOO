package spring.soo.domain.projection;

import org.springframework.data.rest.core.config.Projection;
import spring.soo.domain.ArticleComment;
import spring.soo.domain.UserAccount;

import java.time.LocalDateTime;

@Projection(name = "withUserAccount", types = ArticleComment.class)
public interface ArticleCommentProjection {
    Long getId();
    UserAccount getUserAccount();
    Long getParentCommentId();
    String getContent();
    LocalDateTime getCreatedAt();
    String getCreatedBy();
    LocalDateTime getModifiedAt();
    String getModifiedBy();
}
