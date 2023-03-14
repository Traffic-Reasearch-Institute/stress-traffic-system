package com.project.stress_traffic_system.product;

import com.project.stress_traffic_system.config.TestConfig;
import com.project.stress_traffic_system.members.entity.Members;
import com.project.stress_traffic_system.members.entity.MembersRoleEnum;
import com.project.stress_traffic_system.product.model.Product;
import com.project.stress_traffic_system.product.model.Review;
import com.project.stress_traffic_system.product.model.SubCategory;
import com.project.stress_traffic_system.product.model.dto.ProductResponseDto;
import com.project.stress_traffic_system.product.model.dto.QProductResponseDto;
import com.project.stress_traffic_system.product.model.dto.ReviewRequestDto;
import com.project.stress_traffic_system.product.model.dto.ReviewResponseDto;
import com.project.stress_traffic_system.product.repository.CategoryRepository;
import com.project.stress_traffic_system.product.repository.ProductRepository;
import com.project.stress_traffic_system.product.repository.ReviewRepository;
import com.project.stress_traffic_system.product.repository.SubCategoryRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.project.stress_traffic_system.product.model.QProductFull.productFull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest //레디스 도커를 띄우기 위해 통합테스트로 진행
@ExtendWith(MockitoExtension.class) //Mock객체 생성
@Import(TestConfig.class) //JPAQueryFactory 사용
//@RequiredArgsConstructor
public class ProductServiceTest {

    //TestConfig에 em과 JPAQueryFactory를 Bean으로 등록
    @Autowired private JPAQueryFactory queryFactory;
    //실제 캐시를 불러올 RedisTemplate
    @Autowired private RedisTemplate<String, ProductResponseDto> AutowiredProductRedisTemplate;
    @Autowired private RedisTemplate<String, String> AutowiredClickCountRedisTemplate;
    @Autowired ProductRedisService AutowiredProductRedisService;
    @Autowired ProductService AutowiredProductService;
    //ProductService에 필요한 생성자 매개변수
    @Autowired private ProductRepository AutowiredproductRepository;
    @Autowired private ReviewRepository AutowiredreviewRepository;
    @Autowired private CategoryRepository AutowiredcategoryRepository;
    @Autowired private SubCategoryRepository AutowiredsubCategoryRepository;

    @CreatedDate private LocalDateTime date;

    //ProductService에 필요한 생성자 매개변수
    @Mock private ProductRepository productRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SubCategoryRepository subCategoryRepository;

    //ProductRedisService에 필요한 생성자 매개변수
    @Mock private RedisTemplate<String, ProductResponseDto> productRedisTemplate;
    @Mock private RedisTemplate<String, String> clickCountRedisTemplate;
    @Mock private RedisTemplate<String, ProductResponseDto> searchCountRedisTemplate;

    //productRedisService.getProduct(productId) 에 사용하는 키&밸류
    @Mock ValueOperations<String, ProductResponseDto> ProductValueOperation;
    //productRedisService.getClickCount(productId) 에 사용하는 키&밸류
    @Mock ValueOperations<String, String> ClickValueOperation;

    //Mock Service
    @InjectMocks
    ProductRedisService productRedisService;

    @InjectMocks
    ProductService productService;

    //테스트에 반복되는 변수를 전역변수로 선언
    Integer page = 0;
    ProductResponseDto redisProductDto = ProductResponseDto.builder().id(1L).imgurl(20).name("Robbie").build();


    //테스트를 실행하기 전마다 Service 생성
    @BeforeEach
    void beforeEach(){
        AutowiredProductRedisService = new ProductRedisService(productRepository,AutowiredProductRedisTemplate,AutowiredClickCountRedisTemplate);
        AutowiredProductService = new ProductService(AutowiredreviewRepository,AutowiredproductRepository,AutowiredcategoryRepository,AutowiredsubCategoryRepository,AutowiredProductRedisService);
        productRedisService = new ProductRedisService(productRepository,productRedisTemplate,clickCountRedisTemplate);
        productService = new ProductService(reviewRepository,productRepository,categoryRepository,subCategoryRepository,productRedisService);
    }

    @Nested
    @DisplayName("성공 케이스")
    class Success {

        @DisplayName("전체상품 가져오기 테스트")
        @Test
        void getProducts() {
            //given

            //getProducts 수행시 반환할 new PageImpl<>생성
            List<ProductResponseDto> content = queryFactory
                    .select(new QProductResponseDto(
                            productFull.id,
                            productFull.name,
                            productFull.price,
                            productFull.description,
                            productFull.shippingFee,
                            productFull.imgurl,
                            productFull.clickCount,
                            productFull.orderCount,
                            productFull.stock,
                            productFull.introduction,
                            productFull.pages,
                            productFull.date
                    ))
                    .from(productFull)
                    .orderBy(productFull.clickCount.desc())
                    .offset(page)
                    .limit(10) // 10개만 불러온다.
                    .fetch();

            Page<ProductResponseDto> productResponseDtos = new PageImpl<>(content);

            //ProductService 에 필요한 리포지토리를 모방해서 구현
            when(productRepository.findAllOrderByClickCountDesc(any(Integer.class))).thenReturn(productResponseDtos);

            //when
            Page<ProductResponseDto> productResponse = productService.getProducts(page);

            //then
            Assertions.assertEquals(10, productResponse.getTotalElements());
        }


