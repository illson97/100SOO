package spring.soo.repository.querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import spring.soo.domain.Article;

import java.util.Collection;
import java.util.List;

public interface ArticleRepositoryCustom {
    @Deprecated
    List<String> findAllDistinctHashtags();
    Page<Article> findByHashtagNames(Collection<String> hashtagNames, Pageable pageable);

}
