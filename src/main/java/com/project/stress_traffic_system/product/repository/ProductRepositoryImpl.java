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
import static com.project.stress_traffic_system.product.model.QProductFull.productFull;

@Slf4j
public class ProductRepositoryImpl implements ProductRepositoryCustom{

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JPAQueryFactory queryFactory;
    private final Integer PAGE_LIMIT = 100;

    private ProductRepositoryImpl(EntityManager em, NamedParameterJdbcTemplate jdbcTemplate) {
        queryFactory = new JPAQueryFactory(em);
        namedParameterJdbcTemplate = jdbcTemplate;
    }

    //Product bulk ????????? ????????????
    @Override
    public void bulkInsert() {
        String path = "E:\\???????????????\\??????99\\5. final\\stress-traffic-system\\product.csv"; //csv ??????
        FileReader in = null;
        BufferedReader bufIn = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Product> products = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {

            in = new FileReader(path);
            bufIn = new BufferedReader(in);
//            bufIn.readLine(); // ???????????? ???????????? ????????? ??? ??? ??????

            String data;
            do {//???????????? ???????????? ?????? ???????????? Product ????????? ????????? ArrayList??? ?????????.
                data = bufIn.readLine();  //??? ?????? ??????
                if (data != null) {
                    String[] productInfo = data.split(",");  //????????? ????????????
                    Product product = new Product();   //Product ?????? ????????????

                    if (data != null) {
                        //product.setSubCategory(productInfo[0].isEmpty() ? 1 : Long.parseLong(productInfo[0]));  //todo category
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

        //jdbc batchUpdate ??????
        String sql = String.format("INSERT INTO Product (name, price, description, shipping_fee, imgurl, click_count, stock, introduction, pages, category_id, date) " +
                "VALUES (:name, :price, :description, :shippingFee, :imgurl, :clickCount, :stock, :introduction, :pages, :category_id, :date)", "Product");

        SqlParameterSource[] params = products.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        StopWatch queryStopwatch = new StopWatch();
        queryStopwatch.start();
        namedParameterJdbcTemplate.batchUpdate(sql, params);
        queryStopwatch.stop();

        log.info("products ???????????? = {}", products.size());
        log.info("?????? ?????? ?????? = {}", stopWatch.getTotalTimeSeconds());
        log.info("?????? ?????? ?????? = {}", queryStopwatch.getTotalTimeSeconds());
    }

    //user bulk ????????? ????????????
    public void bulkInsertMembers() {
        String path = "E:\\???????????????\\??????99\\5. final\\stress-traffic-system\\user.csv"; //csv ??????
        FileReader in = null;
        BufferedReader bufIn = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Members> members = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {

            in = new FileReader(path);
            bufIn = new BufferedReader(in);
//            bufIn.readLine(); // ???????????? ???????????? ????????? ??? ??? ??????

            String data;
            do {//???????????? ???????????? ?????? ???????????? Product ????????? ????????? ArrayList??? ?????????.
                data = bufIn.readLine();  //??? ?????? ??????
                if (data != null) {
                    String[] userInfo = data.split(",");  //????????? ????????????
                    Members member = new Members();   //Product ?????? ????????????

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

//        jdbc batchUpdate ??????
        String sql = String.format("INSERT INTO users (username, password, role, address) " +
                "VALUES (:username, :password, 'MEMBER', :address)", "Members");

        SqlParameterSource[] params = members.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        StopWatch queryStopwatch = new StopWatch();
        queryStopwatch.start();
        namedParameterJdbcTemplate.batchUpdate(sql, params);
        queryStopwatch.stop();

        log.info("members ???????????? = {}", members.size());
        log.info("?????? ?????? ?????? = {}", stopWatch.getTotalTimeSeconds());
        log.info("?????? ?????? ?????? = {}", queryStopwatch.getTotalTimeSeconds());
    }

    // ?????? ????????? ?????? ??????(??????, ??????) LIKE ?????? ??????
    /*@Override
    public Page<ProductResponseDto> searchProducts(ProductSearchCondition condition) {
        //TODO ???????????? ???????????? ?????????????????? ??????????????? ????????????????????? content??? ????????? ???????????? ??????.
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

    // ?????? ????????? ?????? ??????(??????, ??????) FULL TEXT INDEX ??????
    @Override
    public Page<ProductResponseDto> searchProducts(ProductSearchCondition condition) {
        //TODO ???????????? ???????????? ?????????????????? ??????????????? ????????????????????? content??? ????????? ???????????? ??????.
        List<ProductResponseDto> content = new ArrayList<>();
        if (!condition.getName().trim().equals("")) {
            BooleanBuilder builder = new BooleanBuilder();
            NumberTemplate booleanTemplate = Expressions.numberTemplate(Double.class,
                    "function('match',{0},{1})", productFull.name, condition.getName() + "*");
            builder.and(booleanTemplate.gt(0));
            content = queryFactory
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
                    .where(builder,priceFrom(condition.getPriceFrom()),
                            priceTo(condition.getPriceTo()))
                    .orderBy(productFull.clickCount.desc())
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
                .limit(PAGE_LIMIT)
                .fetch();

        return new PageImpl<>(content);
    }

    // ???????????? ????????????
    @Override
    public Page<ProductResponseDto> searchByCategory(Long categoryId, int page) {
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
                .where(productFull.subCategory.id.eq(categoryId))
                .orderBy(productFull.clickCount.desc())
                .offset(page)
                .limit(PAGE_LIMIT)
                .fetch();

        return new PageImpl<>(content);

    }

    //???????????? ????????????
    @Override
    public List<ProductResponseDto> findByMainCategory(Category category, int page) {
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
                .where(productFull.category.eq(category))
                .orderBy(productFull.clickCount.desc())
                .offset(page)
                .limit(PAGE_LIMIT)
                .fetch();

    }

    //??????????????? ??????
    @Override
    public List<ProductResponseDto> findBestSeller(int page) {
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
                .orderBy(productFull.orderCount.desc(), productFull.clickCount.desc())
                .offset(page)
                .limit(PAGE_LIMIT)
                .fetch();
    }

    // ??????????????? ?????? 1?????? ???????????? - ????????????
    @Override
    public List<ProductResponseDto> findBestSeller() {
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
                .orderBy(productFull.orderCount.desc(), productFull.clickCount.desc())
                .limit(10000)
                .fetch();

    }

    //???????????? id??? ?????????????????? ???????????? - ????????????
    @Override
    public List<ProductResponseDto> findByMainCategory(Long categoryId) {
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
                .where(productFull.category.id.eq(categoryId))
                .orderBy(productFull.clickCount.desc())
                .limit(10000)
                .fetch();
    }

    //???????????? id??? ?????????????????? ???????????? - ????????? ????????????
    @Override
    public List<ProductResponseDto> TestFindByMainCategory(Long categoryId) {
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
                .where(productFull.category.id.eq(categoryId))
                .orderBy(productFull.clickCount.desc())
                .limit(100)
                .fetch();
    }

    @Override
    public List<ProductResponseDto> findProductDetail() {
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
                .limit(10000)
                .fetch();
    }

    // ?????? ????????? Return
    @Override
    public Long getClickCount(Long productId) {
        return queryFactory.select(productFull.clickCount)
                .from(productFull)
                .where(productFull.id.eq(productId))
                .fetchOne();
    }

    //?????? ????????? ?????? ??????
    @Override
    public void setClickCount(Long productId, long clickCount) {
        queryFactory
                .update(productFull)
                .set(productFull.clickCount, clickCount)
                .where(productFull.id.eq(productId))
                .execute();
    }

    // ??????????????? ????????? ?????? 1000??? ?????? - like ??????
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

    //full text ??????
    @Override
    public List<ProductResponseDto> findByFullKeyword(String keyword) {
            BooleanBuilder builder = new BooleanBuilder();
            NumberTemplate booleanTemplate = Expressions.numberTemplate(Double.class,
                    "function('match',{0},{1})", product.name, keyword + "*");
            builder.and(booleanTemplate.gt(0));

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
                    .where(builder)
                    .orderBy(product.clickCount.desc())
                    .fetch();
    }

    //?????? 1000??? ??????
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
        return StringUtils.isEmpty(name) ? null : productFull.name.contains(name);
    }

    private BooleanExpression priceFrom(Integer priceFrom) {
        return priceFrom == null ? null : productFull.price.goe(priceFrom);
    }

    private BooleanExpression priceTo(Integer priceTo) {
        return priceTo == null ? null : productFull.price.loe(priceTo);
    }
}
