package spring.soo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.soo.domain.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}