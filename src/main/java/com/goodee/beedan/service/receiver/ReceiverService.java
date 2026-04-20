package com.goodee.beedan.service.receiver;

import com.goodee.beedan.dto.receiver.ReceiverDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Receiver;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.receiver.ReceiverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceiverService {
    private final ReceiverRepository receiverRepository;
    private final MemberRepository memberRepository;

    public void addReceiverAddr(ReceiverDto receiverDto) {
        Member member = memberRepository.findById(receiverDto.getMemId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Receiver receiver = Receiver.builder()
                .member(member)
                .rcNm(receiverDto.getRcNm())
                .rcPhn(receiverDto.getRcPhn())
                .rcAdr(receiverDto.getRcAdr())
                .rcAdrDt(receiverDto.getRcAdrDt())
                .rcMsg(receiverDto.getRcMsg())
                .rcIamYn(receiverDto.getRcIamYn())
                .rcZip(receiverDto.getRcZip())
                .rcDelYn(false)
                .rcAdrDfYn(false)
                .build();

        receiverRepository.save(receiver);
    }

    @Transactional(readOnly = true)
    public List<ReceiverDto> getReceiverList(Long memId){
        return receiverRepository.findByMember_memIdAndRcDelYnFalseOrderByRcAdrDfYnDescRcIdAsc(memId).stream()
                .map(this::mapToReceiverDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReceiverDto getReceiver(Long memId, Long rcId){
        Receiver receiver = receiverRepository.findByRcIdAndMember_memIdAndRcDelYnFalse(rcId, memId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 배송지 내역이 없습니다."));

        return mapToReceiverDto(receiver);
    }

    @Transactional
    public void updateReceiver(ReceiverDto receiverDto){
        Receiver receiver = receiverRepository.findByRcIdAndMember_memIdAndRcDelYnFalse(receiverDto.getRcId(), receiverDto.getMemId())
                .orElseThrow(() -> new IllegalArgumentException("해당하는 배송지 내역이 없거나 권한이 없습니다."));

        if(receiverDto.getRcNm() != null) receiver.setRcNm(receiverDto.getRcNm());
        if(receiverDto.getRcPhn() != null) receiver.setRcPhn(receiverDto.getRcPhn());
        if(receiverDto.getRcAdr() != null) receiver.setRcAdr(receiverDto.getRcAdr());
        if(receiverDto.getRcAdrDt() != null) receiver.setRcAdrDt(receiverDto.getRcAdrDt());
        if(receiverDto.getRcZip() != null) receiver.setRcZip(receiverDto.getRcZip()); // 추가
        if(receiverDto.getRcIamYn() != null) receiver.setRcIamYn(receiverDto.getRcIamYn()); // 추가
        if(receiverDto.getRcMsg() != null) receiver.setRcMsg(receiverDto.getRcMsg());
    }

    public void deleteReceiver(Long memId, Long rcId){
        Receiver receiver = receiverRepository.findByRcIdAndMember_memIdAndRcDelYnFalse(rcId, memId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 배송지 내역이 없거나 권한이 없습니다."));

        receiver.setRcDelYn(true);
    }

    public ReceiverDto mapToReceiverDto(Receiver receiver){
        return ReceiverDto.builder()
                .rcId(receiver.getRcId())
                .rcNm(receiver.getRcNm())
                .rcPhn(receiver.getRcPhn())
                .rcAdr(receiver.getRcAdr())
                .rcAdrDt(receiver.getRcAdrDt())
                .rcZip(receiver.getRcZip())
                .rcIamYn(receiver.getRcIamYn())
                .rcMsg(receiver.getRcMsg())
                .build();
    }
}