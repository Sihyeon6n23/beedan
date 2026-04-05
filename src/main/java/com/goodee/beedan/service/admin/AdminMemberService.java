package com.goodee.beedan.service.admin;

import com.goodee.beedan.dto.admin.MemberEditRequest;
import com.goodee.beedan.dto.admin.MemberListDto;
import com.goodee.beedan.dto.admin.MemberSummaryDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.mapper.member.MemberEditRequestToMemberMapper;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.order.OrderRepository;
import com.goodee.beedan.repository.order.ShipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMemberService {
    private final MemberRepository memberRepository;
    private final MemberEditRequestToMemberMapper memberMapper;
    private final OrderRepository orderRepository;       // 주문 내역용
    private final ShipmentRepository shipmentRepository; // 배송 내역용

    @Transactional
    public void updateMember(MemberEditRequest request) {
        // 1. 기존 데이터 조회
        Member member = memberRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));

        memberMapper.updateMemberFromDto(request, member);
    }

    @Transactional(readOnly = true)
    public Page<MemberListDto> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(member -> MemberListDto.builder()
                        .memId(member.getMemId())
                        .memLgnId(member.getMemLgnId())
                        .memNm(member.getMemNm())
                        .memBizNo(member.getMemBizNo())
                        .memBizTtl(member.getMemBizTtl())
                        .memCeoNm(member.getMemCeoNm())
                        .memBizAdr(member.getMemBizAdr())
                        .memStt(member.getMemStt())
                        .build()
                );
    }


    @Transactional(readOnly = true)
    public MemberSummaryDto getMemberSummary(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<Order> recentOrders = orderRepository.findTop3ByMember_MemIdOrderByOrdBaseCreDtDesc(memberId);
        List<Shipment> recentShipments = shipmentRepository.findTop2ByOrder_Member_MemIdOrderByShCreDtDesc(memberId);
        // List<Inquiry> recentInquiries = inquiryRepository.findTop3ByMember_MemIdOrderByInquiryCreDtDesc(memberId); // 문의 관련은 확인 필요.

        return MemberSummaryDto.builder()
                .memLgnId(member.getMemLgnId())
                .memCreDt(member.getMemCreDt())
                .memNm(member.getMemNm())
                .recentOrders(recentOrders.stream().map(this::toOrderSummaryDto).toList())
                .recentShipments(recentShipments.stream().map(this::toShipmentSummaryDto).toList())
                .recentInquiries(Collections.emptyList()) // recentInquiries.stream().map(this::toInquirySummaryDto).toList())
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

    /*private MemberSummaryDto.ShipmentSummaryDto toShipmentSummaryDto(Shipment shipment) {
        // TODO: 17Track 등 외부 API 연동 시, 여기서 trackingNumber를 기반으로
        // 최신 타임라인(trackingDetails) 데이터를 가져와 덧붙이는 로직을 추가할 수 있습니다.

        return MemberSummaryDto.ShipmentSummaryDto.builder()
                .statusName(shipment.getShipmentStatus().name()) // Enum 한글명 매핑 필요
                .trackingNumber(shipment.getTrackingNumber())
                .destinationAddress(shipment.getDestinationAddress())
                .trackingDetails(Collections.emptyList()) // 외부 API 연동 전까지 빈 리스트
                .build();
    }*/

}
