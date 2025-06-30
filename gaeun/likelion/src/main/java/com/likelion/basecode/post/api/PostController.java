package com.likelion.basecode.post.api;

import com.likelion.basecode.common.error.SuccessCode;
import com.likelion.basecode.common.template.ApiResTemplate;
import com.likelion.basecode.post.api.dto.response.PostInfoResponseDto;
import com.likelion.basecode.post.api.dto.response.PostListResponseDto;
import com.likelion.basecode.post.api.dto.request.PostSaveRequestDto;
import com.likelion.basecode.post.api.dto.request.PostUpdateRequestDto;
import com.likelion.basecode.post.application.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 게시물 저장
    @PostMapping("/save")
    public ApiResTemplate<PostInfoResponseDto> postSave(@RequestPart("post") PostSaveRequestDto postSaveRequestDto,
                                                        @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        PostInfoResponseDto postInfoResponseDto = postService.postSave(postSaveRequestDto, imageFile);
        return ApiResTemplate.successResponse(SuccessCode.POST_SAVE_SUCCESS, postInfoResponseDto);
    }

    // 사용자 id를 기준으로 해당 사용자가 작성한 게시글 목록 조회
    @GetMapping("/{memberId}")
    public ApiResTemplate<PostListResponseDto> myPostFindAll(@PathVariable("memberId") Long memberId) {
        PostListResponseDto postListResponseDto = postService.postFindMember(memberId);
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, postListResponseDto);
    }

    // 게시물 id를 기준으로 사용자가 작성한 게시물 수정
    @PatchMapping("/{postId}")
    public ApiResTemplate<PostInfoResponseDto> postUpdate(
            @PathVariable("postId") Long postId,
            @RequestPart("post") PostUpdateRequestDto postUpdateRequestDto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        PostInfoResponseDto postInfoResponseDto = postService.postUpdate(postId, postUpdateRequestDto, imageFile);
        return ApiResTemplate.successResponse(SuccessCode.POST_SAVE_SUCCESS, postInfoResponseDto);
    }

    // 게시물 id를 기준으로 사용자가 작성한 게시물 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> postDelete(
            @PathVariable("postId") Long postId) {
        postService.postDelete(postId);
        return new ResponseEntity<>("게시물 삭제", HttpStatus.OK);
    }
    // postId의 이미지를 삭제
    @DeleteMapping("/{postId}/image")
    public ResponseEntity<String> postImageDelete(@PathVariable("postId") Long postId) {
        postService.postUrlDelete(postId); // 이미지만 삭제하는 메서드
        return new ResponseEntity<>("이미지 삭제 성공", HttpStatus.OK);
    }


}