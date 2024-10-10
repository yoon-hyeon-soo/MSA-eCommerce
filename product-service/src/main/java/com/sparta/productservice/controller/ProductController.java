package com.sparta.productservice.controller;

import com.sparta.productservice.dto.ProductDto;
import com.sparta.productservice.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 전체 상품 리스트 조회
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // 단일 상품 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 제품 추가
    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody ProductDto productDto, HttpServletRequest request) {
        try {
            productService.addProduct(productDto, request);
            return ResponseEntity.ok("Product added successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
    // 재고 감소 API
    @PutMapping("/{id}/decrease-stock")
    public void decreaseStock(@PathVariable Long id, @RequestParam int quantity) {
        productService.decreaseStock(id, quantity);
    }
    // 재고 증가 API
    @PutMapping("/{id}/increase-stock")
    public void increaseStock(@PathVariable Long id, @RequestParam int quantity) {
        productService.increaseStock(id, quantity);
    }
}
