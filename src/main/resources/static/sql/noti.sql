CREATE TABLE `NOTI`
(
    `noti_id`         BIGINT     NOT NULL,
    `noti_ttl`        VARCHAR(30) NULL,
    `noti_con`        TEXT NULL,
    `noti_ref`        VARCHAR(30) NULL,
    `noti_rea_yn`     BOOLEAN NULL,
    `noti_del_yn`     BOOLEAN NULL,
    `noti_cre_dt`     DATETIME   not NULL,
    `noti_upd_dt`     DATETIME NULL,
    `noti_upd_mem_id` VARCHAR(4) NOT NULL,
    `mem_id`          BIGINT     NOT NULL
);

insert into noti
values (1, '주문 승인이 완료되었습니다.', '요청하신 주문이 승인 처리 되었습니다.', null, FALSE, FALSE, now(), null, 1, 1,
        2, '상품 배송이 완료되었습니다.', '요청하신 상품 배송이 완료 되었습니다.', null, FALSE, FALSE, now(), null, 1, 1);