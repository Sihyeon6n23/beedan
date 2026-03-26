package com.goodee.beedan.service.notice;

import com.goodee.beedan.dto.notice.NotiDto;
import com.goodee.beedan.entity.Noti;
import com.goodee.beedan.repository.notice.NotiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotiService {
    private final NotiRepository notiRepository;

    public List<NotiDto> getNotiList(Long id){
        List<NotiDto> notiList = notiRepository.findById(id)
                .stream().map(noti -> mapToNotiDto(noti))
                .toList();

        return notiList;
    }

    public NotiDto mapToNotiDto(Noti noti){
        NotiDto notiDto = NotiDto.builder()
                .notiId(noti.getNotiId())
                .notiTtl(noti.getNotiTtl())
                .notiCon(noti.getNotiCon())
                .notiReaYn(noti.getNotiReaYn())
                .notiDelYn(noti.getNotiDelYn())
                .notiCreAt(noti.getNotiCreDt())
                .notiUptDt(noti.getNotiUpdDt())
                .build();

        return notiDto;
    }
}
