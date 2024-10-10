package com.sparta.productservice.service;

import com.sparta.productservice.client.UserClient;
import com.sparta.productservice.dto.ProductDto;
import com.sparta.productservice.dto.UserResponseDto;
import com.sparta.productservice.entity.Product;
import com.sparta.productservice.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserClient userClient;  // FeignClient를 통해 user-service와 통신

    public ProductService(ProductRepository productRepository, UserClient userClient) {
        this.productRepository = productRepository;
        this.userClient = userClient;
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    // 헤더에서 userId를 가져오는 메서드
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String userIdHeader = request.getHeader("x-claim-userid");
        if (userIdHeader == null) {
            throw new RuntimeException("헤더에 사용자 ID가 없습니다.");
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new RuntimeException("헤더에 있는 사용자 ID 형식이 잘못되었습니다.");
        }

        return userId;
    }

    // 제품 추가 메서드
    public void addProduct(ProductDto productDto, HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);  // 헤더에서 userId 추출

        // FeignClient를 사용하여 user-service에서 사용자 정보 가져오기
        UserResponseDto user = userClient.getUserById(userId);

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 사용자 정보가 유효하면 상품 추가 로직 실행
        Product product = Product.builder()
                .name(productDto.getName())
                .price(productDto.getPrice())
                .description(productDto.getDescription())
                .stockQuantity(productDto.getStockQuantity())
                .build();

        productRepository.save(product);
    }

    private ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity()
        );
    }
    // 재고 감소
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("제품을 찾을 수 없습니다: " + productId));

        product.decreaseStock(quantity);  // Product 엔티티의 메서드 호출
        productRepository.save(product);  // DB에 반영
    }

    // 재고 증가
    public void increaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("제품을 찾을 수 없습니다: " + productId));

        product.increaseStock(quantity);  // Product 엔티티의 메서드 호출
        productRepository.save(product);  // DB에 반영
    }

}
