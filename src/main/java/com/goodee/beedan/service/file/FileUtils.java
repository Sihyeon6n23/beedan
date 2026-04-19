package com.goodee.beedan.service.file;

import com.goodee.beedan.dto.board.notice.BoardResultMessage;
import com.goodee.beedan.dto.file.FileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUtils {
    public BoardResultMessage buildBoardResultMessage(List<FileDto> fileResults) {
        BoardResultMessage resultMessage = new BoardResultMessage();
        List<String> failReasons = new ArrayList<>();

        long successCount = 0;
        long failCount = 0;
        long deleteCount = 0;

        for (FileDto fileDto : fileResults) {
            if (fileDto.isDeleted()) { // 삭제 성공
                deleteCount++;
            } else if (fileDto.getErrorMessage() != null && fileDto.getErrorMessage().contains("삭제")) { // 삭제 실패
                failCount++;
                failReasons.add(fileDto.getFileNm() + " : " + fileDto.getErrorMessage());
            } else if (fileDto.isUploaded()) { // 업로드 성공
                successCount++;
            } else { // 업로드 실패
                failCount++;
                failReasons.add(fileDto.getFileNm() + " : " + fileDto.getErrorMessage());
            }
        }

        resultMessage.setDeleteSuccess(deleteCount);
        resultMessage.setUploadSuccess(successCount);
        resultMessage.setFail(failCount);
        resultMessage.setFailReason(failReasons);

        return resultMessage;
    }

    public String generateBoardResultMessage(BoardResultMessage result, Boolean isEdit) {
        if (result == null) return "처리 결과 데이터가 없습니다.";

        StringBuilder sb = new StringBuilder();

        // 모든 작업 카운트 합산
        long totalActionCount = result.getDeleteSuccess() +
                result.getUploadSuccess() + result.getFail();

        // 파일 변경 사항이 전혀 없다면 null 반환
        if (totalActionCount == 0) {
            return null;
        }

        // 1. 제목 결정 (삭제 건수가 있으면 '수정', 없으면 '작성')
        String title = (isEdit == true)
                ? "🔄 게시글 수정 결과"
                : "📝 게시글 작성 결과";
        sb.append(title).append("\n");
        sb.append("----------------------------\n");

        // 2. 항목별 출력 (0건이 아닌 경우에만 출력)

        // 삭제 결과 (0보다 클 때만)
        if (result.getDeleteSuccess() != null && result.getDeleteSuccess() > 0) {
            sb.append(String.format("🗑️ 기존 파일 삭제: %d건\n", result.getDeleteSuccess()));
        }

        // 업로드 성공 결과 (0보다 클 때만)
        if (result.getUploadSuccess() != null && result.getUploadSuccess() > 0) {
            sb.append(String.format("✅ 파일 업로드 성공: %d건\n", result.getUploadSuccess()));
        }

        // 업로드 실패 결과 (0보다 클 때만)
        if (result.getFail() != null && result.getFail() > 0) {
            sb.append(String.format("❌ 파일 처리 실패: %d건\n", result.getFail()));

            // 상세 사유 출력
            if (result.getFailReason() != null && !result.getFailReason().isEmpty()) {
                sb.append("\n⚠️ 실패 상세 내역:\n");
                for (String reason : result.getFailReason()) {
                    sb.append(String.format("• %s\n", reason));
                }
            }
        }

        return sb.toString();
    }
    public long parseSize(String size) {
        size = size.toUpperCase();
        if (size.endsWith("MB")) {
            return Long.parseLong(size.replace("MB", "")) * 1024 * 1024;
        } else if (size.endsWith("KB")) {
            return Long.parseLong(size.replace("KB", "")) * 1024;
        }
        return Long.parseLong(size);
    }
}
