package com.goodee.beedan.service.order;

import com.goodee.beedan.common.constant.OrderStatus;
import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    public List<OrderDto> getOrderList(Long memId){
        if(!memberRepository.existsById(memId)) return null;

        List<OrderDto> orderList = orderRepository.findByMember_MemIdOrderByOrdBaseCreDtDesc(memId).stream()
                .map(order -> mapToOrderDto(order))
                .toList();

        return orderList;
    }

    public OrderDto getOrderDetail(Long ordId, Long memId){
        Order order = orderRepository.findById(ordId)
                .orElseThrow(()->new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if(!order.getMember().getMemId().equals(memId)) {
            throw new IllegalArgumentException("본인의 주문만 조회할 수 있습니다.");
        }

        return mapToOrderDto(order);
    }

    @Transactional
    public void updateOrder(Long ordId, Long memId, OrderDto dto){
        Order order = orderRepository.findById(ordId).orElseThrow(()->new IllegalArgumentException("Order not found"));

        if(!order.getMember().getMemId().equals(memId)) {
            throw new IllegalArgumentException("본인의 주문만 수정할 수 있습니다.");
        }

        if (order.getOrdBaseStt() != OrderStatus.PREPARING) {
            throw new IllegalStateException("배송 준비 중일 때만 주소를 수정할 수 있습니다.");
        }
        if(dto == null) return;

        if(dto.getOrdBaseAdr() != null) order.setOrdBaseAdr(dto.getOrdBaseAdr());
        if(dto.getOrdBaseAdrDt() != null) order.setOrdBaseAdrDt(dto.getOrdBaseAdrDt());
        if(dto.getOrdBaseRcvNm() != null) order.setOrdBaseRcvNm(dto.getOrdBaseRcvNm());
        if(dto.getOrdBaseMsg() != null) order.setOrdBaseMsg(dto.getOrdBaseMsg());
    }

    @Transactional
    public void updateOrderStatus(Long ordId, OrderStatus newStatus) {
        Order order = orderRepository.findById(ordId).orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if(order.getOrdBaseStt() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("취소된 주문의 상태는 변경할 수 없습니다.");
        }

        order.setOrdBaseStt(newStatus);
    }

    @Transactional
    public void cancelOrder(Long ordId, Long memId) {
        if(!memberRepository.existsById(memId)) throw new IllegalArgumentException("회원이 존재하지 않습니다.");

        Order order = orderRepository.findById(ordId).orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.getOrdBaseStt() == OrderStatus.DELIVERING || order.getOrdBaseStt() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("이미 배송이 시작되어 취소할 수 없습니다.");
        }

        order.setOrdBaseStt(OrderStatus.CANCELLED);
    }

    @Transactional
    public void createOrder(Long memId, OrderDto dto){
        Member member = memberRepository.findById(memId).orElseThrow(()->new UsernameNotFoundException("User not found"));

        Order order = Order.builder()
                .member(member)
                .ordBaseRcvNm(dto.getOrdBaseRcvNm())
                .ordBaseAdr(dto.getOrdBaseAdr())
                .ordBaseAdrDt(dto.getOrdBaseAdrDt())
                .ordBaseMsg(dto.getOrdBaseMsg())
                .ordBaseStt(OrderStatus.PREPARING)
                .build();

        orderRepository.save(order);
    }

    public OrderDto mapToOrderDto(Order order){
        OrderDto orderDto = OrderDto.builder()
                .ordBaseId(order.getOrdBaseId())
                .ordBaseAdr(order.getOrdBaseAdr())
                .ordBaseAdrDt(order.getOrdBaseAdrDt())
                .ordBaseRcvNm(order.getOrdBaseRcvNm())
                .ordBaseMsg(order.getOrdBaseMsg())
                .ordBaseStt(order.getOrdBaseStt())
                .ordBaseNo(order.getOrdBaseNo())
                .ordBaseCreDt(order.getOrdBaseCreDt())
                .build();

        return orderDto;
    }
}
