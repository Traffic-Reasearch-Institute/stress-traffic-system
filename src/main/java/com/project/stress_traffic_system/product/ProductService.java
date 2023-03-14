package com.project.stress_traffic_system.product;

import com.project.stress_traffic_system.members.entity.Members;
import com.project.stress_traffic_system.product.model.*;
import com.project.stress_traffic_system.product.model.dto.*;
import com.project.stress_traffic_system.product.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    /*private final ProductElasticRepository productElasticRepository;
    private final ProductQueryRepository productQueryRepository;*/

    private final ReviewRepository reviewRepository;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductRedisService productRedisService;
    private final int PAGE_SIZE = 100;
    private final String SORT_BY = "date";


    //전체상품 가져오기
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProducts(int page) {

        //모든 상품 100개 단위로 가져오기
        return productRepository.findAllOrderByClickCountDesc(page);

    }

    //상품 id로 상세정보 가져오기 (레디스에서 먼저 조회하기)
    @Transactional
    public ProductResponseDto getProduct(Long productId) throws IOException {

        Optional<ProductResponseDto> redisProduct = productRedisService.getProduct(productId);
        if (redisProduct.isEmpty()) {
            log.info("Redis에 상품정보 없음 -> RDS에서 검색 실행");
            Product product = findProduct(productId);

            //조회수 늘리기
            addClickCount(productId);

            return ProductResponseDto.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .description(product.getDescription())
                    .shippingFee(product.getShippingFee())
                    .imgurl(product.getImgurl())
                    .clickCount(product.getClickCount())
                    .orderCount(product.getOrderCount())
                    .stock(product.getStock())
                    .introduction(product.getIntroduction())
                    .pages(product.getPages())
                    .date(product.getDate())
                    .build();
        }

        ProductResponseDto responseDto = redisProduct.get();

        //레디스에 저장된 조회수를 검색해본다
        Long clickCount = productRedisService.getClickCount(productId);

        // 저장된 조회수가 없으면 dto에서 1을 더한 값을 가져온다
        if (clickCount == -1L) {
            responseDto.setClickCount(responseDto.getClickCount() + 1);
        }
        else{
            //저장된 조회수가 있으면 해당 값 +1 을 dto에 세팅해준다
            responseDto.setClickCount(clickCount + 1);
        }

        // 해당 상품 id의 조회수를 증가시켜서 레디스와 ElasticSearch 두곳에 저장한다
        // 레디스에 저장된 조회수는 주기적으로 RDS에 업데이트한다
        addClickCount(productId);

        return responseDto;
    }

        //todo Elasticsearch 검색 추후 검토 및 수정 요망

    //like 검색해오기
    @Transactional(readOnly = true)
    public List<ProductResponseDto> searchProductsByLike(String keyword) {
        return productRepository.findByKeyword(keyword);
    }

    //full text 검색해오기
    @Transactional(readOnly = true)
    public List<ProductResponseDto> searchProductsByFull(String keyword) {
        return productRepository.findByFullKeyword(keyword);
    }

    //redis 검색해오기
    @Transactional(readOnly = true)
    public List<ProductResponseDto> searchProductsByRedis(String keyword) {
        return productRedisService.searchProductsByRedis(keyword);
    }

    //redis 검색해오기 - cache aside
    public List<ProductResponseDto> searchProductsByRedisCacheAside(String keyword) {
        List<ProductResponseDto> list = productRedisService.searchProductsByRedisCacheAside(keyword);

        //redis에 값이 없으면 db에서 직접 찾아와서 redis에 저장한다
        if (list.size() == 0) {
            List<ProductResponseDto> dbList = productRepository.findByFullKeyword(keyword);
            productRedisService.cacheProductsCacheAside(dbList);
            log.info("db에서 찾아온 검색어 사이즈 = {}", dbList.size());
            return dbList;
        }
        return list;
    }

    //캐싱 키워드 20가지로 검색하기
    public List<ProductResponseDto> searchCacheKeyword(String keyword) {
        Set<ZSetOperations.TypedTuple<ProductResponseDto>> products =  productRedisService.searchCacheKeyword(keyword);

        if (products.size() == 0) {
            throw new IllegalArgumentException("등록되지 않은 키워드입니다");
        }
        return products.stream().map(ProductResponseDto::convertToResponseRankingDto).collect(Collectors.toList());
    }
