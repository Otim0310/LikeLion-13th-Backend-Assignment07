package com.likelion.basecode.naver.api.dto.response;

import java.util.List;

public record NaverListResponseDto(
        List<NaverResponseDto> items
) {
}