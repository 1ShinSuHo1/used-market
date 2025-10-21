package com.wonsu.used_market.product.service;


import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.domain.ProductImage;
import com.wonsu.used_market.product.dto.ProductImageRequestDto;
import com.wonsu.used_market.product.dto.ProductImageResponseDto;
import com.wonsu.used_market.product.repository.ProductImageRepository;
import com.wonsu.used_market.product.repository.ProductRepository;
import com.wonsu.used_market.user.domain.Role;
import com.wonsu.used_market.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;


    //상품 이미지 등록
    @Transactional
    public ProductImageResponseDto addImage(Long productId, ProductImageRequestDto req){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        //썸네일중복 방지
        if(req.isThumbnail()){
            boolean existsThumbnail = productImageRepository.existsByProductIdAndThumbnailTrue(productId);
            if (existsThumbnail) {
                throw new BusinessException(ErrorCode.DUPLICATE_THUMBNAIL);
            }
        }
        ProductImage image = ProductImage.builder()
                .imageUrl(req.getImageUrl())
                .thumbnail(req.isThumbnail())
                .build();

        //연관관계 주입
        image.assignTo(product);
        return new ProductImageResponseDto(productImageRepository.save(image));
    }

    // 상품 이미지 썸네일 변경
    @Transactional
    public ProductImageResponseDto updateThumbnail(Long imageId, User currentUser) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        // 권한 체크
        User seller = image.getProduct().getSeller();
        if (!seller.getId().equals(currentUser.getId())
                && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        Product product = image.getProduct();

        // 기존 썸네일 해제
        product.getImages().forEach(img -> {
            if (img.isThumbnail()) {
                img.unmarkAsThumbnail();
            }
        });

        // 새로운 이미지 썸네일 지정
        image.markAsThumbnail();

        return new ProductImageResponseDto(image);
    }


    //상품 이미지 조회 최적화
    public List<ProductImageResponseDto> getImages(Long productId){
        return productImageRepository.findDtosByProductId(productId);
    }

    public ProductImageResponseDto getThumbnail(Long productId){
        ProductImage image = productImageRepository.findByProductIdAndThumbnailTrue(productId);
        if (image == null) {
            throw new BusinessException(ErrorCode.THUMBNAIL_NOT_FOUND);
        }
        return new ProductImageResponseDto(image);
    }

    //상품 이미지 삭제
    @Transactional
    public void  deleteImage(Long imageId, User currentUser){
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        //권한 체크 본인이나 관리자만 삭제가능하도록
        User seller = image.getProduct().getSeller();
        if (!seller.getId().equals(currentUser.getId())
                && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        productImageRepository.delete(image);
    }
}
