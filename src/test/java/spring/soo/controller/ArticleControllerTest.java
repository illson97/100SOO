package spring.soo.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import spring.soo.config.SecurityConfig;
import spring.soo.config.TestSecurityConfig;
import spring.soo.domain.constant.FormStatus;
import spring.soo.domain.constant.SearchType;
import spring.soo.dto.ArticleDto;
import spring.soo.dto.ArticleWithCommentsDto;
import spring.soo.dto.UserAccountDto;
import spring.soo.dto.request.ArticleRequest;
import spring.soo.dto.response.ArticleResponse;
import spring.soo.service.ArticleService;
import spring.soo.service.PaginationService;
import spring.soo.util.FormDataEncoder;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("view 컨트롤러 - Article")
@Import({TestSecurityConfig.class, FormDataEncoder.class})
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    private final MockMvc mvc;
    private final FormDataEncoder formDataEncoder;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private PaginationService paginationService;

    public ArticleControllerTest(
            @Autowired MockMvc mvc,
            @Autowired FormDataEncoder formDataEncoder

    ) {
        this.mvc = mvc;
        this.formDataEncoder = formDataEncoder;
    }

    @DisplayName("[view][GET] Article 리스트 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenRequestingArticlesView_thenReturnArticlesView() throws Exception {

        given(articleService.searchArticles(eq(null),eq(null),any(Pageable.class))).willReturn(Page.empty());
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(0, 1, 2, 3, 4));

        mvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attributeExists("paginationBarNumbers"));
        then(articleService).should().searchArticles(eq(null),eq(null),any(Pageable.class));
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @WithMockUser
    @DisplayName("[view][GET] 게시글 페이지 - 정상 호출, 인증된 사용자")
    @Test
    public void givenNothing_whenRequestingArticleView_thenReturnArticleView() throws Exception {

        Long articleId = 1L;
        given(articleService.getArticleWithComments(articleId)).willReturn(createArticleWithCommentsDto());

        mvc.perform(get("/articles/" + articleId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/detail"))
                .andExpect(model().attributeExists("article"))
                .andExpect(model().attributeExists("articleComments"));
        then(articleService).should().getArticleWithComments(articleId);
    }

    @DisplayName("[view][GET] 게시글 리스트 (게시판) 페이지 - 페이징, 정렬 기능")
    @Test
    void givenPagingAndSortingParams_whenSearchingArticlesPage_thenReturnsArticlesPage() throws Exception {
        // Given
        String sortName = "title";
        String direction = "desc";
        int pageNumber = 0;
        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc(sortName)));
        List<Integer> barNumbers = List.of(1, 2, 3, 4, 5);
        given(articleService.searchArticles(null, null, pageable)).willReturn(Page.empty());
        given(paginationService.getPaginationBarNumbers(pageable.getPageNumber(), Page.empty().getTotalPages())).willReturn(barNumbers);

        // When & Then
        mvc.perform(
                        get("/articles")
                                .queryParam("page", String.valueOf(pageNumber))
                                .queryParam("size", String.valueOf(pageSize))
                                .queryParam("sort", sortName + "," + direction)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attribute("paginationBarNumbers", barNumbers));
        then(articleService).should().searchArticles(null, null, pageable);
        then(paginationService).should().getPaginationBarNumbers(pageable.getPageNumber(), Page.empty().getTotalPages());
    }

    @DisplayName("[view][GET] 게시글 페이지 - 인증 없을 땐 로그인 페이지로 이동")
    @Test
    void givenNothing_whenRequestingArticlePage_thenRedirectsToLoginPage() throws Exception {
        // Given
        long articleId = 1L;

        // When & Then
        mvc.perform(get("/articles/" + articleId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        then(articleService).shouldHaveNoInteractions();
        then(articleService).shouldHaveNoInteractions();
    }

    @DisplayName("[view][GET] Article 검색 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenSearchingArticleView_thenReturnSearchingArticleView() throws Exception {

        SearchType searchType = SearchType.TITLE;
        String searchValue = "title";
        given(articleService.searchArticles(eq(searchType),eq(searchValue),any(Pageable.class))).willReturn(Page.empty());
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(0, 1, 2, 3, 4));


        mvc.perform(
                get("/articles")
                        .queryParam("searchType", searchType.name())
                        .queryParam("searchValue", searchValue)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attributeExists("searchTypes"));
        then(articleService).should().searchArticles(eq(searchType),eq(searchValue),any(Pageable.class));
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @DisplayName("[view][GET] Article 해시테그 검색 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenRequestingArticleSearchHashtagView_thenReturnArticleSearchHashtagView() throws Exception {

        List<String> hashtags = List.of("#java", "#spring", "#boot");

        given(articleService.searchArticlesViaHashtag(eq(null), any(Pageable.class))).willReturn(Page.empty());
        given(articleService.getHashtags()).willReturn(hashtags);
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(1, 2, 3, 4, 5));


        mvc.perform(get("/articles/search-hashtag"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search-hashtag"))
                .andExpect(model().attribute("articles", Page.empty()))
                .andExpect(model().attribute("hashtags", hashtags))
                .andExpect(model().attributeExists("paginationBarNumbers"))
                .andExpect(model().attribute("searchType", SearchType.HASHTAG));

        then(articleService).should().searchArticlesViaHashtag(eq(null), any(Pageable.class));
        then(articleService).should().getHashtags();
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @DisplayName("[view][GET] 게시글 해시태그 검색 페이지 - 정상 호출")
    @Test
    public void givenNothing_whenRequestingArticleSearchHashtagView_thenReturnsArticleSearchHashtagView() throws Exception {
        // Given
        List<String> hashtags = List.of("#java", "#spring", "#boot");
        given(articleService.searchArticlesViaHashtag(eq(null), any(Pageable.class))).willReturn(Page.empty());
        given(articleService.getHashtags()).willReturn(hashtags);
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(1, 2, 3, 4, 5));

        // When & Then
        mvc.perform(get("/articles/search-hashtag"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search-hashtag"))
                .andExpect(model().attribute("articles", Page.empty()))
                .andExpect(model().attribute("hashtags", hashtags))
                .andExpect(model().attributeExists("paginationBarNumbers"))
                .andExpect(model().attribute("searchType", SearchType.HASHTAG));
        then(articleService).should().searchArticlesViaHashtag(eq(null), any(Pageable.class));
        then(articleService).should().getHashtags();
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @DisplayName("[view][GET] 게시글 해시태그 검색 페이지 - 정상 호출, 해시태그 입력")
    @Test
    public void givenHashtag_whenRequestingArticleSearchHashtagView_thenReturnsArticleSearchHashtagView() throws Exception {
        // Given
        String hashtag = "#java";
        List<String> hashtags = List.of("#java", "#spring", "#boot");
        given(articleService.searchArticlesViaHashtag(eq(hashtag), any(Pageable.class))).willReturn(Page.empty());
        given(articleService.getHashtags()).willReturn(hashtags);
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(1, 2, 3, 4, 5));

        // When & Then
        mvc.perform(
                        get("/articles/search-hashtag")
                                .queryParam("searchValue", hashtag)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search-hashtag"))
                .andExpect(model().attribute("articles", Page.empty()))
                .andExpect(model().attribute("hashtags", hashtags))
                .andExpect(model().attributeExists("paginationBarNumbers"))
                .andExpect(model().attribute("searchType", SearchType.HASHTAG));
        then(articleService).should().searchArticlesViaHashtag(eq(hashtag), any(Pageable.class));
        then(articleService).should().getHashtags();
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @WithMockUser
    @DisplayName("[view][GET] 새 게시글 작성 페이지")
    @Test
    void givenNothing_whenRequesting_thenReturnsNewArticlePage() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/articles/form"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/form"))
                .andExpect(model().attribute("formStatus", FormStatus.CREATE));
    }

    @WithUserDetails(value = "unoTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][POST] 새 게시글 등록 - 정상 호출")
    @Test
    void givenNewArticleInfo_whenRequesting_thenSavesNewArticle() throws Exception {
        // Given
        ArticleRequest articleRequest = ArticleRequest.of("new title", "new content", "#new");
        willDoNothing().given(articleService).saveArticle(any(ArticleDto.class));

        // When & Then
        mvc.perform(
                        post("/articles/form")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(articleRequest))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles"))
                .andExpect(redirectedUrl("/articles"));
        then(articleService).should().saveArticle(any(ArticleDto.class));
    }
    @DisplayName("[view][GET] 게시글 수정 페이지 - 인증 없을 땐 로그인 페이지로 이동")
    @Test
    void givenNothing_whenRequesting_thenRedirectsToLoginPage() throws Exception {
        // Given
        long articleId = 1L;

        // When & Then
        mvc.perform(get("/articles/" + articleId + "/form"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        then(articleService).shouldHaveNoInteractions();
    }

    @WithMockUser
    @DisplayName("[view][GET] 게시글 수정 페이지 - 정상 호출, 인증된 사용자")
    @Test
    void givenNothing_whenRequesting_thenReturnsUpdatedArticlePage() throws Exception {
        // Given
        long articleId = 1L;
        ArticleDto dto = createArticleDto();
        given(articleService.getArticle(articleId)).willReturn(dto);

        // When & Then
        mvc.perform(get("/articles/" + articleId + "/form"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/form"))
                .andExpect(model().attribute("article", ArticleResponse.from(dto)))
                .andExpect(model().attribute("formStatus", FormStatus.UPDATE));
        then(articleService).should().getArticle(articleId);
    }

    @WithUserDetails(value = "unoTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][POST] 게시글 수정 - 정상 호출")
    @Test
    void givenUpdatedArticleInfo_whenRequesting_thenUpdatesNewArticle() throws Exception {
        // Given
        long articleId = 1L;
        ArticleRequest articleRequest = ArticleRequest.of("new title", "new content", "#new");
        willDoNothing().given(articleService).updateArticle(eq(articleId), any(ArticleDto.class));

        // When & Then
        mvc.perform(
                        post("/articles/" + articleId + "/form")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(articleRequest))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/" + articleId))
                .andExpect(redirectedUrl("/articles/" + articleId));
        then(articleService).should().updateArticle(eq(articleId), any(ArticleDto.class));
    }

    @WithUserDetails(value = "unoTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][POST] 게시글 삭제 - 정상 호출")
    @Test
    void givenArticleIdToDelete_whenRequesting_thenDeletesArticle() throws Exception {
        // Given
        long articleId = 1L;
        String userId = "unoTest";
        willDoNothing().given(articleService).deleteArticle(articleId, userId);

        // When & Then
        mvc.perform(
                        post("/articles/" + articleId + "/delete")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles"))
                .andExpect(redirectedUrl("/articles"));
        then(articleService).should().deleteArticle(articleId, userId);
    }


    private ArticleDto createArticleDto() {
        return ArticleDto.of(
                createUserAccountDto(),
                "title",
                "content",
                "#java"
        );
    }
    private ArticleWithCommentsDto createArticleWithCommentsDto() {
        return ArticleWithCommentsDto.of(
                1L,
                createUserAccountDto(),
                Set.of(),
                "title",
                "content",
                "#java",
                LocalDateTime.now(),
                "illson",
                LocalDateTime.now(),
                "illson"
        );
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "uno",
                "pw",
                "uno@mail.com",
                "Uno",
                "memo",
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno"
        );
    }
}
