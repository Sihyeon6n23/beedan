package com.goodee.beedan.service.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.dto.board.inquiry.*;
import com.goodee.beedan.dto.board.notice.BoardResultResponseDto;
import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.entity.Board;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.board.BoardRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.service.file.FileService;
import com.goodee.beedan.service.file.FileUtils;
import com.goodee.beedan.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryBoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final FileService fileService;
    private final FileUtils fileUtils;

    // 사용자 목록 조회
    public Page<InquiryBoardListDto> getUserInquiryBoards(Long memId, InquiryBoardSearchDto searchDto) {
        Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());

        Page<Board> userInquiryBoards = boardRepository.findUserInquiryBoards(
                BoardType.INQUIRY,
                memId,
                searchDto.getBrdInqStt(),
                searchDto.getKeyword(),
                pageable
        );

        // 현재 페이지의 문의 ID들만 모아서 답변을 한 번에 조회
        List<Long> inquiryBoardIds = userInquiryBoards.getContent().stream()
                .map(Board::getBrdId)
                .toList();

        // 부모 문의 ID -> 답변 Board 로 매핑해서 목록 변환 시 재사용
        Map<Long, Board> replyBoardMap = boardRepository
                .findByBrdPrnIdInAndBrdTyAndBrdDelYnFalse(inquiryBoardIds, BoardType.INQUIRY_ANSWER)
                .stream()
                .collect(Collectors.toMap(Board::getBrdPrnId, reply -> reply));

        return userInquiryBoards.map(inquiryBoard -> mapToInquiryBoardListDto(inquiryBoard, replyBoardMap));
    }

    // 관리자 목록 조회
    public Page<InquiryBoardListDto> getAdminInquiryBoards(Long memAdId, InquiryBoardSearchDto searchDto) {
        validateAdminAuthority(memAdId);

        Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());
        Page<Board> adminInquiryBoards;

        if (Boolean.TRUE.equals(searchDto.getMyAnsweredOnly())) {
            // 관리자 본인이 답글을 작성한 문의 목록
            adminInquiryBoards = boardRepository.findInquiryBoardsAnsweredByAdmin(
                    BoardType.INQUIRY,
                    BoardType.INQUIRY_ANSWER,
                    memAdId,
                    searchDto.getBrdInqStt(),
                    searchDto.getKeyword(),
                    pageable
            );
        } else {
            // 관리자 전체 문의 목록
            adminInquiryBoards = boardRepository.findAdminInquiryBoards(
                    BoardType.INQUIRY,
                    searchDto.getBrdInqStt(),
                    searchDto.getKeyword(),
                    pageable
            );
        }

        // 현재 페이지의 문의 ID들만 모아서 답변을 한 번에 조회
        List<Long> inquiryBoardIds = adminInquiryBoards.getContent().stream()
                .map(Board::getBrdId)
                .toList();

        // 부모 문의 ID -> 답변 Board 로 매핑해서 목록 변환 시 재사용
        Map<Long, Board> replyBoardMap = boardRepository
                .findByBrdPrnIdInAndBrdTyAndBrdDelYnFalse(inquiryBoardIds, BoardType.INQUIRY_ANSWER)
                .stream()
                .collect(Collectors.toMap(Board::getBrdPrnId, reply -> reply));

        return adminInquiryBoards.map(inquiryBoard -> mapToInquiryBoardListDto(inquiryBoard, replyBoardMap));
    }

    private InquiryBoardListDto mapToInquiryBoardListDto(Board inquiryBoard, Map<Long, Board> replyBoardMap) {
        // BoardRepository에서 member를 JOIN FETCH로 같이 읽어왔으므로
        // 목록 변환 시에는 board.getMember()를 바로 사용하고 회원을 다시 조회하지 않음
        Member member = inquiryBoard.getMember();

        // 배치 조회해 둔 답변 맵에서 현재 문의의 답변을 꺼내서 사용
        Board replyBoard = replyBoardMap.get(inquiryBoard.getBrdId());

        boolean hasReply = replyBoard != null;
        boolean replyEdited = replyBoard != null
                && replyBoard.getBrdUpdDt() != null
                && !replyBoard.getBrdUpdDt().equals(replyBoard.getBrdCreDt());

        return InquiryBoardListDto.builder()
                .brdId(inquiryBoard.getBrdId())
                .brdTtl(inquiryBoard.getBrdTtl())
                .brdInqStt(inquiryBoard.getBrdInqStt())
                .brdCreDt(inquiryBoard.getBrdCreDt())
                .memBizTtl(member.getMemBizTtl())
                .memNm(member.getMemNm())
                .hasReply(hasReply)
                .replyEdited(replyEdited)
                .build();
    }

    // 사용자 상세 조회
    public InquiryBoardDetailDto getUserInquiryBoardDetail(Long brdId, Long memId) {
        // 본인이 작성한 문의 단일 조회
        Board inquiryBoard = boardRepository
                .findByBrdIdAndBrdTyAndMember_MemIdAndBrdDelYnFalse(brdId, BoardType.INQUIRY, memId)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 문의글입니다."));
        // 문의 작성자 정보 조회
        Member member = memberRepository.findById(inquiryBoard.getMember().getMemId())
                .orElseThrow(() -> new IllegalArgumentException("문의 작성자 정보를 찾을 수 없습니다."));
        // 문의 답글 단일 조회
        Optional<Board> replyBoard = boardRepository
                .findByBrdPrnIdAndBrdTyAndBrdDelYnFalse(brdId, BoardType.INQUIRY_ANSWER);

        InquiryReplyDto replyDto = replyBoard.map(this::mapToInquiryReplyDto)
                .orElse(null);

        // 해당 문의의 첨부 파일 목록 조회
        List<FileDto> fileList = fileService.getFileList(
                RefDto.builder()
                        .refTy(BoardType.INQUIRY.name())
                        .refNo(brdId)
                        .build()
        );

        return InquiryBoardDetailDto.builder()
                .brdId(inquiryBoard.getBrdId())
                .brdTtl(inquiryBoard.getBrdTtl())
                .brdCon(inquiryBoard.getBrdCon())
                .brdInqStt(inquiryBoard.getBrdInqStt())
                .brdCreDt(inquiryBoard.getBrdCreDt())
                .memBizTtl(member.getMemBizTtl())
                .memNm(member.getMemNm())
                .brdCanRe(inquiryBoard.getBrdCanRe())
                .canEdit(inquiryBoard.getBrdInqStt() == InquiryStatus.RECEIVED)
                .canCancel(inquiryBoard.getBrdInqStt() == InquiryStatus.RECEIVED)
                .canAnswer(false)
                .canUpdateStatus(false)
                .canEditReply(false)
                .reply(replyDto)
                .fileList(fileList)
                .build();
    }

    // 관리자 상세 조회
    public InquiryBoardDetailDto getAdminInquiryBoardDetail(Long brdId, Long memAdId) {
        validateAdminAuthority(memAdId);

        // 문의 단일 조회
        Board inquiryBoard = boardRepository
                .findByBrdIdAndBrdTyAndBrdDelYnFalse(brdId, BoardType.INQUIRY)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 문의글입니다."));
        // 문의 작성자 정보 조회
        Member member = memberRepository.findById(inquiryBoard.getMember().getMemId())
                .orElseThrow(() -> new IllegalArgumentException("문의 작성자 정보를 찾을 수 없습니다."));
        // 문의 답글 단일 조회
        Optional<Board> replyBoard = boardRepository
                .findByBrdPrnIdAndBrdTyAndBrdDelYnFalse(brdId, BoardType.INQUIRY_ANSWER);

        InquiryReplyDto replyDto = replyBoard.map(this::mapToInquiryReplyDto)
                .orElse(null);
        boolean canEditReply = replyBoard.map(reply -> reply.getMember().getMemId().equals(memAdId))
                .orElse(false);

        // 해당 문의의 첨부 파일 목록 조회
        List<FileDto> fileList = fileService.getFileList(
                RefDto.builder()
                        .refTy(BoardType.INQUIRY.name())
                        .refNo(brdId)
                        .build()
        );

        return InquiryBoardDetailDto.builder()
                .brdId(inquiryBoard.getBrdId())
                .brdTtl(inquiryBoard.getBrdTtl())
                .brdCon(inquiryBoard.getBrdCon())
                .brdInqStt(inquiryBoard.getBrdInqStt())
                .brdCreDt(inquiryBoard.getBrdCreDt())
                .memBizTtl(member.getMemBizTtl())
                .memNm(member.getMemNm())
                .brdCanRe(inquiryBoard.getBrdCanRe())
                .canEdit(false)
                .canCancel(inquiryBoard.getBrdInqStt() == InquiryStatus.RECEIVED)
                .canAnswer(inquiryBoard.getBrdInqStt() == InquiryStatus.IN_PROGRESS && replyDto == null)
                .canUpdateStatus(inquiryBoard.getBrdInqStt() == InquiryStatus.RECEIVED)
                .canEditReply(canEditReply)
                .reply(replyDto)
                .fileList(fileList)
                .build();
    }

    private InquiryReplyDto mapToInquiryReplyDto(Board replyBoard) {
        Member admin = memberRepository.findById(replyBoard.getMember().getMemId())
                .orElseThrow(() -> new IllegalArgumentException("답변 작성자 정보를 찾을 수 없습니다."));

        boolean edited = replyBoard.getBrdUpdDt() != null
                && !replyBoard.getBrdUpdDt().equals(replyBoard.getBrdCreDt());

        List<FileDto> fileList = fileService.getFileList(
                RefDto.builder()
                        .refTy(BoardType.INQUIRY_ANSWER.name())
                        .refNo(replyBoard.getBrdId())
                        .build()
        );

        return InquiryReplyDto.builder()
                .brdId(replyBoard.getBrdId())
                .brdCon(replyBoard.getBrdCon())
                .memAdName(admin.getMemNm())
                .brdCreDt(replyBoard.getBrdCreDt())
                .brdUpdDt(replyBoard.getBrdUpdDt())
                .edited(edited)
                .fileList(fileList)
                .build();
    }

    // 사용자 문의 작성
    public BoardResultResponseDto createInquiryBoard(Long memId, InquiryBoardCreateDto inquiryBoardCreateDto) throws IOException {
        if (inquiryBoardCreateDto.getBrdTtl() == null || inquiryBoardCreateDto.getBrdTtl().isBlank()) {
            throw new IllegalArgumentException("문의 제목을 입력해 주세요.");
        }

        if (inquiryBoardCreateDto.getBrdCon() == null || inquiryBoardCreateDto.getBrdCon().isBlank()) {
            throw new IllegalArgumentException("문의 내용을 입력해 주세요.");
        }
        // 회원 정보 조회
        Member member = memberRepository.findById(memId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        // 문의 엔티티 생성
        Board inquiryBoard = Board.builder()
                .brdTy(BoardType.INQUIRY)
                .brdTtl(inquiryBoardCreateDto.getBrdTtl().trim())
                .brdCon(inquiryBoardCreateDto.getBrdCon().trim())
                .brdInqStt(InquiryStatus.RECEIVED)
                .member(member)
                .build();

        Board savedBoard = boardRepository.save(inquiryBoard);
        // 파일이 있다면 파일 저장
        List<FileDto> fileResults = new ArrayList<>();
        if (inquiryBoardCreateDto.getNewFiles() != null && !inquiryBoardCreateDto.getNewFiles().isEmpty()) {
            fileResults = fileService.saveFile(
                    inquiryBoardCreateDto.getNewFiles(),
                    RefDto.builder()
                            .refTy(BoardType.INQUIRY.name())
                            .refNo(savedBoard.getBrdId())
                            .build()
            );
        }

        return BoardResultResponseDto.builder()
                .actionMessage("문의가 등록되었습니다.")
                .boardResultMessage(fileUtils.buildBoardResultMessage(fileResults))
                .targetId(savedBoard.getBrdId())
                .build();
    }

    // 사용자 문의 수정
    public BoardResultResponseDto updateInquiryBoard(Long brdId, Long memId, InquiryBoardEditDto inquiryBoardEditDto) throws IOException {
        // 본인 문의 조회
        Board inquiryBoard = boardRepository
                .findByBrdIdAndBrdTyAndMember_MemIdAndBrdDelYnFalse(brdId, BoardType.INQUIRY, memId)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 문의글입니다."));

        if (inquiryBoard.getBrdInqStt() != InquiryStatus.RECEIVED) {
            throw new IllegalStateException("접수 상태의 문의만 수정할 수 있습니다.");
        }

        if (inquiryBoardEditDto.getBrdTtl() == null || inquiryBoardEditDto.getBrdTtl().isBlank()) {
            throw new IllegalArgumentException("문의 제목을 입력해 주세요.");
        }

        if (inquiryBoardEditDto.getBrdCon() == null || inquiryBoardEditDto.getBrdCon().isBlank()) {
            throw new IllegalArgumentException("문의 내용을 입력해 주세요.");
        }

        // 수정 처리
        inquiryBoard.updateInquiry(inquiryBoardEditDto.getBrdTtl().trim(),
                inquiryBoardEditDto.getBrdCon().trim());

        boardRepository.save(inquiryBoard);

        // 기존 첨부파일 삭제
        List<FileDto> fileDeleteResults = new ArrayList<>();
        if (inquiryBoardEditDto.getDeleteUuids() != null && !inquiryBoardEditDto.getDeleteUuids().isEmpty()) {
            fileDeleteResults = fileService.deleteFiles(inquiryBoardEditDto.getDeleteUuids());
        }

        // 신규 첨부파일 저장
        List<FileDto> fileResults = new ArrayList<>();
        if (inquiryBoardEditDto.getNewFiles() != null && !inquiryBoardEditDto.getNewFiles().isEmpty()) {
            fileResults = fileService.saveFile(
                    inquiryBoardEditDto.getNewFiles(),
                    RefDto.builder()
                            .refTy(BoardType.INQUIRY.name())
                            .refNo(brdId)
                            .build()
            );
        }
        fileResults.addAll(fileDeleteResults);

        return BoardResultResponseDto.builder()
                .actionMessage("문의가 수정되었습니다.")
                .boardResultMessage(fileUtils.buildBoardResultMessage(fileResults))
                .build();
    }

    // 사용자 문의 취소
    public void cancelInquiryBoard(Long brdId, Long memId) {
        // 본인 문의 조회
        Board inquiryBoard = boardRepository
                .findByBrdIdAndBrdTyAndMember_MemIdAndBrdDelYnFalse(brdId, BoardType.INQUIRY, memId)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 문의글입니다."));

        if (inquiryBoard.getBrdInqStt() != InquiryStatus.RECEIVED) {
            throw new IllegalStateException("접수 상태의 문의만 취소할 수 있습니다.");
        }

        inquiryBoard.markCancelled(null);

        boardRepository.save(inquiryBoard);
    }

    // 관리자 답글 작성
    public BoardResultResponseDto createInquiryReply(Long brdId, Long memAdId, InquiryReplySaveDto inquiryReplySaveDto) throws IOException{
        validateAdminAuthority(memAdId);

        // 문의 조회
        Board inquiryBoard = boardRepository
                .findByBrdIdAndBrdTyAndBrdDelYnFalse(brdId, BoardType.INQUIRY)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 문의글입니다."));

        if (inquiryBoard.getBrdInqStt() != InquiryStatus.IN_PROGRESS) {
            throw new IllegalStateException("처리중 상태의 문의만 답변할 수 있습니다.");
        }

        if (boardRepository.existsByBrdPrnIdAndBrdTyAndBrdDelYnFalse(brdId, BoardType.INQUIRY_ANSWER)) {
            throw new IllegalStateException("이미 답변이 등록된 문의입니다.");
        }

        if (inquiryReplySaveDto.getBrdCon() == null || inquiryReplySaveDto.getBrdCon().isBlank()) {
            throw new IllegalArgumentException("답변 내용을 입력해 주세요.");
        }

        Member admin = memberRepository.findById(memAdId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));

        Board replyBoard = Board.builder()
                .brdTy(BoardType.INQUIRY_ANSWER)
                .brdTtl(inquiryBoard.getBrdTtl())
                .brdCon(inquiryReplySaveDto.getBrdCon().trim())
                .brdPrnId(brdId)
                .member(admin)
                .build();
        // 답글 저장
        Board savedReplyBoard = boardRepository.save(replyBoard);
        // 첨부 파일 저장
        List<FileDto> fileResults = new ArrayList<>();
        if (inquiryReplySaveDto.getNewFiles() != null && !inquiryReplySaveDto.getNewFiles().isEmpty()) {
            fileResults = fileService.saveFile(
                    inquiryReplySaveDto.getNewFiles(),
                    RefDto.builder()
                            .refTy(BoardType.INQUIRY_ANSWER.name())
                            .refNo(savedReplyBoard.getBrdId())
                            .build()
            );
        }
        // 해당 문의의 상태를 답변 완료로 변경
        inquiryBoard.markAnswered();

        boardRepository.save(inquiryBoard);

        // 웹 알림
        notificationService.createNotification(
                inquiryBoard.getMember().getMemId(),
                NotificationType.INQUIRY_ANSWER_CREATE,
                inquiryBoard.getBrdId()
        );

        return BoardResultResponseDto.builder()
                .actionMessage("답변이 등록되었습니다.")
                .boardResultMessage(fileUtils.buildBoardResultMessage(fileResults))
                .targetId(savedReplyBoard.getBrdId())
                .build();
    }

    // 관리자 답글 수정
    public BoardResultResponseDto updateInquiryReply(Long brdId, Long memAdId, InquiryReplySaveDto inquiryReplySaveDto) throws IOException {
        validateAdminAuthority(memAdId);

        // 답글 조회
        Board replyBoard = boardRepository
                .findByBrdIdAndBrdTyAndBrdDelYnFalse(brdId, BoardType.INQUIRY_ANSWER)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 답변입니다."));

        if (!replyBoard.getMember().getMemId().equals(memAdId)) {
            throw new IllegalStateException("본인이 작성한 답변만 수정할 수 있습니다.");
        }

        if (inquiryReplySaveDto.getBrdCon() == null || inquiryReplySaveDto.getBrdCon().isBlank()) {
            throw new IllegalArgumentException("답변 내용을 입력해 주세요.");
        }
        // 답글 내용 수정
        replyBoard.updateAnswer(inquiryReplySaveDto.getBrdCon().trim());

        boardRepository.save(replyBoard);

        // 답글을 단 문의 정보 조회
        Board inquiryBoard = boardRepository
                .findByBrdIdAndBrdTyAndBrdDelYnFalse(replyBoard.getBrdPrnId(), BoardType.INQUIRY)
                .orElseThrow(() -> new IllegalArgumentException("원본 문의글을 찾을 수 없습니다."));

        // 답변 수정 후 수정 시간 갱신(관리자 목록 순서 갱신용)
        inquiryBoard.touch(memAdId);

        boardRepository.save(inquiryBoard);

        // 기존 첨부파일 삭제
        List<FileDto> fileDeleteResults = new ArrayList<>();
        if (inquiryReplySaveDto.getDeleteUuids() != null && !inquiryReplySaveDto.getDeleteUuids().isEmpty()) {
            fileDeleteResults = fileService.deleteFiles(inquiryReplySaveDto.getDeleteUuids());
        }

        // 신규 첨부파일 추가
        List<FileDto> fileResults = new ArrayList<>();
        if (inquiryReplySaveDto.getNewFiles() != null && !inquiryReplySaveDto.getNewFiles().isEmpty()) {
            fileResults = fileService.saveFile(
                    inquiryReplySaveDto.getNewFiles(),
                    RefDto.builder()
                            .refTy(BoardType.INQUIRY_ANSWER.name())
                            .refNo(replyBoard.getBrdId())
                            .build()
            );
        }
        fileResults.addAll(fileDeleteResults);

        // 웹 알림
        notificationService.createNotification(
                inquiryBoard.getMember().getMemId(),
                NotificationType.INQUIRY_ANSWER_UPDATE,
                inquiryBoard.getBrdId()
        );

        return BoardResultResponseDto.builder()
                .actionMessage("답변이 수정되었습니다.")
                .boardResultMessage(fileUtils.buildBoardResultMessage(fileResults))
                .build();
    }

    // 관리자 문의 상태 변경
    public void updateInquiryStatus(Long brdId, Long memAdId, InquiryStatus inquiryStatus, String brdCanRe) {
        validateAdminAuthority(memAdId);

        // 문의 조회
        Board inquiryBoard = boardRepository
                .findByBrdIdAndBrdTyAndBrdDelYnFalse(brdId, BoardType.INQUIRY)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 문의글입니다."));

        if (inquiryBoard.getBrdInqStt() != InquiryStatus.RECEIVED) {
            throw new IllegalStateException("접수 상태의 문의만 상태를 변경할 수 있습니다.");
        }

        if (inquiryStatus == InquiryStatus.IN_PROGRESS) {
            inquiryBoard.markInProgress(); // 문의 상태를 처리중으로 변경
        } else if (inquiryStatus == InquiryStatus.CANCELLED) {
            if (brdCanRe == null || brdCanRe.isBlank()) {
                throw new IllegalArgumentException("취소 사유를 입력해 주세요.");
            }
            inquiryBoard.markCancelled(brdCanRe.trim()); // 문의 상태를 취소로 변경하고 취소 사유를 저장
        } else {
            throw new IllegalArgumentException("변경할 수 없는 문의 상태입니다.");
        }

        boardRepository.save(inquiryBoard);
    }

    // 권한 검증
    private void validateAdminAuthority(Long memAdId) {
        Member admin = memberRepository.findById(memAdId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));

        if (!admin.getMemAut().equals(MemberAuthority.ADMIN)
                && !admin.getMemAut().equals(MemberAuthority.ROOT)) {
            throw new IllegalArgumentException("관리자만 사용할 수 있는 기능입니다.");
        }
    }

}
