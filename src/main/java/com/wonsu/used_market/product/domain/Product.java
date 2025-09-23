package com.wonsu.used_market.product.domain;

import com.wonsu.used_market.auction.domain.Auction;
import com.wonsu.used_market.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 기본이 즉시로딩이니 지연로딩 설정
    @JoinColumn(name = "seller_id" , nullable = false)
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(name = "model_series", length = 80)
    private String modelSeries; // 기종

    @Column(name = "model_variant", length = 80)
    private String modelVariant; // 세부 기종

    @Column(name = "storage_gb")
    private Integer storageGb; // 저장 용량

    @Column(length = 20, nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "made_year")
    private Integer madeYear;

    @Column(name = "usage_period")
    private String usagePeriod;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private SaleType saleType;

    private Integer price;

    @Column(length = 10, name = "ai_grade")
    private String aiGrade; // 추후에 변경 예정

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private ProductStatus status = ProductStatus.ON_SALE;

    @Column(length = 120,name = "wish_location")
    private String wishLocation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "product",fetch = FetchType.LAZY)
    private Auction auction;

    //생성자 빌더로 필요한 필드만 생성할수 있게 설정
    @Builder
    private Product(User seller,
                    Category category,
                    String title,
                    SaleType saleType,
                    Integer price,
                    String description) {
        this.seller = seller;
        this.category = category;
        this.title = title;
        this.saleType = saleType;
        this.price = price;
        this.description = description;
        this.status = ProductStatus.ON_SALE;
    }


    // 연관관계 편의 메서드
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    //절대 외부에서 직접 호출하지 말기    
    public void assignAuction(Auction auction) {
        this.auction = auction;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }





}
