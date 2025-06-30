package com.likelion.basecode.naver.api.dto.response;

public record NaverResponseDto(
        String title,
        String description,
        String postdate,
        String link
) {}