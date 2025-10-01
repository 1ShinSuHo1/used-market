package com.wonsu.used_market.product.service;

import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.product.domain.Category;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.domain.ProductImage;
import com.wonsu.used_market.product.domain.SaleType;
import com.wonsu.used_market.product.dto.*;
import com.wonsu.used_market.product.repository.ProductRepository;
import com.wonsu.used_market.user.domain.Role;
import com.wonsu.used_market.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    //상품 등록
    @Transactional
    public CreateProductResponseDto createProduct(User seller, CreateProductRequestDto req){
        Category category;

        //알맞은 카테고리인지 검사하는 예외처리
        try {
            category = Category.valueOf(req.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY);
        }

        SaleType saleType;

        //알맞은 판매방식인지 검사하는 예외처리
        try {
            saleType = SaleType.valueOf(req.getSaleType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_SALETYPE);
        }

        //DTO를 엔티티로 변환
        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .maker(req.getMaker())
                .title(req.getTitle())
                .saleType(saleType)
                .price(req.getPrice())
                .description(req.getDescription())
                .aiGrade(req.getAiGrade())
                .modelSeries(req.getModelSeries())
                .modelVariant(req.getModelVariant())
                .storageGb(req.getStorageGb())
                .usagePeriod(req.getUsagePeriod())
                .wishLocation(req.getWishLocation())
                .build();


        // 이미지 등록 (필수: 1개 이상)
        if (req.getImages() == null || req.getImages().isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        boolean hasThumbnail = false;
        for (ProductImageRequestDto imgReq : req.getImages()) {
            ProductImage image = ProductImage.builder()
                    .imageUrl(imgReq.getImageUrl())
                    .thumbnail(imgReq.isThumbnail())
                    .build();

            image.assignTo(product);

            if (imgReq.isThumbnail()) {
                hasThumbnail = true;
            }
        }

        //썸네일이 없는경우
        // 썸네일 없는 경우 → 첫 번째 이미지를 자동 썸네일로
        if (!hasThumbnail) {
            product.getImages().get(0).markAsThumbnail();
        }

        Product savedProduct = productRepository.save(product);

        return new CreateProductResponseDto(savedProduct);

    }

    //상품 상세 조회
    @Cacheable(value = "productDetail", key = "#productId")
    public ProductDetailResponseDto getProductDetail(Long productId){
        Product product = productRepository.findWithImages(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return new ProductDetailResponseDto(product);
    }

    // 상품 목록 조회
    @Cacheable(
            value = "productList",
            key = "#cond.keyword + '_' + #cond.category + '_' + #cond.status + '_' + #pageable.pageNumber"
    )
    public Page<ProductListResponseDto> getProducts(ProductSearchCond cond, Pageable pageable) {
        return productRepository.search(cond, pageable);
    }

    //상품 정보 수정(제목,설명,가격,상태만 변경가능으로 설정)
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productDetail", key = "#productId"),
            @CacheEvict(value = "productList", allEntries = true)
    })
    public UpdateProductResponseDto updateProduct(Long productId, UpdateProductRequestDto req, User currentUser){
        //상품 조회
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        //소유자 검증(본인이랑 관리자만 수정할수 있도록 설정)
        if(!product.getSeller().getId().equals(currentUser.getId())
                && !currentUser.getRole().equals(Role.ADMIN)){
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        //변경감지로 어차피 자동업데이트 예정
        if (req.getTitle() != null) product.changeTitle(req.getTitle());
        if (req.getDescription() != null) product.changeDescription(req.getDescription());
        if (req.getPrice() != null) product.changePrice(req.getPrice());
        if (req.getStatus() != null) product.changeStatus(req.getStatus());

        return new UpdateProductResponseDto(product.getId(), product.getUpdatedAt());
    }

    // 상품 삭제
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productDetail", key = "#productId"),
            @CacheEvict(value = "productList", allEntries = true)
    })
    public void deleteProduct(Long productId, User currentUser){
        //상품 조회
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        //소유자 검증(본인이랑 관리자만 삭제가능하도록 설정)
        if(!product.getSeller().getId().equals(currentUser.getId())
                && !currentUser.getRole().equals(Role.ADMIN)){
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        productRepository.delete(product);
    }



}
