package com.goodee.beedan.repository.webhook;

import com.goodee.beedan.entity.OrderWebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderWebhookLogRepository extends JpaRepository<OrderWebhookLog, Long> {

    List<OrderWebhookLog> findAllByQuIdOrderByOwlCreDtDesc(Long quId);

    List<OrderWebhookLog> findAllByOwlDirOrderByOwlCreDtDesc(String owlDir);
}
