package com.project.stress_traffic_system.product.repository;

import com.project.stress_traffic_system.product.model.Category;
import com.project.stress_traffic_system.product.model.dto.ProductResponseDto;
import com.project.stress_traffic_system.product.model.dto.ProductSearchCondition;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductRepositoryCustom {

    Page<ProductResponseDto> searchProducts(ProductSearchCondition condition);

    void bulkInsert();
    void bulkInsertMembers();
    Page<ProductResponseDto> findAllOrderByClickCountDesc(int page); //전체상품 조회(페이징)

    Page<ProductResponseDto> searchByCategory(Long categoryId, int page); // 카테고리(소분류) 검색
    List<ProductResponseDto> findByMainCategory(Category category, int page); //카테고리(대분류) 검색

    List<ProductResponseDto> findBestSeller(int page); //베스트셀러 검색
    List<ProductResponseDto> findBestSeller(); //베스트셀러 상위 1만건 검색

    List<ProductResponseDto> findByMainCategory(Long categoryId); // 카테고리(대분류) 상위 1만건 검색

    List<ProductResponseDto> TestFindByMainCategory(Long categoryId); // 카테고리(대분류) 상위 100건 검색

    List<ProductResponseDto> findProductDetail();

    Long getClickCount(Long productId);

    void setClickCount(Long productId, long clickCount);

    List<ProductResponseDto> findByKeyword(String keyword); //like 검색 (100건)

    List<ProductResponseDto> findByFullKeyword(String keyword); // full text 검색 (100건)

    List<ProductResponseDto> findTop1000(); //조회수 상위 1000건 검색
}
