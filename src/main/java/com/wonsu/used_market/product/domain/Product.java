package com.wonsu.used_market.product.domain;

import com.wonsu.used_market.auction.domain.Auction;
import com.wonsu.used_market.transaction.domain.Transaction;
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

    @Column(length = 40, nullable = false)
    private String maker;

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
    @OrderBy("thumbnail desc, id asc ")
    private List<ProductImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "product",fetch = FetchType.LAZY)
    private Auction auction;

    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY)
    private Transaction transaction;

    //생성자 빌더로 필요한 필드만 생성할수 있게 설정
    @Builder
    private Product(User seller,
                    Category category,
                    String maker,
                    String title,
                    SaleType saleType,
                    Integer price,
                    String description,
                    String aiGrade,
                    String modelSeries,
                    String modelVariant,
                    Integer storageGb,
                    String usagePeriod,
                    String wishLocation) {
        this.seller = seller;
        this.category = category;
        this.maker = maker;
        this.title = title;
        this.saleType = saleType;
        this.price = price;
        this.description = description;
        this.aiGrade = aiGrade;
        this.modelSeries = modelSeries;
        this.modelVariant = modelVariant;
        this.storageGb = storageGb;
        this.usagePeriod = usagePeriod;
        this.wishLocation = wishLocation;
        this.status = ProductStatus.ON_SALE;
    }


    // 연관관계 편의 메서드
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    // 절대 외부에서 직접 호출하지 말 것
    public void assignTransaction(Transaction transaction) {
        this.transaction = transaction;
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

    //엔티티 변경을 위한 메서드
    // Product.java
    public void changeTitle(String title) { this.title = title; }
    public void changeDescription(String description) { this.description = description; }
    public void changePrice(Integer price) { this.price = price; }
    public void changeStatus(ProductStatus status) { this.status = status; }




}
