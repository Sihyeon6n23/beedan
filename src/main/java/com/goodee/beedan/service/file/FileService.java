package com.goodee.beedan.service.file;

import com.goodee.beedan.dto.file.FileDownloadDto;
import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.entity.FileUpload;
import com.goodee.beedan.repository.file.FileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final Tika tika;
    private final String uploadPath = "D:/beedanFileUplaod";

    // 파일 저장 요청
    public void saveFile(List<MultipartFile> files, RefDto refDto) throws IOException {
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf(".") + 1);
            String uuid = UUID.randomUUID().toString();
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

            if (originalName == null || originalName.isEmpty()) {
                throw new IllegalIdentifierException("파일 이름이 없습니다.");
            }

            uploadToDisk(file, uuid, ext);

            FileUpload fileUpload = FileUpload.builder()
                    .fileNm(originalName)
                    .fileUuid(uuid)
                    .brdRefTy(refDto.getRefTy())
                    .brdRefNo(refDto.getRefNo())
                    .fileSz(file.getSize())
                    .fileExt(ext)
                    .fileCtp(getMimeType(file))
                    .fileOr(i + 1)
                    .filePat(uploadPath + datePath)
                    .build();

        }
    }
    // 물리파일 저장
    public void uploadToDisk(MultipartFile file,String uuid, String ext) throws IOException {
        Path fullPath = Paths.get(uploadPath, getDatePath(), uuid);
        File target = new File(uploadPath + File.separator + getDatePath(), uuid + "." + ext);
        file.transferTo(target);
    }
    // 물리파일 반환
    public FileDownloadDto prepareDownload(Long fileId) {
        // 1. DB에서 파일 정보 조회 (없으면 예외 발생)
        FileUpload fileUpload = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("해당 파일 기록을 찾을 수 없습니다. ID: " + fileId));

        // 2. 경로 복원 (uploadPath + DB의 filePath + UUID.ext)
        // Paths.get을 사용하면 OS별 구분자(\ 또는 /) 문제를 알아서 해결해줍니다.
        Path fullPath = Paths.get(fileUpload.getFilePat(),
                fileUpload.getFileUuid() + "." + fileUpload.getFileExt());

        // 3. 물리 리소스 생성
        Resource resource = new FileSystemResource(fullPath);

        // 4. 최종 검증: DB에는 있는데 실제 하드디스크에 파일이 없을 경우 대비
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("서버에서 물리 파일을 찾을 수 없거나 읽기 권한이 없습니다.");
        }

        // 5. 컨트롤러가 바로 쓸 수 있게 포장해서 반환
        return FileDownloadDto.builder()
                .fileName(fileUpload.getFileNm())
                .contentType(fileUpload.getFileCtp())
                .contentLength(fileUpload.getFileSz())
                .resource(resource)
                .build();
    }

    // 파일리스트 조회
    public List<FileDto> getFileList(RefDto refDto) {
        return fileRepository.findAllByBrdRefTyAndBrdRefNoAndFileDelYnFalseOrderByFileOrAsc(refDto.getRefTy(), refDto.getRefNo())
                .stream()
                .map(file -> FileDto.builder()
                        .fileUuid(file.getFileUuid())
                        .fileNm(file.getFileNm())
                        .fileOr(file.getFileOr())
                        .fileExt(file.getFileExt())
                        .fileSz(file.getFileSz())
                        .fileCtp(file.getFileCtp())
                        .fileUrl("/api/files/display/" + file.getFileId())
                        .build())
                .collect(Collectors.toList());
    }
    // 파일 단건 조회
    public FileDto getFile(Long fileId) {
        return fileRepository.findById(fileId).map(fileUpload -> FileDto.builder()
                .fileNm(fileUpload.getFileNm())
                .fileSz(fileUpload.getFileSz())
                .fileUrl(fileUpload.getFilePat())
                .fileCtp(fileUpload.getFileCtp())
                .build()).orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다."));
    }



    // 파일 삭제

    // 파일 수정

    private String getDatePath() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("MM"));
        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("dd"));

        // 2. Paths.get이 OS에 맞는 구분자(\ 또는 /)를 알아서 넣어줌
        Path datePath = Paths.get(year, month, day);

        File uploadDir = new File(uploadPath, datePath.toString());
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // 폴더가 없으면 생성
        }
        return datePath.toString();
    }

    // MYME 타입 조회 메소드
    public String getMimeType(MultipartFile file) throws IOException {
        return tika.detect(file.getInputStream());
    }
}
