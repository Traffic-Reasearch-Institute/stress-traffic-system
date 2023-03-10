package com.project.stress_traffic_system.product.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

import static com.project.stress_traffic_system.product.model.QProductFull.productFull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest //????????? ????????? ????????? ?????? ?????????????????? ??????
@ExtendWith(MockitoExtension.class) //Mock?????? ??????
@Import(TestConfig.class) //JPAQueryFactory ??????
public class ProductServiceTest {

    //TestConfig??? em??? JPAQueryFactory??? Bean?????? ??????
    @Autowired private JPAQueryFactory queryFactory;
    //?????? ????????? ????????? RedisTemplate
    @Autowired private RedisTemplate<String, ProductResponseDto> AutowiredProductRedisTemplate;

    //ProductService??? ????????? ????????? ????????????
    @Mock private ProductRepository productRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SubCategoryRepository subCategoryRepository;

    //ProductRedisService??? ????????? ????????? ????????????
    @Mock private RedisTemplate<String, ProductResponseDto> productRedisTemplate;
    @Mock private RedisTemplate<String, String> clickCountRedisTemplate;
    @Mock private RedisTemplate<String, ProductResponseDto> searchCountRedisTemplate;

    //productRedisService.getProduct(productId) ??? ???????????? ???&??????
    @Mock ValueOperations<String, ProductResponseDto> ProductValueOperation;
    //productRedisService.getClickCount(productId) ??? ???????????? ???&??????
    @Mock ValueOperations<String, String> ClickValueOperation;

    //Mock Service
    @InjectMocks
    ProductRedisService productRedisService;

    @InjectMocks
    ProductService productService;

    @Autowired
    ProductService AutowiredProductService;

    //???????????? ???????????? ????????? ??????????????? ??????
    Integer page = 0;
    ProductResponseDto redisProductDto = ProductResponseDto.builder().id(1L).imgurl(20).name("Robbie").build();


    //???????????? ???????????? ????????? Mock Service ??????
    @BeforeEach
    void beforeEach(){

        productRedisService = new ProductRedisService(productRepository,productRedisTemplate,clickCountRedisTemplate);
        productService = new ProductService(reviewRepository,productRepository,categoryRepository,subCategoryRepository,productRedisService);
    }

    @Nested
    @DisplayName("?????? ?????????")
    class Success {

        @DisplayName("???????????? ???????????? ?????????")
        @Test
        void getProducts() {
            //given

            //getProducts ????????? ????????? new PageImpl<>??????
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
                    .limit(10) // 10?????? ????????????.
                    .fetch();

            Page<ProductResponseDto> productResponseDtos = new PageImpl<>(content);

            //ProductService ??? ????????? ?????????????????? ???????????? ??????
            when(productRepository.findAllOrderByClickCountDesc(any(Integer.class))).thenReturn(productResponseDtos);

            //when
            Page<ProductResponseDto> productResponse = productService.getProducts(page);

            //then
            Assertions.assertEquals(10, productResponse.getTotalElements());
        }


        @DisplayName("getProduct(productId) ????????? ?????????")
        @Test
        void getProduct() throws IOException {
            //given
            //productRedisService.getProduct()??? ????????? key&value ?????????
            when(productRedisTemplate.opsForValue()).thenReturn(ProductValueOperation);
            when(ProductValueOperation.get(any())).thenReturn(redisProductDto); //???????????? ???
            //productRedisService.addClickCount()??? ????????? key&value ?????????
            when(clickCountRedisTemplate.opsForValue()).thenReturn(ClickValueOperation);
            when(ClickValueOperation.get(any())).thenReturn("10"); //clickCount 10 ??????

            //when
            ProductResponseDto productResponse = productService.getProduct(1L);

            //then
            Assertions.assertEquals(productResponse.getClickCount().longValue(), 11L);
            Assertions.assertEquals(redisProductDto, productResponse);
        }


        @DisplayName("Like ??????")
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

            //ProductService ??? ????????? ?????????????????? ????????? ??????
            when(productRepository.findByKeyword(any(String.class))).thenReturn(content);

            //when
            //"Robbie" ???????????? ????????? ??????
            List<ProductResponseDto> productResponse = productService.searchProductsByLike("Robbie");

            //then
            //????????? ???????????? ????????? "Robbie"??? ????????? ??????
            Assertions.assertTrue(productResponse.get(0).getName().contains("Robbie"));
            Assertions.assertEquals(5, productResponse.size());
        }

        @DisplayName("Full Text ??????")
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

            //ProductService ??? ????????? ?????????????????? ???????????? ??????
            when(productRepository.findByFullKeyword(any(String.class))).thenReturn(content);

