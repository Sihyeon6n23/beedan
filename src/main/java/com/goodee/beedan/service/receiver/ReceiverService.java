package com.goodee.beedan.service.receiver;

import com.goodee.beedan.dto.receiver.ReceiverDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Receiver;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.receiver.ReceiverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceiverService {
    private final ReceiverRepository receiverRepository;
    private final MemberRepository memberRepository;

    public void createReceiver(Long memId){
        Member member = memberRepository.findById(memId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if(!receiverRepository.existsByMember_memId(memId)) {
            Receiver receiver = Receiver.builder()
                    .member(member)
                    .rcNm("기본 배송지")
                    .rcPhn(member.getMemMbPhn())
                    .rcAdr(member.getMemBizAdr())
                    .rcAdrDt(member.getMemBizDtAdr())
                    .rcDelYn(false)
                    .rcAdrDfYn(true)
                    .rcIamYn(false)
                    .build();
            receiverRepository.save(receiver);
        }
    }

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
                .rcDelYn(false)
                .rcAdrDfYn(false)
                .rcIamYn(false)
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

    public void updateReceiver(ReceiverDto receiverDto){
        Receiver receiver = receiverRepository.findByRcIdAndMember_memIdAndRcDelYnFalse(receiverDto.getRcId(), receiverDto.getMemId()).orElseThrow(() -> new IllegalArgumentException("해당하는 배송지 내역이 없거나 권한이 없습니다."));

        if (receiver == null) {
            throw new IllegalArgumentException("해당하는 배송지 내역이 없거나 권한이 없습니다.");
        }

        if(receiverDto.getRcNm() != null) receiver.setRcNm(receiverDto.getRcNm());
        if(receiverDto.getRcAdr() != null) receiver.setRcAdr(receiverDto.getRcAdr());
        if(receiverDto.getRcAdrDt() != null) receiver.setRcAdrDt(receiverDto.getRcAdrDt());
        if(receiverDto.getRcPhn() != null) receiver.setRcPhn(receiverDto.getRcPhn());
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
                .rcMsg(receiver.getRcMsg())
                .build();
    }
}