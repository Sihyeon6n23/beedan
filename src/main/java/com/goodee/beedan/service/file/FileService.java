package com.goodee.beedan.service.file;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.dto.board.notice.BoardResultMessage;
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

import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    private static final MimeTypes TIKA_MIME_TYPES = MimeTypes.getDefaultMimeTypes();

    private static final Map<String, Set<String>> MIME_EXTENSION_MAP = new HashMap<>();
    static {
        MIME_EXTENSION_MAP.put("application/x-tika-ooxml",
                new HashSet<>(Arrays.asList(".xlsx", ".docx", ".pptx", ".xlsm", ".docm", ".pptm", ".ppsx")));
        MIME_EXTENSION_MAP.put("application/zip",
                new HashSet<>(Arrays.asList(".xlsx", ".xltx", ".docx", ".dotx", ".pptx", ".ppsx", ".hwpx", ".pdf", ".zip")));

        Set<String> excelExts = new HashSet<>(Arrays.asList(".xls", ".xlsx", ".xml", ".csv", ".xlsm"));
        MIME_EXTENSION_MAP.put("application/vnd.ms-spreadsheetml", excelExts);
        MIME_EXTENSION_MAP.put("application/vnd.ms-excel", excelExts);
        MIME_EXTENSION_MAP.put("application/msexcel", excelExts);

        Set<String> wordExts = new HashSet<>(Arrays.asList(".doc", ".docx", ".dot", ".dotx"));
        MIME_EXTENSION_MAP.put("application/msword", wordExts);
        MIME_EXTENSION_MAP.put("application/vnd.ms-word", wordExts);

        Set<String> pptExts = new HashSet<>(Arrays.asList(".ppt", ".pptx", ".pps", ".ppsx"));
        MIME_EXTENSION_MAP.put("application/vnd.ms-powerpoint", pptExts);
        MIME_EXTENSION_MAP.put("application/powerpoint", pptExts);

        MIME_EXTENSION_MAP.put("application/octet-stream",
                new HashSet<>(Arrays.asList(".xlsx", ".docx", ".pptx", ".pdf", ".zip")));
    }

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

            String originalName = file.getOriginalFilename();
            String mimeType = getMimeType(file);
            long fileSize = file.getSize();

            if (originalName == null || originalName.isEmpty()) {
                throw new IllegalIdentifierException("파일 이름이 없습니다.");
            }

            String errorMsg = null;
            String ext = "";

            int lastDotIndex = originalName.lastIndexOf(".");
            if (lastDotIndex == -1) {
                errorMsg = "확장자가 존재하지 않는 파일은 업로드 불가능합니다.";
            } else {
                ext = originalName.substring(lastDotIndex + 1);

                // 3. 파일 정책 검증 (화이트리스트 등)
                if (!isValidateFilePolicy(originalName)) {
                    errorMsg = "업로드 불가능한 확장자입니다.";
                }
                // 4. 마임타입 위변조 검증
                else if (!isMimeExtensionMatched(mimeType, ext)) {
                    errorMsg = "파일의 데이터 규격이 확장자 정보와 다릅니다. 원본 파일을 확인해 주세요.";
                }
            }

            if (errorMsg != null) {
                fileListDtoList.add(FileDto.builder()
                        .fileNm(originalName)
                        .fileExt(ext)
                        .fileSz(fileSize)
                        .isUploaded(false)
                        .errorMessage(String.format("[%s] 업로드 실패: %s", originalName, errorMsg))
                        .build());
                continue;
            }

            // 6. 모든 검증 통과 시 저장 진행
            // 저장시 파일명에서 확장자 .을 제외한 나머지 .은 _처리 후 업로드
            String uuid = UUID.randomUUID().toString();
            String datePath = getDatePath();
            uploadToDisk(file, uuid, ext);

            fileListDtoList.add(FileDto.builder()
                    .fileUuid(uuid)
                    .fileNm(sanitizeFileName(originalName)) // 아까 만든 마침표 정화 로직 적용 추천
                    .fileExt(ext)
                    .fileSz(fileSize)
                    .filePat(datePath)
                    .isUploaded(true)
                    .isDeleted(false)
                    .build());

            FileUpload fileUpload = FileUpload.builder()
                    .fileNm(sanitizeFileName(originalName))
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
    public FileDto deleteFile(String fileUuid) {
        FileUpload fileUpload = fileRepository.findFileUploadByFileUuid(fileUuid)
                .orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다."));

        fileUpload.setFileDelYn(true);
        boolean isDeleted = deletePhysicalFile(
                fileUpload.getFilePat(),
                fileUpload.getFileUuid(),
                fileUpload.getFileExt()
        );

        // 3. 결과를 DTO에 담아 반환
        return FileDto.builder()
                .fileUuid(fileUuid)
                .fileNm(fileUpload.getFileNm())
                .isDeleted(isDeleted)
                .isUploaded(false)
                .errorMessage(isDeleted ? "" : fileUpload.getFileNm() + "파일 삭제를 실패했습니다.(잠금 또는 권한)")
                .build();
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
    public void deleteFileById(Long fileId) {
        FileUpload fileUpload = fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다."));

        fileUpload.setFileDelYn(true);
        deletePhysicalFile(fileUpload.getFilePat(), fileUpload.getFileUuid(), fileUpload.getFileExt());
    }

    // 파일 다중 삭제(아이디리스트)
    public List<FileDto> deleteFiles(List<String> fileUuidList) {
        if (fileUuidList == null || fileUuidList.isEmpty()) return new ArrayList<>();

        List<FileDto> fileDtoList = new ArrayList<>();
        for (String fileUuid : fileUuidList) {
            fileDtoList.add(deleteFile(fileUuid));
        }
        return fileDtoList;
    }

    // 물리파일 저장
    private String uploadToDisk(MultipartFile file, String uuid, String ext) throws IOException {
        Path fullPath = Paths.get(uploadPath, getDatePath(), uuid + "." + ext);
        file.transferTo(fullPath.toFile());
        return fullPath.toString();
    }

    // 물리 파일 삭제
    private boolean deletePhysicalFile(String path, String uuid, String ext) {
        if (path == null || uuid == null || ext == null) {
            return false;
        }

        try {
            Path filePath = Paths.get(uploadPath, path, uuid + "." + ext);
            Files.deleteIfExists(filePath); // 파일이 있으면 삭제, 없으면 무시
            log.info("파일 삭제 성공: {}", filePath);
        } catch (IOException e) {
            log.error("물리 파일 삭제 실패: {}", e.getMessage());
        }
        return true;
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

    // MIME 타입 인자, 업로드 가능여부 반환
    public boolean isMimeExtensionMatched(String detectedMime, String extension) {
        try {
            // 1. MIME 타입 문자열을 통해 Tika의 MimeType 객체 획득
            MimeType mimeType = TIKA_MIME_TYPES.forName(detectedMime);

            // 2. 해당 MIME 타입이 허용하는 모든 확장자 리스트 가져오기
            List<String> extensionList = mimeType.getExtensions();
            // 리스트가 비어있거나, application/zip 인 경우 정해진 리스트 반환 및 SET으로 변경
            Set<String> validExtensions = getFallbackExtensions(mimeType.toString(), extensionList);


            log.info("추출된마임타입: {} ,검증할 확장자 목록 크기: {}", mimeType.toString(),validExtensions.size());
            validExtensions.forEach(f -> log.info("{}",f));

            // 3. 사용자가 보낸 확장자에 점(.)이 없다면 추가하여 비교 (Tika는 .jpg 형태를 반환)
            String extensionWithDot = extension.startsWith(".") ? extension.toLowerCase() : "." + extension.toLowerCase();

            // 4. 매핑 테이블 내에 존재 여부 확인 (Containment Check)
            return validExtensions.contains(extensionWithDot);

        } catch (MimeTypeException e) {
            // 정의되지 않은 MIME 타입일 경우 보안상 false 반환 (Fail-Close)
            return false;
        }
    }

    // 업로드 파일 화이트리스트 검사
    public boolean isValidateFilePolicy(String filename) {
        SecurityPolicyDto policy = securityService.getCachedPolicy();
        // 0. 정책 객체가 로드되지 않았을 경우에 대한 방어 로직
        if (policy == null) {
            return true; // 혹은 기본 보안 정책 적용
        }

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex <= 0 || lastDotIndex == filename.length() - 1) {
            return false;
        }

        // 1. 파일 업로드 허용 리스트 정책이 켜져 있는지 확인
        if (Boolean.TRUE.equals(policy.getIsFileUploadAllowListEnabled())) {
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase().trim();

            Set<String> allowSet = policy.getFileUploadAllowSet();

            if (allowSet != null && !allowSet.isEmpty()) {
                return allowSet.contains(ext);
            }
        }
        return true;
    }

    public Set<Long> getFileYnSet(String Type, List<Long> RefNos) {
        return new HashSet<>(
                fileRepository.findExistingRefNos(Type, RefNos)
        );
    }

    public Set<String> getFallbackExtensions(String mimeType, List<String> existingExtensions) {
        Set<String> validExtensions = new HashSet<>();

        // 1. 기존 리스트가 있다면 일단 추가 (Tika가 기본적으로 찾은 것들)
        if (existingExtensions != null && !existingExtensions.isEmpty()) {
            validExtensions.addAll(existingExtensions);
        }

        // 2. 리스트가 비어있거나, 'application/zip' 등 확장자 보충이 필요한 특정 마임타입인 경우
        // MIME_EXTENSION_MAP에서 우리가 정의한 "정해진 매칭" 확장자들을 추가함
        if (validExtensions.isEmpty() || "application/zip".equals(mimeType) || "application/octet-stream".equals(mimeType)) {
            Set<String> customMatch = MIME_EXTENSION_MAP.get(mimeType);
            if (customMatch != null) {
                validExtensions.addAll(customMatch);
            }
        }

        return validExtensions;
    }

    public String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }

        int lastDotIndex = fileName.lastIndexOf(".");

        if (lastDotIndex <= 0) {
            return fileName;
        }

        String namePart = fileName.substring(0, lastDotIndex);
        String extensionPart = fileName.substring(lastDotIndex); // .pdf 포함

        // 이름 부분의 모든 마침표를 언더바로 치환 후 결합
        return namePart.replace(".", "_") + extensionPart;
    }
}