            //when
            //"Robbie" ???????????? ????????? ??????
            List<ProductResponseDto> productResponse = productService.searchProductsByFull("Robbie");

            //then
            //????????? ???????????? ????????? "Robbie"??? ????????? ??????
            Assertions.assertTrue(productResponse.get(0).getName().contains("Robbie"));
            Assertions.assertEquals(5, productResponse.size());
        }


        @DisplayName("redis ????????????")
        @Test
        void searchProductsByRedis() {
            //given
            //???????????? ????????? product-name::test ??????
            AutowiredProductService.TestCacheProduct();

            //when
            List<ProductResponseDto> productResponse = AutowiredProductService.searchProductsByRedis("test");

            //then
            Assertions.assertTrue(productResponse.get(0).getName().equals("test"));
        }

        @DisplayName("redis ?????? - cache aside / ?????? ????????? ????????? ?????? ??????")
        @Test
        void searchProductsByRedisCacheAside() {
            //given
            //findByFullKeyword()??? ????????? ?????????
            List<ProductResponseDto> content = new ArrayList<>();
            content.add(redisProductDto);

            //searchProductsByRedisCacheAside() ???????????? ?????? ??? ????????? ??????
            when(productRepository.findByFullKeyword("test@#test?!@#")).thenReturn(content);

            //when
            List<ProductResponseDto> productResponse = productService.searchProductsByRedisCacheAside("test@#test?!@#");

            //then
            Assertions.assertTrue(productResponse.get(0).getName().contains("Robbie"));
            Assertions.assertEquals(content.size(), productResponse.size());
        }

        @DisplayName("?????? ????????? 20????????? ????????????")
        @Transactional
        @Test
        void searchCacheKeyword() {
            //given
            //????????? ????????? ???????????? ?????? ?????? ??????
            AutowiredProductService.TestCacheProductsByKeyword();

            //Autowired ??? ProductRedisTemplate ??????
            ZSetOperations<String, ProductResponseDto> ZSetOperations = AutowiredProductRedisTemplate.opsForZSet();
            when(productRedisTemplate.opsForZSet()).thenReturn(ZSetOperations);

            //when
            List<ProductResponseDto> productResponse = productService.searchCacheKeyword("Robbie");//????????? ?????? ????????????//

            //then
            Assertions.assertEquals(100, productResponse.size()); //searchCacheKeyword() ?????? 100??? ?????? ??????
        }


        @DisplayName("???????????? 1~5 ?????? ???????????? Api")
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
                    .limit(5) //5?????? ??????????????? ??????
                    .fetch();

            SubCategory subCategory = new SubCategory();
            subCategory.setId(1L); subCategory.setCategoryName("??????");
            when(subCategoryRepository.findById(any(Long.class))).thenReturn(Optional.of(subCategory));
            when(productRepository.searchByCategory(any(Long.class),any(int.class))).thenReturn(new PageImpl<>(content));

            //when
            Page<ProductResponseDto> productResponse = productService.searchByCategory(1L, 0);

            //then
            Assertions.assertEquals(5, productResponse.getTotalElements()); //5?????? ??????????????? ??????
        }

        @DisplayName("??????????????? ??????????????? ????????????")
        @Test
        void findByMainCategory() {
            //given
            //????????? ????????? ???????????? ???????????? ??????
            AutowiredProductService.TestCacheProducts();

            //Autowired ??? ProductRedisTemplate ??????
            ZSetOperations<String, ProductResponseDto> ZSetOperations = AutowiredProductRedisTemplate.opsForZSet();
            when(productRedisTemplate.opsForZSet()).thenReturn(ZSetOperations);

            //when
            List<ProductResponseDto> productResponse = productService.findByMainCategory("????????????",0);//????????? ?????? ????????????//

            //then
            System.out.println("productResponse.get(0).getName()"+productResponse.get(0).getName());
//            Assertions.assertTrue(productResponse.get(0).getName().contains("Robbie"));
            Assertions.assertEquals(100, productResponse.size()); //searchCacheKeyword() ?????? 100??? ?????? ??????
        }

        @DisplayName("?????? ????????????")
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
            //save??? ?????????????????? ??????
            verify(reviewRepository, times(1)).save(any(Review.class));
        }

        @DisplayName("?????? ?????? ????????????")
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
            List<ReviewResponseDto> responseDtos = productService.getReviews(1L);//????????? ?????? ????????????//

            //then
            Assertions.assertEquals(responseDtos.get(0).getContent(),"It's cool");
            Assertions.assertEquals(responseDtos.get(0).getUsername(),"user");
        }

    }
}