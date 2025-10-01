package com.wonsu.used_market.product.service;

import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.product.domain.Category;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.domain.ProductImage;
import com.wonsu.used_market.product.domain.SaleType;
import com.wonsu.used_market.product.dto.ProductImageRequestDto;
import com.wonsu.used_market.product.dto.ProductImageResponseDto;
import com.wonsu.used_market.product.repository.ProductImageRepository;
import com.wonsu.used_market.product.repository.ProductRepository;
import com.wonsu.used_market.user.domain.Role;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductImageServiceTest {

    @Autowired
    private ProductImageService productImageService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private UserRepository userRepository;

    private User seller;
    private Product product;

    @BeforeEach
    void setup() {
        seller = User.builder()
                .email("seller@test.com")
                .password("1234")
                .nickname("seller")
                .role(Role.USER)
                .build();
        userRepository.save(seller);

        product = Product.builder()
                .seller(seller)
                .title("맥북")
                .maker("Apple")
                .price(2000)
                .category(Category.NOTEBOOK)
                .saleType(SaleType.DIRECT)
                .build();
        productRepository.save(product);
    }

    //이미지 등록 테스트
    @Test
    public void addImage () throws Exception{
        // given
        ProductImageRequestDto req = new ProductImageRequestDto("test.jpg",true);

        // when
        ProductImageResponseDto response = productImageService.addImage(product.getId(), req);

        // then
        assertThat(response.getImageUrl()).isEqualTo("test.jpg");
        assertThat(response.isThumbnail()).isTrue();
    }

    //이미지 가져오기 테스트
    @Test
    public void getImage() throws Exception{
        // given
        ProductImage image1 = ProductImage.builder()
                .imageUrl("1.jpg")
                .thumbnail(false)
                .build();
        image1.assignTo(product);
        productImageRepository.save(image1);

        ProductImage image2 = ProductImage.builder()
                .imageUrl("2.jpg")
                .thumbnail(true)
                .build();
        image2.assignTo(product);
        productImageRepository.save(image2);

        // when
        List<ProductImageResponseDto> result = productImageService.getImages(product.getId());

        // then
        assertThat(result).hasSize(2);
    }

    //썸네일 가져오기 테스트
    @Test
    public void getThumbNail() throws Exception{
        // given
        ProductImage image = ProductImage.builder()
                .imageUrl("thumb.jpg")
                .thumbnail(true)
                .build();
        image.assignTo(product);
        productImageRepository.save(image);

        // when
        ProductImageResponseDto result = productImageService.getThumbnail(product.getId());

        // then
        assertThat(result.getImageUrl()).isEqualTo("thumb.jpg");
        assertThat(result.isThumbnail()).isTrue();
    }

    // 이미지 삭제하기
    @Test
    public void deleteImage() throws Exception{
        // given
        ProductImage image = ProductImage.builder()
                .imageUrl("delete.jpg")
                .thumbnail(false)
                .build();
        image.assignTo(product);
        productImageRepository.save(image);

        // when
        productImageService.deleteImage(image.getId(), seller);

        // then
        assertThat(productImageRepository.findById(image.getId())).isEmpty();
    }

    //권한 없는 사람이 이미지 삭제하기 테스트
    @Test
    public void deleteImage_noPermission() throws Exception{
        // given
        User otherUser = User.builder()
                .email("other@test.com")
                .password("5678")
                .nickname("other")
                .role(Role.USER)
                .build();
        userRepository.save(otherUser);

        ProductImage image = ProductImage.builder()
                .imageUrl("delete.jpg")
                .thumbnail(false)
                .build();
        image.assignTo(product);
        productImageRepository.save(image);

        // when & then
        assertThatThrownBy(() -> productImageService.deleteImage(image.getId(), otherUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("권한이 없습니다");
    }

}