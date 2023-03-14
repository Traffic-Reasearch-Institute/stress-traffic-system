package com.project.stress_traffic_system.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.stress_traffic_system.cart.service.CartService;
import com.project.stress_traffic_system.config.WebSecurityConfig;
import com.project.stress_traffic_system.product.controller.ProductController;
import com.project.stress_traffic_system.product.model.dto.ProductResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*전체적인 flow를 확인하는 통합테스트가 아닌 직접 서비스를 만드는 단위테스트로 진행*/
//Spring Security와의 의존성을 제거 ->컨트롤러의 테스트만 하기 위함
@WebMvcTest(controllers = ProductController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class, // 추가
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class)})
public class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CartService cartService;

    @MockBean
    ProductService productService;

    ProductResponseDto redisProductDto = ProductResponseDto.builder().id(1L).imgurl(20).name("Robbie").build();
    List<ProductResponseDto> contents = new ArrayList<>();

    @BeforeEach
    void beforeEach(){
        contents.add(redisProductDto);
    }

    @Test
    @DisplayName("상품전체조회")
    public void getProducts() throws Exception{
        //given
        Page<ProductResponseDto> productResponseDtos = new PageImpl<>(contents);
        when(productService.getProducts(any(Integer.class))).thenReturn(productResponseDtos);

        // when & then
        mockMvc.perform(get("/api/products?page=0"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("상품상세조회")
    public void getProduct() throws Exception{
        //given
        when(productService.getProduct(any(Long.class))).thenReturn(redisProductDto);

        // when & then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Like 검색")
    public void searchProductsByLike() throws Exception{
        //given
        when(productService.searchProductsByLike(any())).thenReturn(contents);

        // when & then
        mockMvc.perform(get("/api/products/search/like/test"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("full text 검색")
    public void searchProductsByFull() throws Exception{
        //given
        when(productService.searchProductsByLike(any())).thenReturn(contents);

        // when & then
        mockMvc.perform(get("/api/products/search/full/test"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("redis read though 검색")
    public void searchProductsByRedis() throws Exception{
        //given
        when(productService.searchProductsByLike(any())).thenReturn(contents);

        // when & then
        mockMvc.perform(get("/api/products/search/redis/test"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("redis 검색 - cache aside")
    public void searchProductsByRedisCacheAside() throws Exception{
        //given
        when(productService.searchProductsByLike(any())).thenReturn(contents);

        // when & then
        mockMvc.perform(get("/api/products/search/redis/cacheaside/test"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("카테고리1 상품조회")
    public void searchByCategory1() throws Exception{
        //given
        Page<ProductResponseDto> productResponseDtos = new PageImpl<>(contents);
        when(productService.searchByCategory(any(Long.class),any(Integer.class))).thenReturn(productResponseDtos);

        // when & then
        mockMvc.perform(get("/api/products/category-1?page=0"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("카테고리2 상품조회")
    public void searchByCategory2() throws Exception{
        //given
        Page<ProductResponseDto> productResponseDtos = new PageImpl<>(contents);
        when(productService.searchByCategory(any(Long.class),any(Integer.class))).thenReturn(productResponseDtos);

        // when & then
        mockMvc.perform(get("/api/products/category-2?page=0"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("카테고리3 상품조회")
    public void searchByCategory3() throws Exception{
        //given
        Page<ProductResponseDto> productResponseDtos = new PageImpl<>(contents);
        when(productService.searchByCategory(any(Long.class),any(Integer.class))).thenReturn(productResponseDtos);

        // when & then
        mockMvc.perform(get("/api/products/category-3?page=0"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("카테고리4 상품조회")
    public void searchByCategory4() throws Exception{
        //given
        Page<ProductResponseDto> productResponseDtos = new PageImpl<>(contents);
        when(productService.searchByCategory(any(Long.class),any(Integer.class))).thenReturn(productResponseDtos);

        // when & then
        mockMvc.perform(get("/api/products/category-4?page=0"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("카테고리5 상품조회")
    public void searchByCategory5() throws Exception{
        //given
        Page<ProductResponseDto> productResponseDtos = new PageImpl<>(contents);
        when(productService.searchByCategory(any(Long.class),any(Integer.class))).thenReturn(productResponseDtos);

        // when & then
        mockMvc.perform(get("/api/products/category-5?page=0"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("국내도서 상품조회")
    public void findDomesticProducts() throws Exception{
        //given
        when(productService.findByMainCategory(any(String.class),any(Integer.class))).thenReturn(contents);

        // when & then
        mockMvc.perform(get("/api/products/domestic?page=0"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("해외도서 상품조회")
    public void findForeignProducts() throws Exception{
        //given
        when(productService.findByMainCategory(any(String.class),any(Integer.class))).thenReturn(contents);

        // when & then
        mockMvc.perform(get("/api/products/foreign?page=0"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("ebook 상품조회")
    public void findEbookProducts() throws Exception{
        //given
        when(productService.findByMainCategory(any(String.class),any(Integer.class))).thenReturn(contents);

        // when & then
        mockMvc.perform(get("/api/products/ebook?page=0"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("베스트셀러 상품조회")
    public void findBestsellerProducts() throws Exception{
        //given
        when(productService.findByMainCategory(any(String.class),any(Integer.class))).thenReturn(contents);

        // when & then
        mockMvc.perform(get("/api/products/bestseller?page=0"))
                .andExpect(status().is2xxSuccessful());
    }

//    @Test
//    @DisplayName("상품 리뷰 등록하기")
//    public void createReview() throws Exception{
//        //given
//        ReviewRequestDto requestDto = ReviewRequestDto.builder().content("good").build();
//        when(productService.createReview(any(Members.class),any(Long.class),any(ReviewRequestDto.class))).thenReturn(any());
//
//        String content = objectMapper.writeValueAsString(requestDto);
//
//        // when & then
//        mockMvc.perform(post("/api/products/1/review")
//                .content(objectMapper.writeValueAsString(requestDto))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }

    @Test
    @DisplayName("상품 리뷰 조회하기")
    public void getReviews() throws Exception{
        // when & then
        mockMvc.perform(get("/api/products/1/review"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("카테고리별 상품데이터 캐싱")
    public void cacheProducts() throws Exception{
        // when & then
        mockMvc.perform(get("/api/products/cache"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("전체카테고리 상품데이터 1만건 캐싱")
    public void cacheProductsdetail() throws Exception{
        // when & then
        mockMvc.perform(get("/api/products/cache/detail"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("캐싱된 키워드 20가지 검색 API")
    public void searchCacheKeyword() throws Exception{
        // when & then
        mockMvc.perform(get("/api/products/search/cache/1"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("키워드 20가지 상위 1000건 데이터 캐싱")
    public void cacheProductsKeyword() throws Exception{
        // when & then
        mockMvc.perform(get("/api/products/cache/keyword"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("normal 키워드 20가지 검색 API")
    public void cacheProductsNormal() throws Exception{
        // when & then
        mockMvc.perform(get("/api/products/search/normal/1"))
                .andExpect(status().is2xxSuccessful());
    }

}
