package com.goodee.beedan.service.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.specification.BoardSpecs;
import com.goodee.beedan.dto.board.notice.*;
import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.entity.Board;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.mapper.board.BoardMapper;
import com.goodee.beedan.repository.board.BoardRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.service.file.FileService;
import com.goodee.beedan.service.file.FileUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NoticeBoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final BoardCoreService boardCoreService;
    private final BoardMapper boardMapper; // 통합 매퍼 사용
    private final FileUtils fileUtils;

    @Value("${board.notice.fixed-size}")
    private int fixedNoticeSize;

    /** 1. 게시글 작성 (공통) */
    public String writeNotice(CommonBoardRequestDto dto, String username) throws IOException {
        Member member = getMemberByUsername(username);

        // 권한 체크 (공지사항 기준)
        if (member.getMemAut() == MemberAuthority.USER) {
            throw new SecurityException("게시글 작성 권한이 없습니다.");
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

        // 파일 저장 로직 통합
        List<FileDto> fileResults = handleFiles(dto, board.getBrdId());

        // 메시지 반환
        return fileUtils.generateBoardResultMessage(fileUtils.buildBoardResultMessage(fileResults), false);
    }

    /** 2. 게시글 수정 (공통) */
    public String updateNotice(CommonBoardRequestDto dto, String username) throws IOException {
        Board board = boardCoreService.getBoard(dto.getBrdId(), BoardType.NOTICE);

        checkModifyAuthority(board, username);

        // 엔티티 업데이트 (Dirty Checking)
        board.updateInquiry(dto.getBrdTtl(), dto.getBrdCon());
        board.setBrdFixYn(dto.getBrdFixYn());

        // 파일 처리 (삭제 + 신규 저장)
        List<FileDto> fileDeleteResult = new ArrayList<>();
        if (dto.getDeleteUuids() != null && !dto.getDeleteUuids().isEmpty()) {
            fileDeleteResult = fileService.deleteFiles(dto.getDeleteUuids());
        }

        // 파일 저장 로직 통합
        List<FileDto> fileResults = handleFiles(dto, board.getBrdId());
        fileResults.addAll(fileDeleteResult);

        // 메시지 반환
        return fileUtils.generateBoardResultMessage(fileUtils.buildBoardResultMessage(fileResults), true);
    }

    /** 3. 상세 조회 (이전/다음글 포함 통합 버전) */
    public CommonBoardDetailDto getNoticeDetail(Long brdId, String currentUsername) {
        Board board = boardCoreService.getBoard(brdId, BoardType.NOTICE);

        // 매퍼를 통한 기본 변환
        CommonBoardDetailDto dto = boardMapper.toDetailDto(board);

        // 이전글/다음글 조회 및 DTO 매핑
        dto.setPrevBoard(boardRepository.findFirstByBrdIdLessThanAndBrdTyAndBrdDelYnFalseOrderByBrdIdDesc(brdId, BoardType.NOTICE)
                .map(boardMapper::toDetailDto).orElse(null));
        dto.setNextBoard(boardRepository.findFirstByBrdIdGreaterThanAndBrdTyAndBrdDelYnFalseOrderByBrdIdAsc(brdId, BoardType.NOTICE)
                .map(boardMapper::toDetailDto).orElse(null));

        // 파일 목록 조회
        List<FileDto> fileList = fileService.getFileList(RefDto.builder()
                .refTy(BoardType.NOTICE.name())
                .refNo(brdId).build());
        dto.setFileList(fileList);

        // 권한 플래그 설정
        dto.setCanEdit(isModifiable(board, currentUsername));

        return dto;
    }

    /** 3. 공지사항 삭제 */
    public void deleteNotice(Long brdId, String username) {
        // 1. 게시글 존재 여부 및 타입 검증
        Board board = boardCoreService.getBoard(brdId, BoardType.NOTICE);

        // 2. 권한 체크
        checkModifyAuthority(board, username);

        // 3. 게시글 소프트 삭제 (DB 상태 변경)
        board.markAsDeleted();

        // 4. 연관 파일 삭제 로직 (복구)
        // RefDto를 통해 'NOTICE' 타입의 'brdId' 번호를 가진 모든 파일을 찾아 삭제
        fileService.deleteFilesByRef(RefDto.builder()
                .refTy(BoardType.NOTICE.name())
                .refNo(brdId)
                .build());

        log.info("공지사항 및 연관 파일 삭제 완료: ID={}, 삭제자={}", brdId, username);
    }

    /** 4. 목록 조회 (통합 DTO 및 배치 조회 적용) */
    public BoardListResponse getBoardList(BoardType boardType, SearchDto searchDto) {
        int targetSize = searchDto.getSize(); // 보통 10
        int currentPage = searchDto.getPage();

        // 1. 고정글 조회 (첫 페이지에서만 표시할지, 모든 페이지에서 표시할지에 따라 로직이 달라지나 보통 첫 페이지에 포함)
        List<Board> fixedEntities = (currentPage == 0 && boardType.isUseFixed()) ?
                boardRepository.findTopFixedNotices(boardType, PageRequest.of(0, fixedNoticeSize, Sort.by("brdCreDt").descending()))
                : Collections.emptyList();

        int fixedCount = fixedEntities.size();

        // 2. 일반글 페이징 조절
        // 첫 페이지라면 (10 - 고정글 개수)만큼만 가져오고,
        // 두 번째 페이지부터는 오프셋을 계산하여 가져와야 합니다.

        Specification<Board> spec = BoardSpecs.isActive(boardType);

        // 고정글 제외 로직 (이미 가져온 고정글이 일반 목록에 중복되지 않도록)
        List<Long> fixedIds = fixedEntities.stream().map(Board::getBrdId).collect(Collectors.toList());
        if (!fixedIds.isEmpty()) {
            spec = spec.and(BoardSpecs.notInIds(fixedIds));
        }

        // 검색 조건 추가
        if (boardType.isUseStatus() && searchDto.getBrdInqStt() != null) {
            spec = spec.and(BoardSpecs.withStatus(searchDto.getBrdInqStt()));
        }
        spec = spec.and(BoardSpecs.withKeyword(searchDto.getKeyword(), searchDto.getSearchType()))
                .and(BoardSpecs.fetchMember());

        // --- 핵심: 페이징 계산 ---
        Pageable pageable;
        if (currentPage == 0) {
            // 첫 페이지: (10 - 고정글수) 개만큼 조회
            pageable = PageRequest.of(0, Math.max(1, targetSize - fixedCount), Sort.by("brdCreDt").descending());
        } else {
            // 이후 페이지: 고정글로 인해 밀려난 offset 계산 필요
            // 하지만 간단한 구현을 위해 '일반 공지' 기준으로만 페이징을 유지하고 싶다면
            // 전체 사이즈 10을 유지하되, 프론트에서 fixedEntities가 0일 때 처리하게 합니다.
            pageable = PageRequest.of(currentPage, targetSize, Sort.by("brdCreDt").descending());
        }

        Page<Board> normalPage = boardRepository.findAll(spec, pageable);

        // 3. 파일 존재 여부 (기존 로직 유지)
        List<Long> allIds = Stream.concat(fixedEntities.stream(), normalPage.getContent().stream())
                .map(Board::getBrdId).distinct().toList();
        Set<Long> fileExistSet = boardType.isFileUpload() ?
                fileService.getFileYnSet(boardType.name(), allIds) : Collections.emptySet();

        // 4. DTO 변환
        List<CommonBoardDetailDto> fixedDtos = fixedEntities.stream()
                .map(entity -> convertToDto(entity, fileExistSet.contains(entity.getBrdId())))
                .toList();

        Page<CommonBoardDetailDto> normalDtos = normalPage.map(entity ->
                convertToDto(entity, fileExistSet.contains(entity.getBrdId())));

        return new BoardListResponse(fixedDtos, new PageResponseDto<>(normalDtos, 5));
    }

    /** 변환 보조 메서드 */
    private CommonBoardDetailDto convertToDto(Board board, boolean hasFile) {
        CommonBoardDetailDto dto = boardMapper.toDetailDto(board);
        dto.setFileYn(hasFile);
        // isNew 계산 로직은 매퍼의 Expression에서 처리되거나 여기서 수동 설정 가능
        return dto;
    }

    // --- Private Helper Methods ---

    private boolean isModifiable(Board board, String username) {
        try { return checkModifyAuthority(board, username); }
        catch (Exception e) { return false; }
    }

    private boolean checkModifyAuthority(Board board, String username) {
        if (username == null || username.isBlank()) throw new SecurityException("로그인이 필요합니다.");
        Member requestMem = getMemberByUsername(username);
        MemberAuthority auth = requestMem.getMemAut();
        if (auth == MemberAuthority.ROOT) return true;
        if (auth == MemberAuthority.ADMIN && board.getMember().getMemLgnId().equals(username)) return true;
        throw new SecurityException("해당 권한이 없습니다.");
    }

    private Member getMemberByUsername(String username) {
        return memberRepository.findByMemLgnId(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 미존재: " + username));
    }

    private List<FileDto> handleFiles(CommonBoardRequestDto dto, Long brdId) throws IOException {
        if (dto.getNewFiles() != null && !dto.getNewFiles().isEmpty()) {
            return fileService.saveFile(dto.getNewFiles(), RefDto.builder()
                    .refTy(BoardType.NOTICE.name())
                    .refNo(brdId).build());
        }
        return new ArrayList<>();
    }
}