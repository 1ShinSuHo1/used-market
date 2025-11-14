package com.wonsu.used_market.product.service;

import com.wonsu.used_market.common.upload.FileUploader;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final AiPredictService aiPredictService;
    private final FileUploader fileUploader;

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



        // 썸네일이 있으면 그걸 ai검증하고 썸네일이 없으면 첫번째 이미지를 ai검증
        AiPredictService.AiResponse aiResult = req.getImages().stream()
                .filter(ProductImageRequestDto::isThumbnail)
                .findFirst()
                .map(img -> aiPredictService.predict(img.getImageUrl()))
                .orElseGet(() -> aiPredictService.predict(req.getImages().get(0).getImageUrl()));

        String aiGrade = aiResult.getGrade();
        Double confidence = aiResult.getConfidence();

        //DTO를 엔티티로 변환
        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .maker(req.getMaker())
                .title(req.getTitle())
                .saleType(saleType)
                .price(req.getPrice())
                .description(req.getDescription())
                .aiGrade(aiGrade)
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

        // ProductImage 엔티티 생성 (현재는 temp URL or 외부 URL)
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


        // TEMP URL → 실제 경로(products/{id}/...) 로 이동 + URL 갱신
        for (ProductImage image : savedProduct.getImages()) {
            String url = image.getImageUrl();

            // TEMP 패턴이면 옮긴다 ("/temp/" 포함 여부로 판단)
            if (url.contains("/temp/")) {
                try {
                    String newUrl = fileUploader.moveTempToProduct(url, savedProduct.getId());
                    image.changeImageUrl(newUrl);
                    log.info("[이미지 TEMP→PRODUCT 이동] {} -> {}", url, newUrl);
                } catch (Exception e) {
                    log.error("[이미지 이동 실패] url={}", url, e);
                    // 필요하면 예외로 터뜨리거나, 그냥 로그만 찍고 넘어가도됨(선택)
                }
            }
        }

        return new CreateProductResponseDto(savedProduct, confidence);

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
    public Map<String, Object> getProducts(ProductSearchCond cond, Pageable pageable) {
        Page<ProductListResponseDto> page = productRepository.search(cond, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("pageNumber", page.getNumber());
        response.put("pageSize", page.getSize());
        return response;
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

        // 상품이미지 뽑기
        List<ProductImage> images = product.getImages();

        //s3삭제
        for (ProductImage img : images) {
            String url = img.getImageUrl();
            try {
                fileUploader.delete(url);
            } catch (Exception e) {
                log.error("[S3 삭제 실패] url={}", url);
            }
        }

        productRepository.delete(product);
    }



}
