-- =====================
-- 고희권 START
-- =====================
DROP TABLE IF EXISTS member;

CREATE TABLE member (
                        mem_id         BIGINT         NOT NULL AUTO_INCREMENT,
                        mem_lgn_id     VARCHAR(50)    NOT NULL,
                        mem_lgn_pw     VARCHAR(100)   NOT NULL,
                        mem_aut        ENUM('USER', 'ADMIN', 'ROOT') NOT NULL,
                        mem_nm         VARCHAR(50)    NULL,
                        mem_mb_phn     VARCHAR(50)    NULL,
                        mem_ci         VARCHAR(100)   NULL,
                        mem_eml        VARCHAR(50)    NULL,
                        mem_biz_no     VARCHAR(50)    NULL,
                        mem_biz_cre_dt DATETIME       NULL,
                        mem_biz_ttl    VARCHAR(50)    NULL,
                        mem_pos_cd     VARCHAR(100)   NULL,
                        mem_biz_adr    VARCHAR(100)   NULL,
                        mem_biz_dt_adr VARCHAR(100)   NULL,
                        mem_ceo_nm     VARCHAR(50)    NULL,
                        mem_ceo_phn    VARCHAR(50)    NULL,
                        mem_cmp_tel    VARCHAR(50)    NULL,
                        mem_lgn_tr     BIGINT         NOT NULL DEFAULT 0,
                        mem_loc_dt     DATETIME       NULL,
                        mem_stt        ENUM('ACTIVE', 'INACTIVE', 'LOCK', 'PENDING') NOT NULL DEFAULT 'PENDING',
                        mem_upd_id     BIGINT         NULL,
                        mem_upd_dt     DATETIME       DEFAULT CURRENT_TIMESTAMP,
                        mem_cre_dt     DATETIME       DEFAULT CURRENT_TIMESTAMP,
                        mem_upd_pw_dt  DATETIME       NULL,

                        PRIMARY KEY (mem_id),
                        CONSTRAINT uk_mem_lgn_id UNIQUE (mem_lgn_id) -- H2와 MySQL 모두 호환되는 문법
);

CREATE TABLE `file_upload` (
                               `file_id`         BIGINT          NOT NULL AUTO_INCREMENT, -- 자동 증가 추가
                               `brd_ref_ty`      VARCHAR(50)     NULL,
                               `brd_ref_no`      BIGINT          NULL,
                               `file_nm`         VARCHAR(100)    NULL,
                               `file_uuid`       VARCHAR(100)    NULL,
                               `file_ext`        VARCHAR(50)     NULL,
                               `mem_id`          BIGINT          NULL,
                               `file_cre_dt`     DATETIME        DEFAULT CURRENT_TIMESTAMP, -- 기본값 설정
                               `file_upd_mem_id` BIGINT          NULL,
                               `file_upd_dt`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               `file_del_yn`     BOOLEAN         DEFAULT FALSE, -- 기본값 FALSE(0)
                               `file_or`         INT             NULL,
                               `file_sz`         BIGINT          NULL,
                               `file_ctp`        VARCHAR(100)    NULL,
                               `file_pat`        VARCHAR(255)    NULL,
                               CONSTRAINT `PK_FILE_UPLOAD` PRIMARY KEY (`file_id`)
);
-- =====================
-- 고희권 END
-- =====================

-- =====================
-- 최준희 START
-- =====================
CREATE TABLE IF NOT EXISTS stock (
                                     st_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     st_cd        VARCHAR(50),
    br_id        BIGINT,
    st_br_nm     VARCHAR(20),
    cat_id       VARCHAR(20),
    st_cat_nm    VARCHAR(20),
    st_nm        VARCHAR(50),
    st_pr        DECIMAL(10,2),
    st_cur       VARCHAR(5),
    st_img_url   LONGTEXT,
    st_exp_yn    BOOLEAN,
    st_use_yn    BOOLEAN,
    st_del_yn    BOOLEAN,
    st_req_yn    BOOLEAN,
    st_req_mem_id BIGINT,
    st_cra_dt    DATETIME,
    st_cre_dt    DATETIME,
    st_upd_dt    DATETIME,
    st_wis_cnt   BIGINT,
    st_pur_cnt   BIGINT
    );

CREATE TABLE IF NOT EXISTS cart (
                                    ca_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    mem_id   BIGINT,
                                    ca_st_qn BIGINT,
                                    st_id    BIGINT,
                                    FOREIGN KEY (st_id) REFERENCES stock(st_id)
    );

CREATE TABLE IF NOT EXISTS brand (
                                     br_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     br_nm VARCHAR(25)
    );

CREATE TABLE IF NOT EXISTS category (
                                        cat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        cat_nm VARCHAR(25)
    );

