package com.likelion.basecode.naver.application;

import com.likelion.basecode.common.client.NaverSearchClient;
import com.likelion.basecode.common.client.TagRecommendationClient;
import com.likelion.basecode.common.error.ErrorCode;
import com.likelion.basecode.common.exception.BusinessException;
import com.likelion.basecode.naver.api.dto.response.NaverListResponseDto;
import com.likelion.basecode.naver.api.dto.response.NaverResponseDto;
import com.likelion.basecode.post.domain.Post;
import com.likelion.basecode.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NaverService {
    private final PostRepository postRepository;
    private final TagRecommendationClient tagClient;
    private final NaverSearchClient naverSearchClient;

    public NaverListResponseDto searchBlogsByPostId(Long postId) {
        // 1. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.POST_NOT_FOUND_EXCEPTION,
                        ErrorCode.POST_NOT_FOUND_EXCEPTION.getMessage()
                ));

        // 2. 태그 추천
        List<String> tags = tagClient.getRecommendedTags(post.getContents());

        // 3. 태그가 없으면 예외
        if (tags.isEmpty()) {
            throw new BusinessException(ErrorCode.TAG_RECOMMENDATION_EMPTY,
                    ErrorCode.TAG_RECOMMENDATION_EMPTY.getMessage());
        }
        // 4. 첫 번째 태그로 블로그 검색
        List<NaverResponseDto> results = naverSearchClient.search(tags.get(0));

        // 5. 결과 없으면 예외
        if (results.isEmpty()) {
            throw new BusinessException(ErrorCode.NAVER_API_NO_RESULT,
                    ErrorCode.NAVER_API_NO_RESULT.getMessage());
        }

        // 6. 최대 3개만 추려서 결과 반환
        return new NaverListResponseDto(results.stream().limit(3).toList());
    }

}