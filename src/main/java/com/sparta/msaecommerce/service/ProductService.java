package com.sparta.msaecommerce.service;

import com.sparta.msaecommerce.dto.ProductDto;
import com.sparta.msaecommerce.entity.Product;
import com.sparta.msaecommerce.entity.User;
import com.sparta.msaecommerce.repository.ProductRepository;
import com.sparta.msaecommerce.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository,UserRepository userRepository) {
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
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public void addProduct(ProductDto productDto) {
        User user = getCurrentUser();

        // 이메일이 'chris24daa@gmail.com'이 아닌 경우 예외 발생
        if (!user.getEmail().equals("chris24daa@gmail.com")) {
            throw new IllegalArgumentException("제품을 추가할 권한이 없습니다.");
        }

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