        @DisplayName("getProduct(productId) 메서드 테스트 - 상품정보 있음")
        @Test
        void getProduct() throws IOException {
            //given
            //productRedisService.getProduct()에 필요한 key&value 커스텀
            when(productRedisTemplate.opsForValue()).thenReturn(ProductValueOperation);
            when(ProductValueOperation.get(any())).thenReturn(redisProductDto); //전역변수 값
            //productRedisService.addClickCount()에 필요한 key&value 커스텀
            when(clickCountRedisTemplate.opsForValue()).thenReturn(ClickValueOperation);
            when(ClickValueOperation.get(any())).thenReturn("10"); //clickCount 10 리턴

            //when
            ProductResponseDto productResponse = productService.getProduct(1L);

            //then
            Assertions.assertEquals(productResponse.getClickCount().longValue(), 11L);
            Assertions.assertEquals(redisProductDto, productResponse);
        }

        @DisplayName("getProduct(productId) 메서드 테스트 - 상품정보 없음")
        @Test
        void getProduct2() throws IOException {
            //given
            redisProductDto = ProductResponseDto.builder()
                    .id(1L).name("Victor").price(1000)
                    .description("des").shippingFee(2500)
                    .imgurl(30).clickCount(100L)
                    .orderCount(100L).stock(100)
                    .introduction("int").pages(100)
                    .date(date).build();

            Product product = new Product(redisProductDto);
            when(productRedisTemplate.opsForValue()).thenReturn(ProductValueOperation);
            when(ProductValueOperation.get(any())).thenReturn(null);
            when(productRepository.findById(any())).thenReturn(Optional.of(product));
            //productRedisService.addClickCount()에 필요한 key&value 커스텀
            when(clickCountRedisTemplate.opsForValue()).thenReturn(ClickValueOperation);
            when(ClickValueOperation.get(any())).thenReturn("100"); //clickCount 10 리턴

            //when
            ProductResponseDto productResponse = productService.getProduct(1L);

            //then
            Assertions.assertEquals(productResponse.getClickCount().longValue(), product.getClickCount());
            Assertions.assertEquals(product.getName(), productResponse.getName());
        }

        @DisplayName("Like 검색")
        @Test
        void searchProductsByLike() {
            //given

            List<ProductResponseDto> content = queryFactory
                    .select(new QProductResponseDto(
                            productFull.id,
                            productFull.name,
                            productFull.price,
                            productFull.description,
                            productFull.shippingFee,
                            productFull.imgurl,
                            productFull.clickCount,
                            productFull.orderCount,
                            productFull.stock,
                            productFull.introduction,
                            productFull.pages,
                            productFull.date
                    ))
                    .from(productFull)
                    .where(productFull.name.contains("Robbie"))
                    .orderBy(productFull.clickCount.desc())
                    .limit(5)
                    .fetch();

            //ProductService 에 필요한 리포지토리를 따라서 구현
            when(productRepository.findByKeyword(any(String.class))).thenReturn(content);

            //when
            //"Robbie" 키워드가 있는지 검색
            List<ProductResponseDto> productResponse = productService.searchProductsByLike("Robbie");

            //then
            //첫번째 리스트의 이르에 "Robbie"가 있는지 확인
            Assertions.assertTrue(productResponse.get(0).getName().contains("Robbie"));
            Assertions.assertEquals(5, productResponse.size());
        }

        @DisplayName("Full Text 검색")
        @Test
        void searchProductsByFull() {
            //given
            BooleanBuilder builder = new BooleanBuilder();
            NumberTemplate booleanTemplate = Expressions.numberTemplate(Double.class,
                    "function('match',{0},{1})", productFull.name, "Robbie" + "*");
            builder.and(booleanTemplate.gt(0));

            List<ProductResponseDto> content = queryFactory
                    .select(new QProductResponseDto(
                            productFull.id,
                            productFull.name,
                            productFull.price,
                            productFull.description,
                            productFull.shippingFee,
                            productFull.imgurl,
                            productFull.clickCount,
                            productFull.orderCount,
                            productFull.stock,
                            productFull.introduction,
                            productFull.pages,
                            productFull.date
                    ))
                    .from(productFull)
                    .where(builder)
                    .orderBy(productFull.clickCount.desc())
                    .limit(5)
                    .fetch();

            //ProductService 에 필요한 리포지토리를 모방해서 구현
            when(productRepository.findByFullKeyword(any(String.class))).thenReturn(content);

            //when
            //"Robbie" 키워드가 있는지 검색
            List<ProductResponseDto> productResponse = productService.searchProductsByFull("Robbie");

            //then
            //첫번째 리스트의 이르에 "Robbie"가 있는지 확인
            Assertions.assertTrue(productResponse.get(0).getName().contains("Robbie"));
            Assertions.assertEquals(5, productResponse.size());
        }


