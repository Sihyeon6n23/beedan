package com.goodee.beedan.service.file;

import com.goodee.beedan.dto.file.FileDownloadDto;
import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.entity.FileUpload;
import com.goodee.beedan.repository.file.FileRepository;
import com.goodee.beedan.service.root.SecurityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileService {
    private final FileRepository fileRepository;
    private final Tika tika;
    @Value("${file.upload.path}")
    private String uploadPath;
    private final SecurityService securityService;

    /**
     * [FileService] - 파일 관리 비즈니스 로직
     * * 주요 기능:
     * 1. saveFile        : 파일 저장 (물리 파일 저장 + DB 기록). 빈 파일은 자동으로 제외함.
     * (인자: List<MultipartFile>, RefDto / 반환: List<FileDto>)
     * * 2. prepareDownload : 파일 다운로드 준비. UUID를 통해 물리 경로를 복원하고 Resource를 생성함.
     * (인자: String fileUuid / 반환: FileDownloadDto)
     * * 3. getFileList     : 특정 게시글(참조타입+번호)에 속한 활성화된 파일 목록 조회. 정렬 순서 반영.
     * (인자: RefDto / 반환: List<FileDto>)
     * * 4. getFile         : 파일 단건 정보 조회 (기본 정보 위주).
     * (인자: Long fileId / 반환: FileDto)
     * * 5. deleteFile      : UUID 기반 파일 단건 논리 삭제(del_yn=true) 및 물리 파일 삭제.
     * (인자: String fileUuid / 반환: void)
     * * 6. deleteFileById  : ID 기반 파일 단건 논리 삭제(del_yn=true) 및 물리 파일 삭제.
     * (인자: Long fileId / 반환: void)
     * * 7. deleteFilesByRef: 게시글 삭제 시 사용. 참조 타입/번호를 가진 모든 파일을 일괄 삭제.
     * (인자: RefDto / 반환: void)
     * * 8. deleteFilesById : ID 리스트 기반 일괄 삭제. 게시글 수정 시 선택된 파일들을 제거할 때 사용.
     * (인자: List<Long> fileIdList / 반환: void)
     * * 9. deleteFiles     : UUID 리스트 기반 일괄 삭제.
     * (인자: List<String> fileUuidList / 반환: void)
     * * 10. getFileCount   : 특정 게시글에 첨부된 활성화된 파일의 총 개수 반환.
     * (인자: RefDto / 반환: int)
     * * ※ 주의: 게시글 수정 시에는 deleteFiles(또는 deleteFilesById)와 saveFile을 순차적으로 호출하며,
     * 트랜잭션 관리는 호출부(상위 Service)에서 수행함을 원칙으로 함.
     */

    // 파일 저장 요청
    public List<FileDto> saveFile(List<MultipartFile> files, RefDto refDto) throws IOException {
        List<FileDto> fileListDtoList = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);


            if (file.isEmpty() || file.getOriginalFilename().isEmpty()) {
                continue;
            }

            String mimeType = getMimeType(file);
            validateFilePolicy(file); // 파일업로드 화이트리스트 정책

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".") + 1);
            }
            String uuid = UUID.randomUUID().toString();
            String datePath = getDatePath();

            if (originalName == null || originalName.isEmpty()) {
                throw new IllegalIdentifierException("파일 이름이 없습니다.");
            }

            String mimeType = getMimeType(file);
            uploadToDisk(file, uuid, ext);

            // FileListDto 생성 및 추가
            fileListDtoList.add(FileDto.builder()
                    .fileUuid(uuid)
                    .fileNm(originalName)
                    .fileExt(ext)
                    .fileSz(file.getSize())
                    .filePat(datePath)
                    .build());

            FileUpload fileUpload = FileUpload.builder()
                    .fileNm(originalName)
                    .fileUuid(uuid)
                    .brdRefTy(refDto.getRefTy())
                    .brdRefNo(refDto.getRefNo())
                    .fileSz(file.getSize())
                    .fileExt(ext)
                    .fileCtp(mimeType)
                    .fileOr(i + 1)
                    .filePat(datePath)
                    .fileDelYn(false)
                    .build();

            fileRepository.save(fileUpload);
        }
        return fileListDtoList;
    }
    // 물리파일 다운로드 서비스
    public FileDownloadDto prepareDownload(String fileUuId) {
        // 1. DB에서 파일 정보 조회 (없으면 예외 발생)
        FileUpload fileUpload = fileRepository.findFileUploadByFileUuid(fileUuId)
                .orElseThrow(() -> new RuntimeException("해당 파일 기록을 찾을 수 없습니다. ID: " + fileUuId));

        // 2. 경로 복원 (uploadPath + DB의 filePath + UUID.ext)
        // Paths.get을 사용하면 OS별 구분자(\ 또는 /) 문제를 알아서 해결해줍니다.
        Path fullPath = Paths.get(uploadPath ,fileUpload.getFilePat(),
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
                        .filePat(file.getFilePat())
                        .build())
                .collect(Collectors.toList());
    }
    // 파일 단건 조회
    public FileDto getFile(Long fileId) {
        return fileRepository.findById(fileId).map(fileUpload -> FileDto.builder()
                .fileId(fileUpload.getFileId())
                .fileNm(fileUpload.getFileNm())
                .fileSz(fileUpload.getFileSz())
                .filePat(fileUpload.getFilePat())
                .fileCtp(fileUpload.getFileCtp())
                .build()).orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다."));
    }

    // 파일 단건 삭제
    public void deleteFile(String fileUuid) {
        FileUpload fileUpload = fileRepository.findFileUploadByFileUuid(fileUuid)
                .orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다."));

        fileUpload.setFileDelYn(true);
        deletePhysicalFile(fileUpload.getFilePat(), fileUpload.getFileUuid(), fileUpload.getFileExt());
    }

    // 파일 단건 삭제
    public void deleteFileById(Long fileId) {
        FileUpload fileUpload = fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다."));

        fileUpload.setFileDelYn(true);
        deletePhysicalFile(fileUpload.getFilePat(), fileUpload.getFileUuid(), fileUpload.getFileExt());
    }

    // 파일 일괄 삭제(참조버전)
    public void deleteFilesByRef(RefDto refDto) {
        List<Long> fileIdList = fileRepository.findAllByBrdRefTyAndBrdRefNoAndFileDelYnFalse(
                        refDto.getRefTy(), refDto.getRefNo())
                .stream()
                .map(FileUpload::getFileId)
                .toList();

        if (fileIdList.isEmpty()) {
            return;
        }
        deleteFilesById(fileIdList);
    }


    public void deleteFilesById(List<Long> fileIdList) {
        if (fileIdList == null || fileIdList.isEmpty()) return;

        for (int i = 0; i < fileIdList.size(); i++) {
            Long fileid = fileIdList.get(i);
            deleteFileById(fileid);
        }
    }

    // 파일 일괄 삭제(아이디리스트)
    public void deleteFiles(List<String> fileUuidList) {
        if (fileUuidList == null || fileUuidList.isEmpty()) return;

        for (int i = 0; i < fileUuidList.size(); i++) {
            String fileUuid = fileUuidList.get(i);
            deleteFile(fileUuid);
        }
    }

    // 물리파일 저장
    private String uploadToDisk(MultipartFile file, String uuid, String ext) throws IOException {
        Path fullPath = Paths.get(uploadPath, getDatePath(), uuid + "." + ext);
        file.transferTo(fullPath.toFile());
        return fullPath.toString();
    }

    // 물리 파일 삭제
    private void deletePhysicalFile(String path, String uuid, String ext) {
        if (path == null || uuid == null || ext == null) {
            return;
        }

        try {
            Path filePath = Paths.get(uploadPath, path, uuid + "." + ext);
            Files.deleteIfExists(filePath); // 파일이 있으면 삭제, 없으면 무시
            log.info("파일 삭제 성공: {}", filePath);
        } catch (IOException e) {
            log.error("물리 파일 삭제 실패: {}", e.getMessage());
        }
    }

    // 파일 날짜경로 생성 메소드
    private String getDatePath() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("MM"));
        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("dd"));

        // 2. Paths.get이 OS에 맞는 구분자(\ 또는 /)를 알아서 넣어줌
        Path datePath = Paths.get(year, month, day);

        File uploadDir = Paths.get(uploadPath, datePath.toString()).toFile();
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // 폴더가 없으면 생성
        }
        return datePath.toString();
    }

    // MIME 타입 조회 메소드
    public String getMimeType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream());
        } catch (IOException e) {
            log.warn("MIME 타입 추출 실패, 파일명 기반 추측 시도: {}", e.getMessage());
            // 스트림 읽기 실패 시 확장자로라도 추측
            return "application/octet-stream";
        }
    }
    // 업로드 파일 화이트리스트 검사
    public void validateFilePolicy(MultipartFile file) {
        SecurityPolicyDto policy = securityService.getCachedPolicy();
        // 0. 정책 객체가 로드되지 않았을 경우에 대한 방어 로직
        if (policy == null) {
            return; // 혹은 기본 보안 정책 적용
        }

        // 1. 파일 업로드 허용 리스트 정책이 켜져 있는지 확인
        if (Boolean.TRUE.equals(policy.getIsFileUploadAllowListEnabled())) {

            String originalName = file.getOriginalFilename();
            if (originalName == null || !originalName.contains(".")) {
                throw new IllegalArgumentException("올바르지 않은 파일명입니다.");
            }

            // 2. 확장자 추출 및 소문자 변환 (비교 규격 통일)
            String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase().trim();

            // 3. 화이트리스트 확인
            // DTO 내부의 setFileUploadAllowList에 의해 이미 Set<String>으로 변환된 상태임
            // 주의: getFileUploadAllowList()는 String을 반환하므로,
            // 필드(Set)에 직접 접근하거나 별도의 전용 Getter를 사용하는 것이 성능상 유리합니다.

            Set<String> allowSet = policy.getFileUploadAllowSet(); // (아래 DTO 수정 참고)

            if (allowSet != null && !allowSet.isEmpty()) {
                if (!allowSet.contains(ext)) {
                    throw new SecurityException("허용되지 않는 파일 확장자입니다: " + ext);
                }
            }
        }
    }

    public Boolean isFileYn(RefDto refDto) {
        if (refDto == null || refDto.getRefNo() == null) return false;
        return fileRepository.existsByRefDto(refDto);
    }
}
