package com.wonsu.used_market.product.service;

import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.product.domain.Category;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.domain.ProductStatus;
import com.wonsu.used_market.product.domain.SaleType;
import com.wonsu.used_market.product.dto.*;
import com.wonsu.used_market.product.repository.ProductRepository;
import com.wonsu.used_market.user.domain.Role;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceTest {
    @Autowired
    ProductService productService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    EntityManager em;

    @Autowired
    StringRedisTemplate redisTemplate;


    private List<ProductImageRequestDto> createDummyImages() {
        return List.of(
                new ProductImageRequestDto("https://test.com/1.jpg", true),
                new ProductImageRequestDto("https://test.com/2.jpg", false)
        );
    }

    @Test
    public void productCreate() throws Exception{
        // given
        User seller = User.builder()
                .email("test@test.com")
                .password("1234")
                .nickname("닉네임")
                .role(Role.USER)
                .build();
        userRepository.save(seller);

        CreateProductRequestDto req = new CreateProductRequestDto(
                "PHONE", "Samsung", "Galaxy", "S21", 128,
                "테스트 제목", "테스트 설명", "1년", "DIRECT", 100000, "서울", "A",
                createDummyImages()
        );

        // when
        CreateProductResponseDto response = productService.createProduct(seller, req);

        // then
        Product findProduct = em.find(Product.class, response.getId());
        assertThat(findProduct).isNotNull();
        assertThat(findProduct.getSeller().getId()).isEqualTo(seller.getId());
        assertThat(findProduct.getTitle()).isEqualTo("테스트 제목");
        assertThat(findProduct.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }

    @Test
    public void productUpdate () throws Exception{
        // given
        User seller = User.builder()
                .email("test2@test.com")
                .password("1234")
                .nickname("닉네임2")
                .role(Role.USER)
                .build();
        userRepository.save(seller);

        CreateProductRequestDto req = new CreateProductRequestDto(
                "PHONE", "Apple", "iPhone", "15 Pro", 256,
                "아이폰15", "좋은폰", "6개월", "DIRECT", 1500000, "서울", "S",
                createDummyImages()
        );
        CreateProductResponseDto created = productService.createProduct(seller, req);

        UpdateProductRequestDto updateReq = new UpdateProductRequestDto(
                "아이폰15 수정", "업데이트 설명", 1400000, ProductStatus.RESERVED
        );

        // when
        UpdateProductResponseDto response = productService.updateProduct(created.getId(), updateReq, seller);

        // then
        Product updated = em.find(Product.class, response.getId());
        assertThat(updated.getTitle()).isEqualTo("아이폰15 수정");
        assertThat(updated.getDescription()).isEqualTo("업데이트 설명");
        assertThat(updated.getPrice()).isEqualTo(1400000);
        assertThat(updated.getStatus()).isEqualTo(ProductStatus.RESERVED);
    }

    @Test
    public void productDelete() throws Exception{
        // given
        User seller = User.builder()
                .email("test3@test.com")
                .password("1234")
                .nickname("닉네임3")
                .role(Role.USER)
                .build();
        userRepository.save(seller);

        CreateProductRequestDto req = new CreateProductRequestDto(
                "TABLET", "LG", "GPad", "Pro", 64,
                "LG패드", "테스트용", "2년", "DIRECT", 300000, "부산", "B",
                createDummyImages()
        );
        CreateProductResponseDto created = productService.createProduct(seller, req);

        // when
        productService.deleteProduct(created.getId(), seller);

        // then
        Product deleted = em.find(Product.class, created.getId());
        assertThat(deleted).isNull();
    }

    //내가 아닌 사용자가 수정시예외발생
    @Test
    public void ifNotMeUpdateProductError() throws Exception{
        // given
        User seller = User.builder()
                .email("test4@test.com")
                .password("1234")
                .nickname("닉네임4")
                .role(Role.USER)
                .build();
        userRepository.save(seller);

        User another = User.builder()
                .email("other@test.com")
                .password("1234")
                .nickname("다른유저")
                .role(Role.USER)
                .build();
        userRepository.save(another);

        CreateProductRequestDto req = new CreateProductRequestDto(
                "PHONE", "Apple", "iPhone", "13", 128,
                "아이폰13", "좋은폰", "1년", "DIRECT", 800000, "서울", "B",
                createDummyImages()
        );
        CreateProductResponseDto created = productService.createProduct(seller, req);

        UpdateProductRequestDto updateReq = new UpdateProductRequestDto(
                "수정제목", "수정설명", 750000, ProductStatus.ON_SALE
        );

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(created.getId(), updateReq, another))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NO_PERMISSION.getMessage());
    }

    //캐쉬 잘 돌아가는지 테스트
    @Test
    void cache_check() throws Exception {
        // given
        User seller = User.builder()
                .email("test@test.com")
                .nickname("suho")
                .password("1234")
                .role(Role.USER)
                .build();

        userRepository.save(seller);

        Product product = Product.builder()
                .seller(seller)
                .title("맥북")
                .maker("Apple")
                .price(1000)
                .category(Category.NOTEBOOK)
                .saleType(SaleType.DIRECT)
                .build();

        productRepository.save(product);

        // when
        productService.getProductDetail(product.getId());

        // then (캐시 저장 확인)
        String redisKey = "productDetail::" + product.getId();
        String cached = redisTemplate.opsForValue().get(redisKey);
        assertThat(cached).isNotNull();
    }
}