        @DisplayName("redis 상품검색")
        @Transactional
        @Test
        void searchProductsByRedis() {
            //given
            //테스트에 사용할 product-name::test 캐싱
            AutowiredProductService.TestCacheProduct();

            //when
            List<ProductResponseDto> productResponse = AutowiredProductService.searchProductsByRedis("test");

            System.out.println("productResponse.get(0).getName()"+productResponse.get(0).getName());
            //then
            Assertions.assertTrue(productResponse.get(0).getName().equals("test"));
        }

        @DisplayName("캐싱 키워드 20가지로 검색하기")
        @Transactional
        @Test
        void searchCacheKeyword() {
            //given
            //도커로 실행한 레디스에 일정 상품 캐싱
            AutowiredProductService.TestCacheProductsByKeyword();

            //Autowired 된 ProductRedisTemplate 사용
            ZSetOperations<String, ProductResponseDto> ZSetOperations = AutowiredProductRedisTemplate.opsForZSet();
            when(productRedisTemplate.opsForZSet()).thenReturn(ZSetOperations);

            //when
            List<ProductResponseDto> productResponse = productService.searchCacheKeyword("Robbie");//검색할 때는 소문자로//

            //then
            Assertions.assertEquals(100, productResponse.size()); //searchCacheKeyword() 에서 100개 값을 조회
        }


        @DisplayName("카테고리 1~5 각각 조회하는 Api")
        @Test
        void searchByCategory() {
            //given
            List<ProductResponseDto> content = queryFactory
                    .select(new QProductResponseDto(
                            productFull.id,
                            productFull.name,
                            productFull.price,
                            productFull.description,
                            productFull.shippingFee,
                            productFull.imgurl,
                            productFull.clickCount,
                            productFull.orderCount,
                            productFull.stock,
                            productFull.introduction,
                            productFull.pages,
                            productFull.date
                    ))
                    .from(productFull)
                    .where(productFull.subCategory.id.eq(1L))
                    .orderBy(productFull.clickCount.desc())
                    .offset(page)
                    .limit(5) //5개의 상품정보를 조회
                    .fetch();

            SubCategory subCategory = new SubCategory();
            subCategory.setId(1L); subCategory.setCategoryName("인문");
            when(subCategoryRepository.findById(any(Long.class))).thenReturn(Optional.of(subCategory));
            when(productRepository.searchByCategory(any(Long.class),any(int.class))).thenReturn(new PageImpl<>(content));

            //when
            Page<ProductResponseDto> productResponse = productService.searchByCategory(1L, 0);

            //then
            Assertions.assertEquals(5, productResponse.getTotalElements()); //5개의 상품정보를 조회
        }

        @DisplayName("카테고리별 상품리스트 가져오기")
        @Test
        void findByMainCategory() {
            //given
            //도커로 실행한 레디스에 카테고리 캐싱
            AutowiredProductService.TestCacheProducts();

            //Autowired 된 ProductRedisTemplate 사용
            ZSetOperations<String, ProductResponseDto> ZSetOperations = AutowiredProductRedisTemplate.opsForZSet();
            when(productRedisTemplate.opsForZSet()).thenReturn(ZSetOperations);

            //when
            List<ProductResponseDto> productResponse = productService.findByMainCategory("국내도서",0);//검색할 때는 소문자로//

            //then
            System.out.println("productResponse.get(0).getName()"+productResponse.get(0).getName());
//            Assertions.assertTrue(productResponse.get(0).getName().contains("Robbie"));
            Assertions.assertEquals(100, productResponse.size()); //searchCacheKeyword() 에서 100개 값을 조회
        }

        @DisplayName("리뷰 등록하기")
        @Test
        void createReview() {
            //given
            Product product = new Product(1L,30,"Victor Donnelly",16000,10,150L);
            Members member = new Members(1L,"user","1234","test@com","nickname",MembersRoleEnum.MEMBER);
            ReviewRequestDto reviewRequestDto = ReviewRequestDto.builder().content("It's cool").build();
            Review review = new Review(product, member, reviewRequestDto.getContent());

            when(productRepository.findById(any(Long.class))).thenReturn(Optional.of(product));
            when(reviewRepository.save(any(Review.class))).thenReturn(review);

            //when
            ReviewResponseDto reviewResponseDto = productService.createReview(member,1L,reviewRequestDto);

            //then
            Assertions.assertEquals(reviewResponseDto.getContent(),"It's cool");
            Assertions.assertEquals(reviewResponseDto.getUsername(),"user");
            //save가 실행되었는지 확인
            verify(reviewRepository, times(1)).save(any(Review.class));
        }

