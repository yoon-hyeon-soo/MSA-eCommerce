package com.sparta.productservice.service;

import com.sparta.productservice.dto.ProductDto;
import com.sparta.productservice.entity.Product;
import com.sparta.productservice.entity.User;
import com.sparta.productservice.repository.ProductRepository;
import com.sparta.productservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
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
    public int extractUserIdFromRequest(HttpServletRequest request) {
        String userIdHeader = request.getHeader("x-claim-userid");
        if (userIdHeader == null) {
            throw new RuntimeException("헤더에 사용자 ID가 없습니다.");
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdHeader);
        } catch (NumberFormatException e) {
            throw new RuntimeException("헤더에 있는 사용자 ID 형식이 잘못되었습니다.");
        }

        return userId;
    }

    // 제품 추가 메서드
    public void addProduct(ProductDto productDto, HttpServletRequest request) {
        int userId = extractUserIdFromRequest(request);  // 헤더에서 userId 추출
        User user = userRepository.findById((long) userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

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
}