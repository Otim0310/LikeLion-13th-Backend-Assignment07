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
    private final TagRecommendationClient tagRecommendationClient;
    private final NaverSearchClient naverSearchClient;

    public NaverListResponseDto searchBlogsByPostId(Long postId) {
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.POST_NOT_FOUND_EXCEPTION,
                        ErrorCode.POST_NOT_FOUND_EXCEPTION.getMessage()
                ));

        List<String> recommendedTags = tagRecommendationClient.getRecommendedTags(foundPost.getContents());

        if (recommendedTags.isEmpty()) {
            throw new BusinessException(ErrorCode.TAG_RECOMMENDATION_EMPTY,
                    ErrorCode.TAG_RECOMMENDATION_EMPTY.getMessage());
        }

        String primaryTag = recommendedTags.get(0);
        List<NaverResponseDto> searchResults = naverSearchClient.search(primaryTag);

        if (searchResults.isEmpty()) {
            throw new BusinessException(ErrorCode.NAVER_API_NO_RESULT,
                    ErrorCode.NAVER_API_NO_RESULT.getMessage());
        }

        List<NaverResponseDto> limitedResults = searchResults.stream().limit(3).toList();
        return new NaverListResponseDto(limitedResults);
    }
}
