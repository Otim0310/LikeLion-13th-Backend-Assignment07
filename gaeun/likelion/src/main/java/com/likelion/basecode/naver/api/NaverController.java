package com.likelion.basecode.naver.api;

import com.likelion.basecode.common.error.SuccessCode;
import com.likelion.basecode.common.template.ApiResTemplate;
import com.likelion.basecode.naver.api.dto.response.NaverListResponseDto;
import com.likelion.basecode.naver.application.NaverService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/naver")
public class NaverController {
    private final NaverService naverService;

    @GetMapping("/recommendations")
    public ApiResTemplate<NaverListResponseDto> searchBlogsByPostId(@RequestParam("postId") Long postId) {
        NaverListResponseDto naverListResponseDto = naverService.searchBlogsByPostId(postId);
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, naverListResponseDto);
    }
}