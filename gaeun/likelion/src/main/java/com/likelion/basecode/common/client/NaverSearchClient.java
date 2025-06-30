package com.likelion.basecode.common.client;

import com.likelion.basecode.common.error.ErrorCode;
import com.likelion.basecode.common.exception.BusinessException;
import com.likelion.basecode.naver.api.dto.response.NaverResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NaverSearchClient {
    private final RestTemplate restTemplate;

    @Value("${naver-api.base-url}")
    private String baseUrl;

    @Value("${naver-api.client-id}")
    private String clientId;

    @Value("${naver-api.client-secret}")
    private String clientSecret;

    public List<NaverResponseDto> search(String query) {
        // url 만들기
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/blog.json")
                .queryParam("query", query)
                .queryParam("display", 3)
                .queryParam("start", 1)
                .build()
                .encode()
                .toUri();

        // 네이버는 반드시 헤더에 클라이언트 id와 secret코드를 포함해야함.
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers); // 요청에 header 정보를 포함해줌

        ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);

        // 응답 body가 null인 경우 예외 발생
        Map<String, Object> body = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NAVER_API_RESPONSE_NULL,
                        ErrorCode.NAVER_API_RESPONSE_NULL.getMessage()
                ));

        // 'items' 리스트를 추출한 후 NaverResponseDto 형태로 변환
        @SuppressWarnings("unchecked") // Java 컴파일러 경고를 무시하도록 하는 어노테이션-!
        Object itemsObj = body.get("items");

        if (!(itemsObj instanceof List<?> items)) {
            throw new BusinessException(
                    ErrorCode.NAVER_API_ITEM_MALFORMED,
                    ErrorCode.NAVER_API_ITEM_MALFORMED.getMessage()
            );
        }

        return items.stream()
                .map(item -> toDto((Map<String, Object>) item))  //
                .collect(Collectors.toList());
    }

    // Map을 NaverResponseDto로 변환
    private NaverResponseDto toDto(Map<String, Object> item) {
        return new NaverResponseDto(
                (String) item.getOrDefault("title", ""),
                (String) item.getOrDefault("description", ""),
                (String) item.getOrDefault("postdate", ""),
                (String) item.getOrDefault("link", "")
        );
    }

    // 위에서 설명한 상황과 유사하다고 생각하면 됨.
    // (정확히는 Map<String, Object> 캐스팅은 컴파일러가 타입 안정성을 확인할 수 없기 때문)
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object obj, ErrorCode errorCode) {
        // obj가 Map 타입이 아닌 경우 예외를 발생
        if (!(obj instanceof Map)) {
            // 비즈니스 로직에 정의된 에러 코드와 메시지를 포함한 예외를 던짐
            throw new BusinessException(errorCode, errorCode.getMessage());
        }

        // Map 타입이 확인 -> 따라서 안전하게 형변환하여 반환-!
        return (Map<String, Object>) obj;
    }
}