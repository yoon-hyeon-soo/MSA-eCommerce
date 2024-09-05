package com.sparta.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    public JwtFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // Authorization 헤더가 존재하는지 확인
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return chain.filter(exchange);
            }

            // Authorization 헤더의 값이 "Bearer "로 시작하는지 확인
            String authHeader = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
            if (!authHeader.startsWith("Bearer ")) {
                return handleUnauthorized(response, "잘못된 Authorization 헤더 형식입니다.");
            }

            String token = authHeader.substring(7);

            try {
                // JWT 토큰 파싱 및 검증
                Claims claims = Jwts.parser()
                        .setSigningKey(jwtSecret)
                        .parseClaimsJws(token)
                        .getBody();

                log.info("JWT 클레임: ");
                ServerHttpRequest.Builder mutatedRequest = request.mutate();
                for (Map.Entry<String, Object> entry : claims.entrySet()) {
                    String claimKey = "X-Claim-" + entry.getKey();
                    String claimValue = String.valueOf(entry.getValue());
                    mutatedRequest.header(claimKey, claimValue);
                    log.info("{}: {}", claimKey, claimValue);
                }

                // 수정된 요청을 새로운 요청으로 설정
                request = mutatedRequest.build();
                exchange = exchange.mutate().request(request).build();

            } catch (Exception e) {
                log.error("JWT 검증 실패", e);
                return handleUnauthorized(response, "JWT 검증 실패: " + e.getMessage());
            }

            log.info("커스텀 PRE 필터: 요청 URI -> {}", request.getURI());
            log.info("커스텀 PRE 필터: 요청 ID -> {}", request.getId());

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("커스텀 POST 필터: 응답 상태 코드 -> {}", response.getStatusCode());
            }));
        };
    }

    // 인증되지 않은 응답 처리
    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"error\": \"%s\", \"message\": \"%s\"}", HttpStatus.UNAUTHORIZED.getReasonPhrase(), message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    @Data
    public static class Config {
        private boolean preLogger;
        private boolean postLogger;
    }
}