CREATE TABLE IF NOT EXISTS crawling_url (
                                            url_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            url_url      VARCHAR(500),
    br_id        BIGINT,
    cat_id       BIGINT,
    url_sel_item VARCHAR(100),
    url_sel_nm   VARCHAR(100),
    url_sel_pr   VARCHAR(100),
    url_sel_img  VARCHAR(300),
    url_cur      VARCHAR(5),
    url_ty       VARCHAR(20),
    url_upd_dt   DATETIME,
    url_use_yn   BOOLEAN,
    url_del_yn   BOOLEAN,
    url_at_yn    BOOLEAN
);

CREATE TABLE IF NOT EXISTS wishlist (
    wi_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    st_id        BIGINT,
    mem_id       BIGINT,
    wi_cre_dt    DATETIME
);

CREATE TABLE IF NOT EXISTS requirement (
    req_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    req_ttl      VARCHAR(50),
    req_con      LONGTEXT,
    req_ref      LONGTEXT,
    req_pr       VARCHAR(30),
    req_stt      VARCHAR(30),
    req_rep_yn   BOOLEAN,
    req_per_yn   BOOLEAN,
    req_cre_dt   DATETIME,
    req_upd_dt   DATETIME,
    req_del_yn   BOOLEAN,
    mem_id       BIGINT,
    FOREIGN KEY (mem_id) REFERENCES member(mem_id)
);

-- =====================
-- 최준희 END
-- =====================



-- =====================
-- 백시현 START
-- =====================















-- =====================
-- 백시현 END
-- =====================






-- =====================
-- 장 준 START
-- =====================
CREATE TABLE chatbot_topic (
   cb_tp_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
   cb_tp_nm VARCHAR(100) NOT NULL,
   cb_tp_lvl INT NOT NULL,
   cb_tp_ord INT NOT NULL,
   cb_tp_use_yn BOOLEAN NOT NULL DEFAULT TRUE,
   cb_tp_cre_dt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   cb_tp_upd_dt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   cb_tp_prn_id BIGINT NULL, -- FK
   -- 1차 질의는 cb_tp_prn_id 가 NULL 이라 UNIQUE 만으로 순서 중복 방지가 불완전할 수 있으므로 서비스에서도 중복 검사 필요
   UNIQUE (cb_tp_prn_id, cb_tp_ord),
   CHECK (cb_tp_lvl IN (1, 2)),
   CHECK (
      (cb_tp_lvl = 1 AND cb_tp_prn_id IS NULL)
      OR
      (cb_tp_lvl = 2 AND cb_tp_prn_id IS NOT NULL)
   )
);

CREATE TABLE chatbot_response (
  cb_res_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  cb_res_ttl VARCHAR(255) NOT NULL,
  cb_res_con CLOB NOT NULL,
  cb_res_lnk_btn_nm VARCHAR(100) NULL,
  cb_res_lnk_url VARCHAR(255) NULL,
  cb_res_use_yn BOOLEAN NOT NULL DEFAULT TRUE,
  cb_res_cre_dt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cb_res_upd_dt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cb_tp_id BIGINT NOT NULL UNIQUE, -- FK
  CHECK (
    (cb_res_lnk_btn_nm IS NULL AND cb_res_lnk_url IS NULL)
    OR
    (cb_res_lnk_btn_nm IS NOT NULL AND cb_res_lnk_url IS NOT NULL)
  )
);

CREATE TABLE chat_room (
   ch_ro_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
   ch_ro_ttl VARCHAR(255) NOT NULL,
   ch_ro_stt VARCHAR(20) NOT NULL,
   ch_ro_cre_dt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   ch_ro_asg_dt TIMESTAMP NULL,
   ch_ro_cls_dt TIMESTAMP NULL,
   ch_ro_cls_rsn VARCHAR(20) NULL,
   ch_ro_last_ms_dt TIMESTAMP NULL,
   mem_id BIGINT NOT NULL, -- FK
   mem_ad_id BIGINT NULL, -- FK
   CHECK (ch_ro_stt IN ('OPEN', 'ONGOING', 'CLOSED')),
   CHECK (ch_ro_cls_rsn IS NULL OR ch_ro_cls_rsn IN ('USER', 'ADMIN', 'AUTO')),
   CHECK (
      (ch_ro_stt IN ('OPEN', 'ONGOING') AND ch_ro_cls_dt IS NULL AND ch_ro_cls_rsn IS NULL)
      OR
      (ch_ro_stt = 'CLOSED' AND ch_ro_cls_dt IS NOT NULL AND ch_ro_cls_rsn IS NOT NULL)
   ),
   CHECK (
       (ch_ro_stt = 'OPEN' AND mem_ad_id IS NULL)
           OR
       (ch_ro_stt = 'ONGOING' AND mem_ad_id IS NOT NULL)
           OR
       (ch_ro_stt = 'CLOSED')
   )
);

