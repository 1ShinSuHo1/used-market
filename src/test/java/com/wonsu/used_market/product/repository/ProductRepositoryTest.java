package com.wonsu.used_market.product.repository;

import com.wonsu.used_market.product.domain.Category;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.domain.ProductImage;
import com.wonsu.used_market.product.domain.SaleType;
import com.wonsu.used_market.product.dto.ProductListResponseDto;
import com.wonsu.used_market.product.dto.ProductSearchCond;
import com.wonsu.used_market.user.domain.Role;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductRepositoryTest {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    //페치조인 테스트
    @Test
    public void findWithImages_Test () throws Exception{
        // given
        User seller = User.builder()
                .email("test@test.com")
                .password("1234")
                .nickname("테스터")
                .role(Role.USER)
                .build();
        userRepository.save(seller);

        Product product = Product.builder()
                .seller(seller)
                .title("맥북프로")
                .maker("Apple")
                .price(2000000)
                .category(Category.NOTEBOOK)
                .saleType(SaleType.DIRECT)
                .build();

        ProductImage image1 = ProductImage.builder()
                .imageUrl("image1.jpg")
                .thumbnail(false)
                .build();
        image1.assignTo(product);

        ProductImage image2 = ProductImage.builder()
                .imageUrl("thumb.jpg")
                .thumbnail(true)
                .build();
        image2.assignTo(product);

        productRepository.save(product);
        em.flush();
        em.clear();

        // when
        Product findProduct = productRepository.findWithImages(product.getId());

        // then
        assertThat(findProduct).isNotNull();
        assertThat(findProduct.getImages()).hasSize(2);
        assertThat(findProduct.getImages().stream().anyMatch(ProductImage::isThumbnail)).isTrue();
    }

    @Test
    public void search_Test() throws Exception{
        // given
        User seller = User.builder()
                .email("list@test.com")
                .password("1234")
                .nickname("리스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(seller);

        Product product = Product.builder()
                .seller(seller)
                .title("아이폰")
                .maker("Apple")
                .price(1000000)
                .category(Category.PHONE)
                .saleType(SaleType.DIRECT)
                .aiGrade("A")
                .build();

        ProductImage thumb = ProductImage.builder()
                .imageUrl("iphone-thumb.jpg")
                .thumbnail(true)
                .build();
        thumb.assignTo(product);

        productRepository.save(product);
        em.flush();
        em.clear();

        // when
        PageRequest pageable = PageRequest.of(0, 10);
        ProductSearchCond cond = new ProductSearchCond();
        Page<ProductListResponseDto> result = productRepository.search(cond, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        ProductListResponseDto dto = result.getContent().get(0);
        assertThat(dto.getThumbnailUrl()).isEqualTo("iphone-thumb.jpg");
    }


}