        @DisplayName("리뷰 목록 가져오기")
        @Test
        void getReviews() {
            //given
            List<Review> reviews = new ArrayList<>();
            Product product = new Product(1L,30,"Victor Donnelly",16000,10,150L);
            Members member = new Members(1L,"user","1234","test@com","nickname",MembersRoleEnum.MEMBER);
            Review review = new Review(product,member,"It's cool");
            reviews.add(review);

            when(productRepository.findById(any(Long.class))).thenReturn(Optional.of(product));
            when(reviewRepository.findAllByProduct(any(Product.class))).thenReturn(reviews);

            //when
            List<ReviewResponseDto> responseDtos = productService.getReviews(1L);//검색할 때는 소문자로//

            //then
            Assertions.assertEquals(responseDtos.get(0).getContent(),"It's cool");
            Assertions.assertEquals(responseDtos.get(0).getUsername(),"user");
        }

        @DisplayName("상품 상세페이지 조회를 위한 조회수 상위 1만건 캐싱")
        @Test
        @Transactional
        void cacheProductsDetail() {
            //when
            AutowiredProductService.cacheProductsDetail();

            //then
            ScanOptions options = ScanOptions.scanOptions().match("product::*").count(1000).build();
            Cursor<byte[]> keys = AutowiredProductRedisTemplate.getConnectionFactory().getConnection().scan(options);
            int count=0;
            while (keys.hasNext()) {
                keys.next();
                count++;
            }
            Assertions.assertEquals(count,10000);
        }

        @DisplayName("Redis에 있는 상품 조회수를 주기적으로 DB에 업데이트")
        @Test
        @Transactional
        void updateClickCount() {
            //given
            //테스트를 위해 임의값을 저장
            ValueOperations<String, String> valueOperations = AutowiredClickCountRedisTemplate.opsForValue();
            valueOperations.set("clickCount::1","100");

            RedisConnectionFactory connectionFactory = AutowiredClickCountRedisTemplate.getConnectionFactory();
            when(clickCountRedisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
            when(clickCountRedisTemplate.opsForValue()).thenReturn(valueOperations);

            //when
            productRedisService.updateClickCount();

            //then
            verify(productRepository, times(1)).setClickCount(any(Long.class),any(Long.class));
            verify(clickCountRedisTemplate, times(1)).delete(any(String.class));
        }

        @DisplayName("상품이름으로 검색하기 위한 캐싱데이터(1000건)")
        @Test
        @Transactional
        void cacheProductsTop1000() {
            //when
            AutowiredProductService.cacheProductsTop1000();

            //then
            ScanOptions options = ScanOptions.scanOptions().match("product-name::*").count(500).build();
            Cursor<byte[]> keys = AutowiredProductRedisTemplate.getConnectionFactory().getConnection().scan(options);
            int count=0;
            while (keys.hasNext()) {
                keys.next();
                count++;
            }
            Assertions.assertEquals(count,1000);
        }

        @DisplayName("redis 에서 상품이름으로 검색하기 - cache aside")
        @Test
        @Transactional
        void searchProductsByRedisCacheAside() {
            //given
            ValueOperations<String, ProductResponseDto> valueOperations = AutowiredProductRedisTemplate.opsForValue();
            valueOperations.set("product-name-aside::test",redisProductDto);

            //when
            List<ProductResponseDto> responseDtos = AutowiredProductService.searchProductsByRedisCacheAside("test");

            //then
            Assertions.assertEquals(responseDtos.get(0).getName(),"Robbie");
        }

        @DisplayName("키워드별로 조회수 상위 100건 캐싱")
        @Test
        @Transactional
        void cacheProductsByKeyword() {
            //given
            List<ProductResponseDto> responseDtos = new ArrayList<>();
            responseDtos.add(redisProductDto);
            when(productRepository.findByFullKeyword(any())).thenReturn(responseDtos);

            //when
            productService.cacheProductsByKeyword();

            //then
            verify(productRepository, times(20)).findByFullKeyword(any(String.class));
        }

        @DisplayName("카테고리별로(대분류) 상위 1만건 캐싱하기")
        @Test
        @Transactional
        void cacheProducts() {
            //when
            productService.cacheProducts();

            //when
            verify(productRedisTemplate, times(4)).delete(any(String.class));
        }

    }
}