CREATE TABLE chat_message (
  ch_ms_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  ch_ms_sen_ty VARCHAR(20) NOT NULL,
  ch_ms_con CLOB NOT NULL,
  ch_ms_cre_dt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ch_ro_id BIGINT NOT NULL, -- FK
  mem_id BIGINT NOT NULL, -- FK
  CHECK (ch_ms_sen_ty IN ('USER', 'ADMIN'))
);

CREATE TABLE chat_room_read_status (
   ch_ro_re_st_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
   ch_ro_re_st_last_dt TIMESTAMP NULL,
   ch_ro_re_st_unr_yn BOOLEAN NOT NULL DEFAULT FALSE,
   ch_ro_id BIGINT NOT NULL, -- FK
   mem_id BIGINT NOT NULL, -- FK
   ch_ms_last_id BIGINT NULL, -- FK
   UNIQUE (mem_id, ch_ro_id)
);
-- =====================
-- 장 준 END
-- =====================



-- =====================
-- 임 욱 START
-- =====================
CREATE TABLE `NOTIFICATION`
(
    `noti_id` BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    `noti_ttl`        VARCHAR(30) NULL,
    `noti_con`        TEXT NULL,
    `noti_ref`        VARCHAR(30) NULL,
    `noti_rea_yn`     BOOLEAN DEFAULT FALSE,
    `noti_del_yn`     BOOLEAN DEFAULT FALSE,
    `noti_cre_dt`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `noti_upd_dt`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `noti_upd_mem_id` VARCHAR(10) NULL,
    `mem_id`          BIGINT NULL
);
CREATE TABLE `ORDER_BASE`
(
    `ord_base_id`     BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    `ord_base_rcv_nm` VARCHAR(10) NULL,
    `ord_base_adr`    VARCHAR(50) NULL,
    `ord_base_adr_dt` VARCHAR(50) NULL,
    `ord_base_cre_dt` DATETIME NULL,
    `ord_base_upd_dt` DATETIME NULL,
    `ord_base_stt`    ENUM('PREPARING', 'DELIVERING', 'DELIVERED') NOT NULL DEFAULT 'PREPARING',
    `ord_base_can_yn` BOOLEAN DEFAULT FALSE,
    `ord_base_no`     VARCHAR(10) NULL,
    `ord_base_msg`    TEXT NULL,
    `mem_id`          BIGINT NOT NULL,
    `ord_base_tt_am`  DECIMAL(18, 0) NULL
);

CREATE TABLE `RECEIVER` (
    `rc_id`	BIGINT	GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    `rc_nm`	VARCHAR(50)	NULL,
    `rc_phn`	VARCHAR(20)	NULL,
    `rc_msg`	TEXT	NULL,
    `rc_pos_cd` VARCHAR(20)	NULL,
    `rc_adr`	VARCHAR(50)	NULL,
    `rc_adr_dt`	VARCHAR(50)	NULL,
    `rc_rgn`	VARCHAR(20) NULL,
    `rc_cre_dt`	DATETIME	DATETIME DEFAULT CURRENT_TIMESTAMP,
    `rc_upd_dt`	DATETIME	DATETIME DEFAULT CURRENT_TIMESTAMP,
    `rc_adr_df_yn` BOOLEAN DEFAULT FALSE NULL,
    `rc_del_yn`	BOOLEAN DEFAULT FALSE,
    `mem_id`	BIGINT	NOT NULL
);

CREATE TABLE `SHIPMENT` (
     `sh_id`	BIGINT AUTO_INCREMENT PRIMARY KEY,
     `sh_tra_no`	VARCHAR(12)	NULL,
     `sh_car_cd`	VARCHAR(6)	NULL,
     `sh_stt`       ENUM('DELIVERED', 'DELIVERING', 'SHIPPING', 'PREPARING') NOT NULL DEFAULT 'PREPARING',
     `sh_can_yn`    BOOLEAN DEFAULT FAlSE,
     `sh_cre_dt`	DATETIME DEFAULT CURRENT_TIMESTAMP,
     `sh_upd_dt`	DATETIME DEFAULT CURRENT_TIMESTAMP,
     `ord_base_id`	BIGINT	NOT NULL
);

CREATE TABLE `ORDER_ITEM` (
                              `ord_item_id`     BIGINT AUTO_INCREMENT PRIMARY KEY,
                              `ord_base_id`     BIGINT NOT NULL,
                              `st_nm`           BIGINT NULL,
                              `ord_item_qn`     BIGINT NULL, -- 총 주문 수량
                              `ord_item_am`     DECIMAL NULL,  -- 총 결제금액
                              `ord_item_cre_dt` DATETIME DEFAULT CURRENT_TIMESTAMP,
                              `ord_item_upd_dt` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `SHIPMENT_ITEM` (
                                 `sh_item_id`      BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 `sh_id`           BIGINT NOT NULL,
                                 `ord_item_id`     BIGINT NOT NULL,
                                 `sh_qn`           BIGINT NOT NULL  -- 해당 배송지로 가는 수량 ( 분할 수량 )
);
-- =====================
-- 임 욱 END
-- =====================
