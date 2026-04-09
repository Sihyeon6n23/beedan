package com.goodee.beedan.service.admin;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.dto.admin.MemberEditRequest;
import com.goodee.beedan.dto.admin.MemberListDto;
import com.goodee.beedan.dto.admin.MemberSummaryDto;
import com.goodee.beedan.dto.board.inquiry.InquiryBoardListDto;
import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.dto.order.ShipmentItemDto;
import com.goodee.beedan.entity.Board;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.mapper.member.MemberEditRequestToMemberMapper;
import com.goodee.beedan.repository.board.BoardRepository;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.order.OrderRepository;
import com.goodee.beedan.repository.order.ShipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMemberService {
    private final MemberRepository memberRepository;
    private final MemberEditRequestToMemberMapper memberMapper;
    private final OrderRepository orderRepository;       // 주문 내역용
    private final ShipmentRepository shipmentRepository; // 배송 내역용
    private final BoardRepository boardRepository;

    @Transactional
    public void updateMember(MemberEditRequest request) {
        Member member = memberRepository.findById(request.getId()).orElseThrow(() -> new EntityNotFoundException("회원 없음"));

        memberMapper.updateMemberFromDto(request, member);
    }

    @Transactional(readOnly = true)
    public Page<MemberListDto> getAllMembers(Pageable pageable) {
        return memberRepository.findAllUsers(pageable)
                .map(member -> MemberListDto.builder()
                        .memId(member.getMemId())
                        .memLgnId(member.getMemLgnId())
                        .memNm(member.getMemNm())
                        .memBizNo(member.getMemBizNo())
                        .memBizTtl(member.getMemBizTtl())
                        .memCeoNm(member.getMemCeoNm())
                        .memCreDt(member.getMemCreDt())
                        .memBizAdr(member.getMemBizAdr())
                        .memStt(member.getMemStt())
                        .memAut(member.getMemAut())
                        .build()
                );
    }

    @Transactional(readOnly = true)
    public Page<MemberListDto> getMembersByStatus(String status, Pageable pageable) {
        Page<Member> members;
        if ("ALL".equals(status)) {
            members = memberRepository.findAllUsers(pageable);
        } else {
            members = memberRepository.findUsersByStatus(status, pageable);
        }

        return members.map(member -> MemberListDto.builder()
                .memId(member.getMemId())
                .memLgnId(member.getMemLgnId())
                .memNm(member.getMemNm())
                .memBizNo(member.getMemBizNo())
                .memBizTtl(member.getMemBizTtl())
                .memCeoNm(member.getMemCeoNm())
                .memCreDt(member.getMemCreDt())
                .memBizAdr(member.getMemBizAdr())
                .memStt(member.getMemStt())
                .build()
        );
    }

    @Transactional(readOnly = true)
    public MemberSummaryDto getMemberSummary(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<Order> recentOrders = orderRepository.findTop3ByMember_MemIdOrderByOrdBaseCreDtDesc(memberId);
        List<Shipment> recentShipments = shipmentRepository.findTop3ByOrder_Member_MemIdOrderByShCreDtDesc(memberId);
        PageRequest pageRequest = PageRequest.of(0, 3); // 첫 페이지의 5건

        Page<Board> recentInquiries = boardRepository.findUserInquiryBoards(
                BoardType.INQUIRY,
                memberId,
                null,              // 문의 상태 (전체 조회 시 null)
                null,              // 키워드 (검색어 없을 시 null)
                pageRequest
        );

        return MemberSummaryDto.builder()
                .memLgnId(member.getMemLgnId())
                .memCreDt(member.getMemCreDt())
                .memNm(member.getMemNm())
                .recentOrders(recentOrders.stream().map(this::toOrderSummaryDto).toList())
                .recentShipments(recentShipments.stream().map(this::toShipmentSummaryDto).toList())
                .recentInquiries(recentInquiries.map(this::toInquirySummaryDto))
                .build();
    }

    private MemberSummaryDto.OrderSummaryDto toOrderSummaryDto(Order order) {
        String summaryName = "상품 정보 없음";

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            int totalItems = order.getOrderItems().size();
            String firstItemName = order.getOrderItems().get(0).getOrdItmNm();

            if (totalItems > 1) {
                summaryName = firstItemName + " 외 " + (totalItems - 1) + "건";
            } else {
                summaryName = firstItemName;
            }
        }

        return MemberSummaryDto.OrderSummaryDto.builder()
                .ordBaseCreDt(order.getOrdBaseCreDt())
                .ordBaseId(order.getOrdBaseId())
                .ordBaseNo(order.getOrdBaseNo())
                .ordSummaryNm(summaryName)
                .ordBaseTtAm(order.getOrdBaseTtAm())
                .ordBaseStt(order.getOrdBaseStt())
                .build();
    }

    private MemberSummaryDto.ShipmentSummaryDto toShipmentSummaryDto(Shipment shipment) {
        return MemberSummaryDto.ShipmentSummaryDto.builder()
                .shStt(shipment.getShStt())
                .shTraNo(shipment.getShTraNo())
                .shAdr(shipment.getShAdr())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ShipmentDto> getShipmentList(Long memId,Pageable pageable){
        Page<Shipment> shipments = shipmentRepository.findByMemberId(memId, pageable);
        return  shipments.map(this::mapToShipmentDto);
    }

    private ShipmentDto mapToShipmentDto(Shipment shipment){
        return ShipmentDto.builder()
                .shId(shipment.getShId())
                .shCarCd(shipment.getShCarCd())
                .shStt(shipment.getShStt())
                .shCreDt(shipment.getShCreDt())
                .shTraNo(shipment.getShTraNo())
                .shRcvNm(shipment.getShRcvNm())
                .shAdr(shipment.getShAdr())
                .shAdrDt(shipment.getShAdrDt())
                .items(shipment.getShipmentItems().stream()
                        .map(item -> ShipmentItemDto.builder()
                                .ordItmNm(item.getOrdItmNm())
                                .shQn(item.getShQn())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private InquiryBoardListDto toInquirySummaryDto(Board inquiryBoard) {
        // 문의 작성자 조회
        Member member = memberRepository.findById(inquiryBoard.getMember().getMemId())
                .orElseGet(Member::new);
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

}