/*
    // 상품 검색하기 (이름, 가격 필터링)
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(ProductSearchCondition condition) {

        //조회수 내림차순으로 정렬한다
        Sort sort = Sort.by(Sort.Direction.DESC, "click_count");
        Pageable pageable = PageRequest.of(condition.getPage(), PAGE_SIZE, sort);

        //Elasticsearch에서 검색 조건에 맞는 ProductDoc 리스트를 찾아온다
//        List<ProductDoc> list = productQueryRepository.findByCondition(condition, pageable);
//        List<ProductDoc> list = productElasticRepository.findTopByNameAndPriceBetween(condition.getName(), condition.getPriceFrom(), condition.getPriceTo());
        Page<ProductDoc> list = productElasticRepository.findTopByNameAndPriceBetween(condition.getName(), condition.getPriceFrom(), condition.getPriceTo(), pageable);

//        log.info("Elasticsearch에서 찾아온 상품 검색 list size = {}", list.size());

        //ProductDoc를 Dto리스트로 변환한다
        List<ProductResponseDto> collect = list.stream().map(product -> ProductResponseDto.builder()
                .id(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .shippingFee(product.getShippingFee())
                .imgurl(product.getImgurl())
                .clickCount(product.getClickCount() + 1)
                .orderCount(product.getOrderCount())
                .stock(product.getStock())
                .introduction(product.getIntroduction())
                .pages(product.getPages())
                .date(convertDate(product.getDate()))
                .build()).collect(Collectors.toList());

        return new PageImpl<>(collect);

        // RDS에서 직접 검색해서 가져온다
//        return productRepository.searchProducts(condition);
    }
*/

      //  카테고리 1~5 각각 조회하는 Api
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchByCategory(Long categoryId, int page) {

        Optional<SubCategory> subCategory = subCategoryRepository.findById(categoryId);

        if (subCategory.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 카테고리 입니다");
        }
        return productRepository.searchByCategory(categoryId, page);
    }


/*
        카테고리(대분류) API - 국내도서, 해외도서, E-Book
     */


    //카테고리별 상품리스트 가져오기
    // Redis에서 조회하고 없을 경우에는 DB 조회하기
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findByMainCategory(String categoryName, int page) {
        Set<ZSetOperations.TypedTuple<ProductResponseDto>> products = productRedisService.findProductsByCategory(convertCategoryName(categoryName) + "::", page);

        log.info("Redis에서 조회한 products 사이즈는 = {}", products.size());

        if (products.size() == 0) {
            Category category = categoryRepository.findByCategoryName(categoryName).orElseThrow(
                    () -> new IllegalArgumentException("등록되지 않은 카테고리 입니다"));

            if (categoryName.equals("best")) {
                return productRepository.findBestSeller(page);
            }
            return productRepository.findByMainCategory(category, page);
        }

        return products.stream().map(ProductResponseDto::convertToResponseRankingDto).collect(Collectors.toList());

    }

    //리뷰 등록하기
    @Transactional
    public ReviewResponseDto createReview(Members member, Long productId, ReviewRequestDto requestDto) {

        log.info("requestDto.getContent = {}", requestDto.getContent());

        //리뷰를 등록할 상품 찾아오기
        Product product = findProduct(productId);

        Review review = new Review(product, member, requestDto.getContent());

        Review savedReview = reviewRepository.save(review);
        return ReviewResponseDto.builder()
                .id(savedReview.getId())
                .content(savedReview.getContent())
                .username(savedReview.getMember().getUsername())
                .createdAt(savedReview.getCreatedAt())
                .build();
    }

    //리뷰 목록 가져오기
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviews(Long productId) {
        Product product = findProduct(productId);

        List<Review> reviewList = reviewRepository.findAllByProduct(product);

        return reviewList.stream().map(review ->
                        ReviewResponseDto.builder()
                                .id(review.getId())
                                .content(review.getContent())
                                .username(review.getMember().getUsername())
                                .createdAt(review.getCreatedAt())
                                .build())
                .collect(Collectors.toList());
    }

    //상품 id로 상품 찾아오기
    private Product findProduct(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다"));
    }


