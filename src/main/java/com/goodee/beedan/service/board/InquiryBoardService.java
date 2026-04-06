package com.goodee.beedan.service.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.dto.board.*;
import com.goodee.beedan.entity.Board;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.board.BoardRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.service.mail.MailService;
import com.goodee.beedan.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InquiryBoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final MailService mailService;

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

        return userInquiryBoards.map(this::mapToInquiryBoardListDto);
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

        return adminInquiryBoards.map(this::mapToInquiryBoardListDto);
    }

    private InquiryBoardListDto mapToInquiryBoardListDto(Board inquiryBoard) {
        // 문의 작성자 조회
        Member member = memberRepository.findById(inquiryBoard.getMember().getMemId())
                .orElseThrow(() -> new IllegalArgumentException("문의 작성자 정보를 찾을 수 없습니다."));
        // 문의 답글 조회
        Optional<Board> replyBoard = boardRepository
                .findByBrdPrnIdAndBrdTyAndBrdDelYnFalse(inquiryBoard.getBrdId(), BoardType.INQUIRY_ANSWER);

        boolean hasReply = replyBoard.isPresent(); // 답글이 있는지, 없는지
        boolean replyEdited = replyBoard
                .map(reply -> reply.getBrdUpdDt() != null // 수정 시간이 있고
                        && !reply.getBrdUpdDt().equals(reply.getBrdCreDt())) // 생성 시간과 수정 시간이 다르다면
                .orElse(false);

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
                .build();
    }

    private InquiryReplyDto mapToInquiryReplyDto(Board replyBoard) {
        Member admin = memberRepository.findById(replyBoard.getMember().getMemId())
                .orElseThrow(() -> new IllegalArgumentException("답변 작성자 정보를 찾을 수 없습니다."));

        boolean edited = replyBoard.getBrdUpdDt() != null
                && !replyBoard.getBrdUpdDt().equals(replyBoard.getBrdCreDt());

        return InquiryReplyDto.builder()
                .brdId(replyBoard.getBrdId())
                .brdCon(replyBoard.getBrdCon())
                .memAdName(admin.getMemNm())
                .brdCreDt(replyBoard.getBrdCreDt())
                .brdUpdDt(replyBoard.getBrdUpdDt())
                .edited(edited)
                .build();
    }

    // 사용자 문의 작성
    public Long createInquiryBoard(Long memId, InquiryBoardCreateDto inquiryBoardCreateDto) {
        if (inquiryBoardCreateDto.getBrdTtl() == null || inquiryBoardCreateDto.getBrdTtl().isBlank()) {
            throw new IllegalArgumentException("문의 제목을 입력해 주세요.");
        }

        if (inquiryBoardCreateDto.getBrdCon() == null || inquiryBoardCreateDto.getBrdCon().isBlank()) {
            throw new IllegalArgumentException("문의 내용을 입력해 주세요.");
        }

        Member member = memberRepository.findById(memId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        Board inquiryBoard = Board.builder()
                .brdTy(BoardType.INQUIRY)
                .brdTtl(inquiryBoardCreateDto.getBrdTtl().trim())
                .brdCon(inquiryBoardCreateDto.getBrdCon().trim())
                .brdInqStt(InquiryStatus.RECEIVED)
                .member(member)
                .build();

        Board savedBoard = boardRepository.save(inquiryBoard);

        return savedBoard.getBrdId();
    }

    // 사용자 문의 수정
    public void updateInquiryBoard(Long brdId, Long memId, InquiryBoardEditDto inquiryBoardEditDto) {
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
    public Long createInquiryReply(Long brdId, Long memAdId, InquiryReplySaveDto inquiryReplySaveDto) {
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
        // 해당 문의의 상태를 답변 완료로 변경
        inquiryBoard.markAnswered();

        boardRepository.save(inquiryBoard);

        // 웹 알림
        notificationService.createNotification(
                inquiryBoard.getMember().getMemId(),
                NotificationType.INQUIRY_ANSWER_CREATE,
                inquiryBoard.getBrdId()
        );

        // 답글을 단 문의의 회원 정보 조회
        Member inquiryMember = memberRepository.findById(inquiryBoard.getMember().getMemId())
                .orElseThrow(() -> new IllegalArgumentException("문의 작성자 정보를 찾을 수 없습니다."));
        // 메일 알림 (인자값 때문에 일단 비활성화)
//        mailService.sendMail(
//                inquiryMember.getMemEml(),
//                NotificationType.INQUIRY_ANSWER_CREATE,
//                inquiryBoard.getBrdTtl(), // detail은 보류(임시로 제목 넣어놨음)
//                inquiryBoard.getBrdId()
//        );

        return savedReplyBoard.getBrdId();
    }

    // 관리자 답글 수정
    public void updateInquiryReply(Long brdId, Long memAdId, InquiryReplySaveDto inquiryReplySaveDto) {
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

        // 웹 알림
        notificationService.createNotification(
                inquiryBoard.getMember().getMemId(),
                NotificationType.INQUIRY_ANSWER_UPDATE,
                inquiryBoard.getBrdId()
        );

        // 답글을 단 문의의 회원 정보 조회
        Member inquiryMember = memberRepository.findById(inquiryBoard.getMember().getMemId())
                .orElseThrow(() -> new IllegalArgumentException("문의 작성자 정보를 찾을 수 없습니다."));
        // 메일 알림 (인자값 때문에 일단 비활성화)
//        mailService.sendMail(
//                inquiryMember.getMemEml(),
//                NotificationType.INQUIRY_ANSWER_UPDATE,
//                inquiryBoard.getBrdTtl(), // detail은 보류(임시로 제목 넣어놨음)
//                inquiryBoard.getBrdId()
//        );
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
