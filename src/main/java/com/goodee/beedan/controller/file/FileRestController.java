package com.goodee.beedan.controller.file;

import com.goodee.beedan.dto.file.FileDownloadDto;
import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/files")
public class FileRestController {
    private final FileService fileService;

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        FileDownloadDto downloadDto = fileService.prepareDownload(fileId);

        String encodedFileName = UriUtils.encode(downloadDto.getFileName(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.parseMediaType(downloadDto.getContentType()))
                .contentLength(downloadDto.getContentLength())
                .body(downloadDto.getResource());
    }

}
