package com.sparta.orderservice.client;

import com.sparta.orderservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")  // product-service와 통신
public interface ProductFeignClient {

    // 제품 ID로 제품 정보 조회
    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);

    @PutMapping("/api/products/{id}/decrease-stock")
    void decreaseStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);

    @PutMapping("/api/products/{id}/increase-stock")
    void increaseStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);
}
