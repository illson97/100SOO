package spring.soo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.soo.domain.ArticleComment;

public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
}
