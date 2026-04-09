package com.goodee.beedan.controller.image;

import com.goodee.beedan.entity.OrderItem;
import com.goodee.beedan.repository.quote.OrderItemRepository;
import com.goodee.beedan.service.order.ThumbnailRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ThumbnailApiController {

    private final ThumbnailRedisService thumbnailRedisService;
    private final OrderItemRepository orderItemRepository;

    @GetMapping(value = "/thumb/{thumbKey}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getThumbnail(@PathVariable String thumbKey) {
        byte[] imageBytes;

        try {
            imageBytes = thumbnailRedisService.getCachedThumbnail(thumbKey);  // DB 조회 전에 Redis 캐시부터 확인

            if (imageBytes == null) { // 캐시에 이미지가 없다면 원본 찾고, 있으면 썸네일 보여줌
                log.info("Redis 캐시 미스 - 썸네일 복구 진행: {}", thumbKey);

                // 키를 가진 주문 아이템의 원본 URL을 찾기.
                OrderItem item = orderItemRepository.findByOrdItmThumbKey(thumbKey).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 썸네일 키입니다."));

                imageBytes = thumbnailRedisService.getThumbnail(thumbKey, item.getOrdItmStUrl()); // 썸네일 생성 후 저장
            }

            if (imageBytes == null || imageBytes.length == 0) return getDefaultImage(); // 기본 이미지 처리


            // 응답에 Cache-Control 헤더 추가. 브라우저가 이 이미지를 보관시간 지정(초)
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=604800")
                    .body(imageBytes);

        } catch (Exception e) {
            log.error("썸네일 응답 중 에러 발생: {}", thumbKey, e);
            return getDefaultImage();
        }
    }

    private ResponseEntity<byte[]> getDefaultImage() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new byte[0]);
    }
}
