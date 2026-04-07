package com.goodee.beedan;

import com.goodee.beedan.dto.file.FileDownloadDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.entity.FileUpload;
import com.goodee.beedan.repository.file.FileRepository;
import com.goodee.beedan.service.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 테스트 후 DB는 롤백되지만, 물리 파일은 직접 지워야 함
@Slf4j
class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    private final String TEST_PATH = "D:/beedanFileUplaod";

    @Test
    @DisplayName("파일 업로드 및 물리 파일 생성 확인")
    void saveFileTest() throws IOException {
        // given: 가짜 파일 생성
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test_image.png",
                "image/png",
                "test content".getBytes()
        );
        RefDto refDto = new RefDto("BOARD", 1L);

        // when: 서비스 호출
        fileService.saveFile(List.of(file), refDto);

        // then: DB 조회 및 물리 파일 존재 확인
        List<FileUpload> savedFiles = fileRepository.findAllByBrdRefTyAndBrdRefNoAndFileDelYnFalseOrderByFileOrAsc("BOARD", 1L);

        assertThat(savedFiles).isNotEmpty();
        FileUpload savedFile = savedFiles.get(0);

        Path physicalPath = Paths.get(savedFile.getFilePat(), savedFile.getFileUuid() + "." + savedFile.getFileExt());
        log.info("체크할 물리 경로: {}", physicalPath);

        assertThat(Files.exists(physicalPath)).isTrue(); // 실제 디스크에 파일이 있는지 확인
    }

    @Test
    @DisplayName("파일 다운로드용 리소스 준비 확인")
    void prepareDownloadTest() throws IOException {
        // given: 파일 하나 저장 후 ID 가져오기
        MockMultipartFile file = new MockMultipartFile("files", "down.png", "text/plain", "hello".getBytes());
        fileService.saveFile(List.of(file), new RefDto("FREE", 99L));
        FileUpload target = fileRepository.findAllByBrdRefTyAndBrdRefNoAndFileDelYnFalseOrderByFileOrAsc("FREE", 99L).get(0);

        // when
        FileDownloadDto downloadDto = fileService.prepareDownload(target.getFileUuid());

        // then
        assertThat(downloadDto.getResource().exists()).isTrue();
        assertThat(downloadDto.getFileName()).isEqualTo("down.png");
    }

    @Test
    @DisplayName("수정 시나리오: 기존 파일 삭제 후 신규 추가")
    void updateScenarioTest() throws IOException {
        // 1. 기존 파일 1개 저장
        MockMultipartFile oldFile = new MockMultipartFile("files", "old.jpg", "image/jpeg", "old".getBytes());
        fileService.saveFile(List.of(oldFile), new RefDto("UPDATE", 100L));
        FileUpload oldEntity = fileRepository.findAllByBrdRefTyAndBrdRefNoAndFileDelYnFalseOrderByFileOrAsc("UPDATE", 100L).get(0);
        Long oldId = oldEntity.getFileId();

        // 2. [수정 진행] 기존 삭제 + 신규 추가
        // 삭제 호출
        fileService.deleteFilesById(List.of(oldId));

        // 신규 추가 호출
        MockMultipartFile newFile = new MockMultipartFile("files", "new.png", "image/png", "new".getBytes());
        fileService.saveFile(List.of(newFile), new RefDto("UPDATE", 100L));

        // 3. 결과 검증
        FileUpload updatedOld = fileRepository.findById(oldId).get();
        assertThat(updatedOld.getFileDelYn()).isTrue(); // DB상 삭제 처리 확인

        List<FileUpload> currentFiles = fileRepository.findAllByBrdRefTyAndBrdRefNoAndFileDelYnFalseOrderByFileOrAsc("UPDATE", 100L);
        assertThat(currentFiles).hasSize(1);
        assertThat(currentFiles.get(0).getFileNm()).isEqualTo("new.png");
    }
}