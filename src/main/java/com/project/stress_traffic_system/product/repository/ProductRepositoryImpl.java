package com.project.stress_traffic_system.product.repository;

import com.project.stress_traffic_system.members.entity.Members;
import com.project.stress_traffic_system.members.entity.MembersRoleEnum;
import com.project.stress_traffic_system.product.model.Category;
import com.project.stress_traffic_system.product.model.Product;
import com.project.stress_traffic_system.product.model.dto.ProductResponseDto;
import com.project.stress_traffic_system.product.model.dto.ProductSearchCondition;
import com.project.stress_traffic_system.product.model.dto.QProductResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.project.stress_traffic_system.product.model.QProduct.product;

@Slf4j
public class ProductRepositoryImpl implements ProductRepositoryCustom{

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JPAQueryFactory queryFactory;
    private final Integer PAGE_LIMIT = 100;

    private ProductRepositoryImpl(EntityManager em, NamedParameterJdbcTemplate jdbcTemplate) {
        queryFactory = new JPAQueryFactory(em);
        namedParameterJdbcTemplate = jdbcTemplate;
    }

    //Product bulk 데이터 입력하기
    @Override
    public void bulkInsert() {
        String path = "E:\\프로그래밍\\항해99\\5. final\\stress-traffic-system\\product.csv"; //csv 경로
        FileReader in = null;
        BufferedReader bufIn = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Product> products = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {

            in = new FileReader(path);
            bufIn = new BufferedReader(in);
//            bufIn.readLine(); // 컬럼명은 저장되지 않도록 한 줄 읽기

            String data;
            do {//파일에서 데이터를 읽어 파싱하고 Product 객체로 만들어 ArrayList에 넣는다.
                data = bufIn.readLine();  //한 라인 읽기
                if (data != null) {
                    String[] productInfo = data.split(",");  //콤마로 분리하기
                    Product product = new Product();   //Product 객체 생성하기

                    if (data != null) {
//                        product.setCategory_id(productInfo[0].isEmpty() ? 1 : Long.parseLong(productInfo[0]));  //todo category
                        product.setName(productInfo[1].isEmpty() ? "" : productInfo[1]);
                        product.setPrice(productInfo[2].isEmpty() ? 10000 : Integer.parseInt(productInfo[2]));
                        product.setDescription(productInfo[3].isEmpty() ? "" : productInfo[3]);
                        product.setShippingFee(productInfo[4].isEmpty() ? 2500 : Integer.parseInt(productInfo[4]));
                        product.setImgurl(productInfo[5].isEmpty() ? 1 : Integer.parseInt(productInfo[5]));
                        product.setClickCount(productInfo[6].isEmpty() ? 0 : Long.parseLong(productInfo[6]));
                        product.setStock(productInfo[7].isEmpty() ? 100 : Integer.parseInt(productInfo[7]));
                        product.setIntroduction(productInfo[8].isEmpty() ? "" : productInfo[8]);
                        product.setPages(productInfo[9].isEmpty() ? 200 : Integer.parseInt(productInfo[9]));
                        product.setDate(LocalDateTime.now());
                    }
                    products.add(product);
                }
            } while (data != null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }  finally {
            try {
                in.close();
            } catch (Exception e) {
            }
            try {
                bufIn.close();
            } catch (Exception e) {
            }
        }
        stopWatch.stop();

        //jdbc batchUpdate 실행
        String sql = String.format("INSERT INTO Product (name, price, description, shipping_fee, imgurl, click_count, stock, introduction, pages, category_id, date) " +
                "VALUES (:name, :price, :description, :shippingFee, :imgurl, :clickCount, :stock, :introduction, :pages, :category_id, :date)", "Product");

        SqlParameterSource[] params = products.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        StopWatch queryStopwatch = new StopWatch();
        queryStopwatch.start();
        namedParameterJdbcTemplate.batchUpdate(sql, params);
        queryStopwatch.stop();

        log.info("products 사이즈는 = {}", products.size());
        log.info("객체 생성 시간 = {}", stopWatch.getTotalTimeSeconds());
        log.info("쿼리 실행 시간 = {}", queryStopwatch.getTotalTimeSeconds());
    }

    //Product bulk 데이터 입력하기
    public void bulkInsertMembers() {
        String path = "E:\\프로그래밍\\항해99\\5. final\\stress-traffic-system\\user.csv"; //csv 경로
        FileReader in = null;
        BufferedReader bufIn = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Members> members = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {

            in = new FileReader(path);
            bufIn = new BufferedReader(in);
//            bufIn.readLine(); // 컬럼명은 저장되지 않도록 한 줄 읽기

            String data;
            do {//파일에서 데이터를 읽어 파싱하고 Product 객체로 만들어 ArrayList에 넣는다.
                data = bufIn.readLine();  //한 라인 읽기
                if (data != null) {
                    String[] userInfo = data.split(",");  //콤마로 분리하기
                    Members member = new Members();   //Product 객체 생성하기

                    if (data != null) {
                        member.setUsername(userInfo[0].isEmpty() ? UUID.randomUUID().toString() : userInfo[0]);
                        member.setPassword("$2a$10$8aYgCESquxcDpVSMTGHGEOawngL3UncAMpBwcux.Zr4WbpClUYerG");
                        member.setRole(MembersRoleEnum.MEMBER);
                    }
                    members.add(member);
                }
            } while (data != null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }  finally {
            try {
                in.close();
            } catch (Exception e) {
            }
            try {
                bufIn.close();
            } catch (Exception e) {
            }
        }
        stopWatch.stop();

//        jdbc batchUpdate 실행
        String sql = String.format("INSERT INTO users (username, password, role, address) " +
                "VALUES (:username, :password, 'MEMBER', :address)", "Members");

        SqlParameterSource[] params = members.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        StopWatch queryStopwatch = new StopWatch();
        queryStopwatch.start();
        namedParameterJdbcTemplate.batchUpdate(sql, params);
        queryStopwatch.stop();

        log.info("members 사이즈는 = {}", members.size());
        log.info("객체 생성 시간 = {}", stopWatch.getTotalTimeSeconds());
        log.info("쿼리 실행 시간 = {}", queryStopwatch.getTotalTimeSeconds());
    }

    // 상품 조건에 따라 검색(이름, 가격) LIKE 구문 사용
    /*@Override
    public Page<ProductResponseDto> searchProducts(ProductSearchCondition condition) {
        //TODO 검색어가 공백으로 넘어왔을경우 전체조회가 이뤄지기때문에 content를 비워서 내보내야 한다.
        List<ProductResponseDto> content = new ArrayList<>();
        if (!condition.getName().trim().equals("")) {
            content = queryFactory
                    .select(new QProductResponseDto(
                            product.id,
                            product.name,
                            product.price,
                            product.description,
                            product.shippingFee,
                            product.imgurl,
                            product.clickCount,
                            product.stock,
                            product.introduction,
                            product.pages,
                            product.date
                    ))
                    .from(product)
                    .where(nameLike(condition.getName()),
                            priceFrom(condition.getPriceFrom()),
                            priceTo(condition.getPriceTo()))
                    .orderBy(product.clickCount.desc())
                    .offset(condition.getPage())
                    .limit(PAGE_LIMIT)
                    .fetch();
        }
        return new PageImpl<>(content);
    }*/

    // 상품 조건에 따라 검색(이름, 가격) FULL TEXT INDEX 사용
    @Override
    public Page<ProductResponseDto> searchProducts(ProductSearchCondition condition) {
        //TODO 검색어가 공백으로 넘어왔을경우 전체조회가 이뤄지기때문에 content를 비워서 내보내야 한다.
        List<ProductResponseDto> content = new ArrayList<>();
        if (!condition.getName().trim().equals("")) {
            BooleanBuilder builder = new BooleanBuilder();
            NumberTemplate booleanTemplate = Expressions.numberTemplate(Double.class,
                    "function('match',{0},{1})",product.name, condition.getName() + "*");
            builder.and(booleanTemplate.gt(0));
            content = queryFactory
                    .select(new QProductResponseDto(
                            product.id,
                            product.name,
                            product.price,
                            product.description,
                            product.shippingFee,
                            product.imgurl,
                            product.clickCount,
                            product.orderCount,
                            product.stock,
                            product.introduction,
                            product.pages,
                            product.date
                    ))
                    .from(product)
                    .where(builder,priceFrom(condition.getPriceFrom()),
                            priceTo(condition.getPriceTo()))
                    .orderBy(product.clickCount.desc())
                    .offset(condition.getPage())
                    .limit(PAGE_LIMIT)
                    .fetch();
        }
        return new PageImpl<>(content);
    }

    @Override
    public Page<ProductResponseDto> findAllOrderByClickCountDesc(int page) {
        List<ProductResponseDto> content = queryFactory
                .select(new QProductResponseDto(
                        product.id,
                        product.name,
                        product.price,
                        product.description,
                        product.shippingFee,
                        product.imgurl,
                        product.clickCount,
                        product.orderCount,
                        product.stock,
                        product.introduction,
                        product.pages,
                        product.date
                ))
                .from(product)
                .orderBy(product.clickCount.desc())
                .offset(page)
                .limit(PAGE_LIMIT)
                .fetch();

        return new PageImpl<>(content);
    }

    // 소분류로 조회하기
    @Override
    public Page<ProductResponseDto> searchByCategory(Long categoryId, int page) {
        List<ProductResponseDto> content = queryFactory
                .select(new QProductResponseDto(
                        product.id,
                        product.name,
                        product.price,
                        product.description,
                        product.shippingFee,
                        product.imgurl,
                        product.clickCount,
                        product.orderCount,
                        product.stock,
                        product.introduction,
                        product.pages,
                        product.date
                ))
                .from(product)
                .where(product.subCategory.id.eq(categoryId))
                .orderBy(product.clickCount.desc())
                .offset(page)
                .limit(PAGE_LIMIT)
                .fetch();

        return new PageImpl<>(content);

    }

    //대분류로 조회하기
    @Override
    public List<ProductResponseDto> findByMainCategory(Category category, int page) {
        return queryFactory
                .select(new QProductResponseDto(
                        product.id,
                        product.name,
                        product.price,
                        product.description,
                        product.shippingFee,
                        product.imgurl,
                        product.clickCount,
                        product.orderCount,
                        product.stock,
                        product.introduction,
                        product.pages,
                        product.date
                ))
                .from(product)
                .where(product.category.eq(category))
                .orderBy(product.clickCount.desc())
                .offset(page)
                .limit(PAGE_LIMIT)
                .fetch();

    }

    //베스트셀러 검색
    @Override
    public List<ProductResponseDto> findBestSeller(int page) {
        return queryFactory
                .select(new QProductResponseDto(
                        product.id,
                        product.name,
                        product.price,
                        product.description,
                        product.shippingFee,
                        product.imgurl,
                        product.clickCount,
                        product.orderCount,
                        product.stock,
                        product.introduction,
                        product.pages,
                        product.date
                ))
                .from(product)
                .orderBy(product.orderCount.desc(), product.clickCount.desc())
                .offset(page)
                .limit(PAGE_LIMIT)
                .fetch();
    }

    // 베스트셀러 상위 1만건 가져오기 - 캐싱용도
    @Override
    public List<ProductResponseDto> findBestSeller() {
        return queryFactory
                .select(new QProductResponseDto(
                        product.id,
                        product.name,
                        product.price,
                        product.description,
                        product.shippingFee,
                        product.imgurl,
                        product.clickCount,
                        product.orderCount,
                        product.stock,
                        product.introduction,
                        product.pages,
                        product.date
                ))
                .from(product)
                .orderBy(product.orderCount.desc(), product.clickCount.desc())
                .limit(10000)
                .fetch();

    }

    //카테고리 id로 상품상세정보 가져오기 - 캐싱용도
    @Override
    public List<ProductResponseDto> findByMainCategory(Long categoryId) {
        return queryFactory
                .select(new QProductResponseDto(
                        product.id,
                        product.name,
                        product.price,
                        product.description,
                        product.shippingFee,
                        product.imgurl,
                        product.clickCount,
                        product.orderCount,
                        product.stock,
                        product.introduction,
                        product.pages,
                        product.date
                ))
                .from(product)
                .where(product.category.id.eq(categoryId))
                .orderBy(product.clickCount.desc())
                .limit(10000)
                .fetch();
    }

    @Override
    public List<ProductResponseDto> findProductDetail() {
        return queryFactory
                .select(new QProductResponseDto(
                        product.id,
                        product.name,
                        product.price,
                        product.description,
                        product.shippingFee,
                        product.imgurl,
                        product.clickCount,
                        product.orderCount,
                        product.stock,
                        product.introduction,
                        product.pages,
                        product.date
                ))
                .from(product)
                .orderBy(product.clickCount.desc())
                .limit(10000)
                .fetch();
    }

    // 상품 조회수 Return
    @Override
    public Long getClickCount(Long productId) {
        return queryFactory.select(product.clickCount)
                .from(product)
                .where(product.id.eq(productId))
                .fetchOne();
    }

    //상품 조회수 변경 로직
    @Override
    public void setClickCount(Long productId, long clickCount) {
        queryFactory
                .update(product)
                .set(product.clickCount, clickCount)
                .where(product.id.eq(productId))
                .execute();
    }

    // 키워드별로 조회수 상위 1000건 조회 - like 검색
    @Override
    public List<ProductResponseDto> findByKeyword(String keyword) {
        return queryFactory
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
                .where(productFull.name.contains(keyword))
                .orderBy(productFull.clickCount.desc())
                .fetch();
    }

    //full text 검색
    @Override
    public List<ProductResponseDto> findByFullKeyword(String keyword) {
            BooleanBuilder builder = new BooleanBuilder();
            NumberTemplate booleanTemplate = Expressions.numberTemplate(Double.class,
                    "function('match',{0},{1})", productFull.name, keyword + "*");
            builder.and(booleanTemplate.gt(0));

            return queryFactory
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
                    .fetch();
    }

    //상위 1000건 검색
    @Override
    public List<ProductResponseDto> findTop1000() {
        return queryFactory
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
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(String name) {
        return StringUtils.isEmpty(name) ? null : product.name.contains(name);
    }

    private BooleanExpression priceFrom(Integer priceFrom) {
        return priceFrom == null ? null : product.price.goe(priceFrom);
    }

    private BooleanExpression priceTo(Integer priceTo) {
        return priceTo == null ? null : product.price.loe(priceTo);
    }
}
