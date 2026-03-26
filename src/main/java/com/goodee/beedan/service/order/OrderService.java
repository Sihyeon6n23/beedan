package com.goodee.beedan.service.order;

import com.goodee.beedan.dto.order.OrderDto;
import com.goodee.beedan.entity.Order;
import com.goodee.beedan.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;

    public List<OrderDto> getOrderList(Long id){
        List<OrderDto> orderList = orderRepository.findById(id).stream()
                .map(order -> mapToOrderDto(order))
                .toList();

        return orderList;
    }

    public void createOrder(Long id){

    }

    public OrderDto mapToOrderDto(Order order){
        OrderDto orderDto = OrderDto.builder()
                .ord_base_id(order.getOrd_base_id())
                .ord_base_adr_dt(order.getOrd_base_adr_dt())
                .ord_base_adr_da(order.getOrd_base_adr_da())
                .ord_base_nm(order.getOrd_base_nm())
                .ord_base_msg(order.getOrd_base_msg())
                .ord_base_rcv_nm(order.getOrd_base_rcv_nm())
                .build();

        return orderDto;
    }
}
