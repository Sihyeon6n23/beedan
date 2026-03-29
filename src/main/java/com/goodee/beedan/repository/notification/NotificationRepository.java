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
    @Query("SELECT n FROM Notification n " +
            "WHERE n.member.memId = :memId " +
            "AND n.notiDelYn = false " +
            "ORDER BY n.notiCreDt DESC")
    List<Notification> findAllNotDeletedByMemId(Long memId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.notiReaYn = true WHERE n.member.memId = :memId AND n.notiReaYn = false AND n.notiDelYn = false")
    void updateAllReaYnByMemId(@Param("memId") Long memId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n " +
            "SET n.notiDelYn = true " +
            "WHERE n.member.memId = :memId " +
            "AND n.notiDelYn = false")
    void updateAllDelYnByMemId(@Param("memId") Long memId);

    int countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(Long memId);
}