package com.goodee.beedan.service.shipment;

import com.goodee.beedan.dto.order.ShipmentDto;
import com.goodee.beedan.entity.Shipment;
import com.goodee.beedan.repository.order.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;

    public List<ShipmentDto> getShipmentList(Long id){
        List<ShipmentDto> shipmentDtoList = shipmentRepository.findById(id).stream()
                .map(shipment -> mapToShipmentDto(shipment))
                .toList();

        return shipmentDtoList;
    }


    public ShipmentDto mapToShipmentDto(Shipment shipment){
        ShipmentDto shipmentDto = ShipmentDto.builder()
                .shi_id(shipment.getShi_id())
                .shi_ca_cd(shipment.getShi_ca_cd())
                .shi_stt(shipment.getShi_stt())
                .shi_tra_no(shipment.getShi_tra_no())
                .shi_base_cre_dt(shipment.getShi_base_cre_dt())
                .shi_div_dt(shipment.getShi_div_dt())
                .build();

        return shipmentDto;
    }
}
