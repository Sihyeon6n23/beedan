package com.goodee.beedan.dto.board.inquiry;

import com.goodee.beedan.dto.file.FileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 관리자 답변 작성/수정용
public class InquiryReplySaveDto {
    private String brdCon;

    private List<MultipartFile> newFiles;   // 답글 작성/수정 시 새로 올린 첨부파일 목록
    private List<String> deleteUuids;       // 답글 수정 시 삭제할 기존 첨부파일 UUID 목록
    private List<FileDto> existingFiles;    // 답글 수정 화면에 있는 기존 첨부파일 목록
}
