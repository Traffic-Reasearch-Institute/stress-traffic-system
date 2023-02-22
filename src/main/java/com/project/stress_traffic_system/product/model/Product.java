package com.project.stress_traffic_system.product.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", updatable = false)
    private Long id;

    private String name; //상품이름
    private int price; //가격
    private String description; //상세설명
    private int shippingFee; // 배송료
    private int imgurl; //상품이미지
    private Long clickCount; // 조회수
    private int stock; //상품수량
    private int discount; //할인(금액할인, 퍼센트할인)

    private String introduction; //책소개
    private int pages; //쪽수
    private Long orderCount;// 누적주문수량

    @ManyToOne(fetch = FetchType.LAZY)
    private SubCategory subCategory;

//    private Long category_id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @CreatedDate
    private LocalDateTime date;

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }

    //상품 주문 시 재고수량 감소 시키기
    public void removeStock(int quantity) {
        int restStock = this.stock - quantity;
        if (restStock < 0) {
            throw new IllegalArgumentException("재고가 부족합니다"); //todo 예외
        }
        this.stock = restStock;
    }

    //할인가 세팅하기
    public int getDiscount(int price, int dc) {
        if (dc > 100) {
            //dc가 "가격할인"일 경우는 정가에서 할인가를 뺀다.
            return price - dc;

        } else {
            //dc가 "할인율"일 경우는 정가에 (100-할인율)% 을 곱한다.
            return (int) (price * ( (100.0 - dc) / 100.0));
        }
    }


    //테스트를 위한 생성자 추가
    public Product(Long id, int stock) {
        this.id = id;
        this.stock = stock;
    }
}