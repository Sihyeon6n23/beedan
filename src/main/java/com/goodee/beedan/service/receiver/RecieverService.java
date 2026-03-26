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
public class RecieverService {
    private final ReceiverRepository receiverRepository;
    private final MemberRepository memberRepository;

    public void createReciver(Long memId){
        Member member = memberRepository.findById(memId).orElseThrow(()-> new UsernameNotFoundException("User not found"));

        if(!receiverRepository.existsByMember_memId(memId)) {
            Receiver receiver = Receiver.builder()
                    .member(member)
                    .rcNm("기본 배송지")
                    .rcPhn(member.getMemMbPhn())
                    .rcAdr(member.getMemBizAdr())
                    .rcAdrDt(member.getMemBizDtAdr())
                    .rcCreDt(LocalDateTime.now())
                    .build();
            receiverRepository.save(receiver);
        }

    }

    public void addReciverAddr(ReceiverDto receiverDto) {
        Member member = memberRepository.findById(receiverDto.getMem_id()).orElseThrow(()-> new UsernameNotFoundException("User not found"));

        Receiver receiver = Receiver.builder()
                .member(member)
                .rcNm(receiverDto.getRcNm())
                .rcPhn(receiverDto.getRcPhn())
                .rcAdr(receiverDto.getRcAdr())
                .rcAdrDt(receiverDto.getRcAdrDt())
                .build();

        receiverRepository.save(receiver);
    }

    public List<ReceiverDto> getRecieverList(Long memId){
        Member member = memberRepository.findById(memId).orElseThrow(()-> new UsernameNotFoundException("User not found"));

        List<ReceiverDto> receiverDtoList = receiverRepository.findByMember_memIdOrderByRcCreDtDesc(memId).stream()
                .map(receiver -> mapToRecieverDto(receiver)).toList();

        return receiverDtoList;
    }

    public void updateReciever(ReceiverDto receiverDto){
        Receiver receiver = receiverRepository.findById(receiverDto.getRcId())
                .orElseThrow(()->new IllegalArgumentException("해당하는 배송지 내역이 없습니다."));

        if(receiverDto.getRcNm() != null) receiver.setRcNm(receiverDto.getRcNm());
        if(receiverDto.getRcAdr() != null) receiver.setRcAdr(receiverDto.getRcAdr());
        if(receiverDto.getRcAdrDt() != null) receiver.setRcAdrDt(receiver.getRcAdrDt());
        if(receiverDto.getRcPhn() != null) receiver.setRcPhn(receiver.getRcPhn());
    }

    public void deleteReciver(Long rcId){
        Receiver receiver = receiverRepository.findById(rcId).orElseThrow(()->new IllegalArgumentException("해당하는 배송지 내역이 없습니다."));
        receiver.setRcDelYn(true);
    }

    public ReceiverDto mapToRecieverDto(Receiver receiver){
        return ReceiverDto.builder()
                .rcNm(receiver.getRcNm())
                .rcPhn(receiver.getRcPhn())
                .rcAdr(receiver.getRcAdr())
                .rcAdrDt(receiver.getRcAdrDt())
                .build();
    }

}
