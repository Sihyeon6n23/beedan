package com.goodee.beedan.service.notice;

import com.goodee.beedan.common.constant.NoticeType;
import com.goodee.beedan.dto.notice.NoticeDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Notice;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.notice.NotiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NoticeService {
    private final NotiRepository notiRepository;
    private final MemberRepository memberRepositroy;

    public List<NoticeDto> getNotiList(Long memId){
        memberRepositroy.findById(memId).orElseThrow(()->new UsernameNotFoundException("Not user found"));
        List<NoticeDto> notiList = notiRepository.findByMember_MemIdAndNotiDelYnFalseOrderByNotiCreDtDesc(memId)
                .stream()
                .map(notice -> mapToNoticeDto(notice))
                .toList();

        return notiList;
    }

    public void createNoti(Long memId, NoticeType notiTp, String detail, Long targetId){
        Member member = memberRepositroy.findById(memId).orElseThrow(()-> new UsernameNotFoundException("User not found"));

        String content = notiTp.generateContent(detail);
        String refUrl = notiTp.generateUrl(targetId);

        Notice notice = Notice.builder()
                .member(member)
                .notiTtl(notiTp.getDefaultTitle())
                .notiCon(content)
                .notiRef(refUrl)
                .notiReaYn(false)
                .notiDelYn(false)
                .notiCreDt(LocalDateTime.now())
                .build();

        log.info("알림 생성 테스트: {}", notice.toString());

        notiRepository.save(notice);
    }

    public NoticeDto mapToNoticeDto(Notice notice){
        return NoticeDto.builder()
                .notiId(notice.getNotiId())
                .notiTtl(notice.getNotiTtl())
                .notiCon(notice.getNotiCon())
                .notiReaYn(notice.getNotiReaYn())
                .notiCreAt(notice.getNotiCreDt())
                .notiUptDt(notice.getNotiUpdDt())
                .build();
    }

    public void deleteNoti(Long notiId) {
        Notice notice = notiRepository.findById(notiId).orElseThrow(() -> new IllegalArgumentException("Can't find notice"));
        notice.setNotiDelYn(true);
    }

    public String updateNotiReaYn(Long notiId){
        Notice notice = notiRepository.findById(notiId).orElseThrow(()->new IllegalArgumentException("Can't find notice"));

        notice.setNotiReaYn(true);

        return notice.getNotiRef();
    }

    public int updateAllNotiReaYn(Long memId){
        return notiRepository.updateAllRedYnByMemId(memId);
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(Long memId) {
        return notiRepository.countByMember_MemIdAndNotiReaYnFalse(memId);
    }

    public void updateNoti(Long notiId, Long upd_mem_id, NoticeDto updateDto){
        Notice notice = notiRepository.findById(notiId).orElseThrow(()-> new IllegalArgumentException("Can't find notice"));

        notice.setNotiTtl(updateDto.getNotiTtl());
        notice.setNotiCon(updateDto.getNotiCon());
        notice.setNotiUpdMemId(upd_mem_id);
        notice.setNotiUpdDt(LocalDateTime.now());
    }

}
