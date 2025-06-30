package com.likelion.basecode.common.client;

import com.likelion.basecode.common.error.ErrorCode;
import com.likelion.basecode.common.exception.BusinessException;
import com.likelion.basecode.naver.api.dto.response.NaverResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NaverSearchClient {

    private static final String BLOG_ENDPOINT = "/blog.json";
    private static final int DEFAULT_DISPLAY_COUNT = 3;
    private static final int DEFAULT_START_INDEX = 1;

    private final RestTemplate restTemplate;

    @Value("${naver-api.base-url}")
    private String naverApiBaseUrl;

    @Value("${naver-api.client-id}")
    private String naverClientId;

    @Value("${naver-api.client-secret}")
    private String naverClientSecret;

    public List<NaverResponseDto> search(String searchKeyword) {
        String requestUrl = buildSearchUrl(searchKeyword);
        HttpEntity<String> httpRequest = createHttpRequest();

        ResponseEntity<Map> apiResponse = executeSearchRequest(requestUrl, httpRequest);
        Map<String, Object> responseBody = validateAndExtractResponseBody(apiResponse);

        List<Map<String, Object>> searchItems = extractSearchItems(responseBody);
        return convertToResponseDtos(searchItems);
    }

    private String buildSearchUrl(String keyword) {
        return String.format("%s%s?query=%s&display=%d&start=%d",
                naverApiBaseUrl,
                BLOG_ENDPOINT,
                keyword,
                DEFAULT_DISPLAY_COUNT,
                DEFAULT_START_INDEX);
    }

    private HttpEntity<String> createHttpRequest() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("X-Naver-Client-Id", naverClientId);
        requestHeaders.add("X-Naver-Client-Secret", naverClientSecret);
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(requestHeaders);
    }

    private ResponseEntity<Map> executeSearchRequest(String url, HttpEntity<String> request) {
        try {
            return restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.NAVER_API_RESPONSE_NULL,
                    "네이버 API 요청 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> validateAndExtractResponseBody(ResponseEntity<Map> response) {
        if (response.getBody() == null) {
            throw new BusinessException(
                    ErrorCode.NAVER_API_RESPONSE_NULL,
                    ErrorCode.NAVER_API_RESPONSE_NULL.getMessage()
            );
        }
        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractSearchItems(Map<String, Object> responseBody) {
        Object itemsData = responseBody.get("items");

        if (itemsData == null) {
            return Collections.emptyList();
        }

        if (!(itemsData instanceof List<?>)) {
            throw new BusinessException(
                    ErrorCode.NAVER_API_ITEM_MALFORMED,
                    ErrorCode.NAVER_API_ITEM_MALFORMED.getMessage()
            );
        }

        List<?> rawItems = (List<?>) itemsData;
        return rawItems.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .collect(Collectors.toList());
    }

    private List<NaverResponseDto> convertToResponseDtos(List<Map<String, Object>> items) {
        return items.stream()
                .map(this::mapToNaverResponseDto)
                .collect(Collectors.toList());
    }

    private NaverResponseDto mapToNaverResponseDto(Map<String, Object> itemData) {
        String title = extractStringValue(itemData, "title");
        String description = extractStringValue(itemData, "description");
        String postDate = extractStringValue(itemData, "postdate");
        String linkUrl = extractStringValue(itemData, "link");

        return new NaverResponseDto(title, description, postDate, linkUrl);
    }

    private String extractStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }
}