/*
        캐싱
     */


    //카테고리id로 국내도서, 해외도서, EBook 캐싱하기(조휘수 상위 1만개)
//    @Scheduled(cron = "0 0 0 * * *") //밤 12시마다 실행
    @Transactional
    public void cacheProducts() {
        List<ProductResponseDto> domesticProducts = productRepository.findByMainCategory(1L);
        productRedisService.cacheProducts(domesticProducts, "domestic");

        List<ProductResponseDto> foreignProducts = productRepository.findByMainCategory(2L);
        productRedisService.cacheProducts(foreignProducts, "foreign");

        List<ProductResponseDto> EBookProducts = productRepository.findByMainCategory(3L);
        productRedisService.cacheProducts(EBookProducts, "ebook");

        List<ProductResponseDto> BestSellers = productRepository.findBestSeller();
        productRedisService.cacheProducts(BestSellers, "best");
    }

    //테스트용 캐싱
    @Transactional
    public void TestCacheProducts() {
        List<ProductResponseDto> domesticProducts = productRepository.TestFindByMainCategory(1L);
        productRedisService.cacheProducts(domesticProducts, "domestic");
    }

    //상품 상세페이지 상위 1만건 캐싱하기
    @Scheduled(cron = "0 0 0 * * *") //밤 12시마다 실행
    @Transactional
    public void cacheProductsDetail() {
        List<ProductResponseDto> list = productRepository.findProductDetail();
        productRedisService.cacheProductsDetail(list);
    }

    // 키워드별로 조회수 상위 1000건 캐싱
    @Transactional
    public void cacheProductsByKeyword() {

        String[] keywords = new String[]{"Federic", "Levi", "Victor", "Robbie", "Jeffery", "Isaac", "Monika", "Jade", "Harber", "Matthew",
                "Gayle", "Ami", "Paris", "Shenna", "Celia", "Ted", "Elicia", "Debora", "Coy", "Violette"};

        //키워드 20가지 캐싱
        for (String keyword : keywords) {
            List<ProductResponseDto> products = productRepository.findByFullKeyword(keyword);
            productRedisService.cacheProductsByKeyword(products, keyword);
        }
    }

    // 테스트용 100건 캐싱
    @Transactional
    public void TestCacheProductsByKeyword() {

        String[] keywords = new String[]{"Robbie"};

        //키워드 20가지 캐싱
        for (String keyword : keywords) {
            List<ProductResponseDto> products = productRepository.findByFullKeyword(keyword);
            productRedisService.cacheProductsByKeyword(products, keyword);
        }
    }

    //레디스 테스트용 캐시
    public void TestCacheProduct() {
        List<ProductResponseDto> list = new ArrayList<>();
        ProductResponseDto productResponseDto = new ProductResponseDto();
        productResponseDto.setName("test");
        list.add(productResponseDto);
        productRedisService.testCacheProduct(list);
    }

    //상품이름 검색을 위한 상위 1000건 캐싱
    @Scheduled(cron = "0 0 0 * * *") //밤 12시마다 실행
    public void cacheProductsTop1000() {
        List<ProductResponseDto> list = productRepository.findTop1000();
        productRedisService.cacheProductsTop1000(list);
    }

    //한글 대분류 이름을 영어로 변환
    private String convertCategoryName(String categoryName) {
        switch (categoryName) {
            case "국내도서":
                return "domestic";
            case "해외도서":
                return "foreign";
            case "e-book":
                return "ebook";
            case "best":
                return "best";
        }
        return categoryName;
    }

    // 상품 조회수 추가로직
    public void addClickCount(Long productId) throws IOException {
        String key = "clickCount::" + productId;
        log.info("조회수 증가 메서드 실행");
        productRedisService.addClickCount(key, productId);
    }

    //Elasticsearch에서 date가 String 타입이므로 변환해준다
    public LocalDateTime convertDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(date, formatter);
    }
}

