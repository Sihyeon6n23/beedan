package com.goodee.beedan.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailRedisService {

    @Value("${file.thumbnail-dir}")
    private String uploadDir;
    private final @Qualifier("thumbnailRedisTemplate") RedisTemplate<String, byte[]> redisTemplate;

    @Async
    public void generateAndCache(String thumbKey, String originPath) {
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(thumbKey))) return;

            Thumbnails.Builder<?> builder;
            if (originPath.startsWith("http")) {
                URLConnection conn = new URL(originPath).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                builder = Thumbnails.of(conn.getInputStream());
            } else {
                String fullPath = uploadDir + originPath.replace("\\", "/");
                builder = Thumbnails.of(new File(fullPath));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            builder.size(100, 100)
                    .outputFormat("jpg")
                    .outputQuality(0.6f)
                    .toOutputStream(baos);

            redisTemplate.opsForValue().set(thumbKey, baos.toByteArray(), 3, TimeUnit.HOURS);
            log.info("썸네일 생성 및 캐싱 완료: {}", thumbKey);

        } catch (Exception e) {
            log.error("썸네일 생성 실패: key={}, path={}", thumbKey, originPath, e);
        }
    }

    public byte[] getThumbnail(String thumbKey, String originUrl) {
        byte[] cachedImage = redisTemplate.opsForValue().get(thumbKey);
        if (cachedImage != null) {
            return cachedImage;
        }

        byte[] newThumbnail = createThumbnailSync(originUrl);

        if (newThumbnail.length > 0) {
            redisTemplate.opsForValue().set(thumbKey, newThumbnail, 1, TimeUnit.DAYS);
        }

        return newThumbnail;
    }

    public byte[] createThumbnailSync(String originPath) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Thumbnails.Builder<?> builder;

            if (originPath.startsWith("http")) {
                URLConnection conn = new URL(originPath).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                builder = Thumbnails.of(conn.getInputStream());
            } else {
                String fullPath = uploadDir + originPath.replace("\\", "/");
                File file = new File(fullPath);
                if (!file.exists()) {
                    log.error("파일이 존재하지 않습니다: {}", fullPath);
                    return getDefaultImage();
                }
                builder = Thumbnails.of(file);
            }

            builder.size(100, 100)
                    .outputFormat("jpg")
                    .toOutputStream(baos);

            return baos.toByteArray();
        } catch (IOException e) {
            log.error("동기 썸네일 생성 실패: path={}", originPath, e);
            return getDefaultImage();
        }
    }

    private byte[] getDefaultImage() {
        return new byte[0];
    }

    public byte[] getCachedThumbnail(String thumbKey) {
        return redisTemplate.opsForValue().get(thumbKey);
    }

}