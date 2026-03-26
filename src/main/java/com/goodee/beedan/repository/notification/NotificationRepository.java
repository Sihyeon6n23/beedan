package com.goodee.beedan.repository.notification;

import com.goodee.beedan.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMember_MemIdAndNotiDelYnFalseOrderByNotiCreDtDesc(Long memId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.notiReaYn = true WHERE n.member.memId = :memId AND n.notiReaYn = false AND n.notiDelYn = false")
    int updateAllRedYnByMemId(@Param("memId") Long memId);

    int countByMember_MemIdAndNotiReaYnFalse(Long memId);
}
