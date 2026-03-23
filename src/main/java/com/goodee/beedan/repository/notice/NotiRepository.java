package com.goodee.beedan.repository.notice;

import com.goodee.beedan.entity.Noti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotiRepository extends JpaRepository<Noti, Long> {
}
