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

    public OrderDto getOrderDetail(Long ordId){
        Order order = orderRepository.findById(ordId).orElseThrow(()->new IllegalArgumentException("Order not found"));
        return mapToOrderDto(order);
    }

    @Transactional
    public void updateOrder(Long ordId, OrderDto dto){
        Order order = orderRepository.findById(ordId).orElseThrow(()->new IllegalArgumentException("Order not found"));

        if(dto == null) return;

        if(dto.getOrdBaseAdr() != null) order.setOrdBaseAdr(dto.getOrdBaseAdr());
        if(dto.getOrdBaseAdrDt() != null) order.setOrdBaseAdrDt(dto.getOrdBaseAdrDt());
        if(dto.getOrdBaseRcvNm() != null) order.setOrdBaseRcvNm(dto.getOrdBaseRcvNm());
        if(dto.getOrdBaseMsg() != null) order.setOrdBaseMsg(dto.getOrdBaseMsg());
        if(dto.getOrdBaseStt() != null) order.setOrdBaseStt(dto.getOrdBaseStt());
    }

    @Transactional
    public void cancelOrder(Long ordId){
        Order order = orderRepository.findById(ordId).orElseThrow(()->new IllegalArgumentException("Order not found"));

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
                .build();

        return orderDto;
    }
}
