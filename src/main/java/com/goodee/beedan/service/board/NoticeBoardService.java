package com.goodee.beedan.service.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.dto.board.notice.NoticeBoardRequestDto;
import com.goodee.beedan.dto.board.notice.NoticeDetailDto;
import com.goodee.beedan.dto.board.notice.NoticeListDto;
import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.entity.Board;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.mapper.board.NoticeMapper;
import com.goodee.beedan.repository.board.BoardRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.service.file.FileService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NoticeBoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final BoardCoreService boardCoreService;
    private final NoticeMapper noticeMapper;

    /** 1. 공지사항 작성 */
    public void writeNotice(NoticeBoardRequestDto dto, String username) throws IOException {
        Member member = getMemberByUsername(username);

        // 권한 체크: ADMIN, ROOT만 작성 가능
        if (member.getMemAut() == MemberAuthority.USER) {
            throw new SecurityException("공지사항 작성 권한이 없습니다.");
        }

        Board board = Board.builder()
                .brdTy(BoardType.NOTICE)
                .brdTtl(dto.getBrdTtl())
                .brdCon(dto.getBrdCon())
                .brdFixYn(dto.getBrdFixYn())
                .member(member)
                .brdVstCnt(0L)
                .brdDelYn(false)
                .build();

        boardRepository.save(board);

        // 파일 저장 (생성된 ID 사용)
        handleFiles(dto, board.getBrdId());
    }

    /** 2. 공지사항 수정 */
    public void updateNotice(NoticeBoardRequestDto dto, String username) throws IOException {
        Board board = boardCoreService.getBoard(dto.getBrdId(), BoardType.NOTICE);

        // 권한 검증 (실패 시 예외 발생)
        checkModifyAuthority(board, username);

        // 내용 수정
        board.updateInquiry(dto.getBrdTtl(), dto.getBrdCon());
        board.setBrdFixYn(dto.getBrdFixYn());

        // 파일 처리 (삭제 후 신규 저장)
        if (dto.getDeleteUuids() != null && !dto.getDeleteUuids().isEmpty()) {
            fileService.deleteFiles(dto.getDeleteUuids());
        }
        handleFiles(dto, board.getBrdId());
    }

    /** 3. 공지사항 삭제 */
    public void deleteNotice(Long brdId, String username) {
        Board board = boardCoreService.getBoard(brdId, BoardType.NOTICE);

        checkModifyAuthority(board, username);

        board.markAsDeleted();
        fileService.deleteFilesByRef(RefDto.builder()
                .refTy(BoardType.NOTICE.name())
                .refNo(brdId).build());

        log.info("공지사항 삭제 완료: ID={}, 삭제자={}", brdId, username);
    }

    /** 4. 상세 조회 (이전/다음글 포함) */
    public NoticeDetailDto getNoticeDetail(Long brdId, String currentUsername) {
        Board board = boardCoreService.getBoard(brdId, BoardType.NOTICE);

        // 이전글/다음글 조회
        NoticeDetailDto.NeighborNotice prev = boardRepository.findFirstByBrdIdLessThanAndBrdTyAndBrdDelYnFalseOrderByBrdIdDesc(brdId, BoardType.NOTICE)
                .map(b -> new NoticeDetailDto.NeighborNotice(b.getBrdId(), b.getBrdTtl()))
                .orElse(null);

        NoticeDetailDto.NeighborNotice next = boardRepository.findFirstByBrdIdGreaterThanAndBrdTyAndBrdDelYnFalseOrderByBrdIdAsc(brdId, BoardType.NOTICE)
                .map(b -> new NoticeDetailDto.NeighborNotice(b.getBrdId(), b.getBrdTtl()))
                .orElse(null);

        // 파일 목록 조회
        List<FileDto> fileList = fileService.getFileList(RefDto.builder().refTy("NOTICE").refNo(brdId).build());

        // 수정 권한 여부 (화면 버튼 노출용)
        boolean canModify = false;
        try {
            canModify = checkModifyAuthority(board, currentUsername);
        } catch (Exception e) {
            canModify = false;
        }

        return NoticeDetailDto.builder()
                .brdId(board.getBrdId())
                .brdTtl(board.getBrdTtl())
                .brdCon(board.getBrdCon())
                .brdVstCnt(board.getBrdVstCnt())
                .brdFixYn(board.getBrdFixYn())
                .brdCreDt(board.getBrdCreDt())
                .memNm(board.getMember().getMemNm())
                .canModify(canModify)
                .fileList(fileList)
                .prevNotice(prev)
                .nextNotice(next)
                .build();
    }

    /** 5. 목록 조회 (고정글 + 일반글 페이징) */
    public Map<String, Object> getNoticeList(Pageable pageable) {
        Map<String, Object> result = new HashMap<>();

        // 1. 고정 공지 조회 (첫 페이지에서만 노출하고 싶을 경우 조건 추가)
        if (pageable.getPageNumber() == 0) {
            List<Board> fixedEntities = boardRepository.findByBrdTyAndBrdFixYnTrueAndBrdDelYnFalseOrderByBrdCreDtDesc(BoardType.NOTICE);

            List<NoticeListDto> fixedDtoList = fixedEntities.stream()
                    .map(board -> {
                        NoticeListDto dto = noticeMapper.toListDto(board);
                        // 파일 유무
                        RefDto ref = RefDto.builder()
                                .refTy(BoardType.NOTICE.name())
                                .refNo(board.getBrdId())
                                .build();
                        dto.setFileYn(fileService.isFileYn(ref));
                        return dto;
                    })
                    .collect(Collectors.toList());

            result.put("fixedNotices", fixedDtoList);
        } else {
            // 첫 페이지가 아니면 빈 리스트 반환 (Null 방지)
            result.put("fixedNotices", new ArrayList<NoticeListDto>());
        }

        // 2. 일반 공지 조회
        Page<Board> noticePage = boardRepository.findByBrdTyAndBrdFixYnFalseAndBrdDelYnFalseOrderByBrdCreDtDesc(BoardType.NOTICE, pageable);

        // 3. 일반 공지 DTO 변환 및 파일 유무 세팅
        List<NoticeListDto> dtoList = noticePage.getContent().stream()
                .map(board -> {
                    NoticeListDto dto = noticeMapper.toListDto(board);
                    RefDto ref = RefDto.builder()
                            .refTy(BoardType.NOTICE.name())
                            .refNo(board.getBrdId())
                            .build();

                    dto.setFileYn(fileService.isFileYn(ref));
                    return dto;
                })
                .collect(Collectors.toList());

        // 페이징 객체로 감싸서 반환
        result.put("notices", new PageImpl<>(dtoList, pageable, noticePage.getTotalElements()));

        return result;
    }

    // --- Private Helper Methods ---

    /** 공통 권한 체크 문지기 */
    private boolean checkModifyAuthority(Board board, String username) {
        if (username == null || username.isBlank()) throw new SecurityException("로그인이 필요합니다.");

        Member requestMem = getMemberByUsername(username);
        MemberAuthority auth = requestMem.getMemAut();

        if (auth == MemberAuthority.ROOT) return true;
        if (auth == MemberAuthority.ADMIN && board.getMember().getMemLgnId().equals(username)) return true;

        throw new SecurityException("해당 게시글에 대한 권한이 없습니다.");
    }

    /** 사용자 조회 공통 로직 */
    private Member getMemberByUsername(String username) {
        return memberRepository.findByMemLgnId(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
    }

    /** 파일 저장 공통 로직 */
    private void handleFiles(NoticeBoardRequestDto dto, Long brdId) throws IOException {
        if (dto.getNewFiles() != null && !dto.getNewFiles().isEmpty()) {
            fileService.saveFile(dto.getNewFiles(), RefDto.builder()
                    .refTy(BoardType.NOTICE.name())
                    .refNo(brdId).build());
        }
    }
}