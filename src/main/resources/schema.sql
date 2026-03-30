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
-- =====================
-- 최준희 END
-- =====================



-- =====================
-- 백시현 START
-- =====================
DROP TABLE IF EXISTS BUYER_GRADE_POLICY;

CREATE TABLE BUYER_GRADE_POLICY (
                                    bgp_id          BIGINT          NOT NULL AUTO_INCREMENT,
                                    bgp_gr          VARCHAR(20)     NOT NULL,
                                    bgp_min_ord_cnt INT             NULL,
                                    bgp_min_tt_am   DECIMAL(18, 0)  NULL,
                                    bgp_ef_fr_dt    DATETIME        NULL,
                                    bgp_ef_to_dt    DATETIME        NULL,
                                    bgp_des         TEXT            NULL,
                                    bgp_ac_yn       BOOLEAN         NULL,
                                    bgp_cr_dt       DATETIME        NULL,
                                    bgp_up_dt       DATETIME        NULL,

                                    PRIMARY KEY (bgp_id)
);
DROP TABLE IF EXISTS BUYER;

CREATE TABLE BUYER (
                       by_id       BIGINT          NOT NULL AUTO_INCREMENT,
                       mem_biz_no  VARCHAR(50)     NOT NULL,
                       bgp_gr      VARCHAR(20)     NOT NULL,
                       mem_biz_ttl VARCHAR(50)     NULL,
                       by_ord_cnt  INT             NOT NULL DEFAULT 0,
                       by_ttl_am   DECIMAL(18, 0)  NOT NULL DEFAULT 0,
                       by_fr_dt    DATETIME        NULL,
                       by_lt_dt    DATETIME        NULL,

                       PRIMARY KEY (by_id)
);

DROP TABLE IF EXISTS FEE_POLICY;

CREATE TABLE FEE_POLICY (
                            fp_id       BIGINT          NOT NULL AUTO_INCREMENT,
                            bgp_gr      VARCHAR(20)     NULL,
                            fp_fee_ty   VARCHAR(50)     NULL,
                            fp_calc_ty  VARCHAR(10)     NULL,
                            fp_val      DECIMAL(10, 4)  NULL,
                            fp_ac_yn    BOOLEAN         NULL,
                            fp_ef_fr_dt DATETIME        NULL,
                            fp_ef_to_dt DATETIME        NULL,
                            fp_des      TEXT            NULL,
                            fp_cr_dt    DATETIME        NULL,
                            fp_up_dt    DATETIME        NULL,

                            PRIMARY KEY (fp_id)
);

DROP TABLE IF EXISTS NEGOTIATION;

CREATE TABLE NEGOTIATION (
                             ng_id       BIGINT          NOT NULL AUTO_INCREMENT,
                             ng_nm       VARCHAR(50)     NULL,
                             ng_cre_dt   DATETIME        NULL,
                             ng_end_dt   DATETIME        NULL,
                             mem_id      BIGINT          NULL,

                             PRIMARY KEY (ng_id)
);

DROP TABLE IF EXISTS UNIT_GROUP;

CREATE TABLE UNIT_GROUP (
                            un_g_id     BIGINT          NOT NULL AUTO_INCREMENT,
                            un_g_nm     VARCHAR(50)     NULL UNIQUE,
                            un_g_qn     INT             NULL,
                            un_g_yn     BOOLEAN         NULL,
                            un_g_cr_dt  DATETIME        NULL,
                            un_g_up_dt  DATETIME        NULL,

                            PRIMARY KEY (un_g_id)
);

DROP TABLE IF EXISTS UNIT_DISCOUNT;

CREATE TABLE UNIT_DISCOUNT (
                               un_d_id     BIGINT          NOT NULL AUTO_INCREMENT,
                               un_g_id     BIGINT          NULL,
                               un_d_min_qn INT             NULL,
                               un_d_min_am DECIMAL(18, 0)  NULL,
                               un_d_qn_dr  DECIMAL(10, 4)  NULL,
                               un_d_am_dr  DECIMAL(10, 4)  NULL,
                               un_d_ov_ty  VARCHAR(20)     NULL,
                               un_d_des    TEXT            NULL,
                               un_d_cr_dt  DATETIME        NULL,
                               un_d_up_dt  DATETIME        NULL,

                               PRIMARY KEY (un_d_id)
);
DROP TABLE IF EXISTS FACTORY;

CREATE TABLE FACTORY (
                         fa_id       BIGINT          NOT NULL AUTO_INCREMENT,
                         br_id       BIGINT          NULL,
                         fa_nm       VARCHAR(100)    NULL,
                         fa_ad       VARCHAR(255)    NULL,
                         fa_cty      VARCHAR(50)     NULL,
                         fa_c_cd     VARCHAR(10)     NULL,
                         fa_yn       BOOLEAN         NULL,
                         fa_cr_dt    DATETIME        NULL,
                         fa_up_dt    DATETIME        NULL,

                         PRIMARY KEY (fa_id)
);
DROP TABLE IF EXISTS EXCHANGE_RATE;

CREATE TABLE EXCHANGE_RATE (
                               er_id       BIGINT          NOT NULL AUTO_INCREMENT,
                               er_cr       VARCHAR(10)     NULL,
                               er_ra       DECIMAL(18, 6)  NULL,
                               er_ba       VARCHAR(10)     NULL,
                               er_f_dt     DATETIME        NULL,
                               er_cr_dt    DATETIME        NULL,

                               PRIMARY KEY (er_id)
);
DROP TABLE IF EXISTS SHIPPING_RATE;

CREATE TABLE SHIPPING_RATE (
                               sr_id       BIGINT          NOT NULL AUTO_INCREMENT,
                               sr_c_cd     VARCHAR(10)     NULL,
                               sr_trsp_ty  VARCHAR(10)     NULL,
                               sr_sm_qn    INT             NULL,
                               sr_sm_am    DECIMAL(18, 0)  NULL,
                               sr_md_qn    INT             NULL,
                               sr_md_am    DECIMAL(18, 0)  NULL,
                               sr_lg_qn    INT             NULL,
                               sr_lg_am    DECIMAL(18, 0)  NULL,
                               sr_yn       BOOLEAN         NULL,
                               sr_des      TEXT            NULL,
                               sr_cr_dt    DATETIME        NULL,
                               sr_up_dt    DATETIME        NULL,

                               PRIMARY KEY (sr_id)
);

DROP TABLE IF EXISTS PORT_CUSTOMS_RATE;

CREATE TABLE PORT_CUSTOMS_RATE (
                                   pcr_id      BIGINT          NOT NULL AUTO_INCREMENT,
                                   pcr_ty      VARCHAR(20)     NULL,
                                   pcr_sm_am   DECIMAL(18, 0)  NULL,
                                   pcr_md_am   DECIMAL(18, 0)  NULL,
                                   pcr_lg_am   DECIMAL(18, 0)  NULL,
                                   pcr_yn      BOOLEAN         NULL,
                                   pcr_des     TEXT            NULL,
                                   pcr_cr_dt   DATETIME        NULL,
                                   pcr_up_dt   DATETIME        NULL,

                                   PRIMARY KEY (pcr_id)
);

DROP TABLE IF EXISTS DOMESTIC_DELIVERY_RATE;

CREATE TABLE DOMESTIC_DELIVERY_RATE (
                                        ddr_id      BIGINT          NOT NULL AUTO_INCREMENT,
                                        ddr_rgn     VARCHAR(20)     NULL,
                                        ddr_am      DECIMAL(18, 0)  NULL,
                                        ddr_e_am    DECIMAL(18, 0)  NULL,
                                        ddr_des     TEXT            NULL,
                                        ddr_yn      BOOLEAN         NULL,
                                        ddr_cr_dt   DATETIME        NULL,
                                        ddr_up_dt   DATETIME        NULL,

                                        PRIMARY KEY (ddr_id)
);

DROP TABLE IF EXISTS QU_BASE;

CREATE TABLE QU_BASE (
                         qu_id       BIGINT          NOT NULL AUTO_INCREMENT,
                         ng_id       BIGINT          NULL,
                         qu_sid      BIGINT          NULL,
                         qu_rid      BIGINT          NULL,
                         qu_stt      VARCHAR(20)     NULL,
                         qu_exp_dt   DATETIME        NULL,
                         qu_op_yn    BOOLEAN         NULL,
                         qu_con      TEXT            NULL,
                         qu_cre_dt   DATETIME        NULL,
                         qu_upd_dt   DATETIME        NULL,

                         PRIMARY KEY (qu_id)
);

DROP TABLE IF EXISTS QU_INFO;

CREATE TABLE QU_INFO (
                         qu_info_id          BIGINT          NOT NULL AUTO_INCREMENT,
                         qu_id               BIGINT          NOT NULL,
                         ng_id               BIGINT          NOT NULL,
                         qu_info_exc_rt      DECIMAL(18, 6)  NULL,
                         qu_info_cur_cd      VARCHAR(10)     NULL,
                         bgp_id              BIGINT          NULL,
                         fp_id               BIGINT          NULL,
                         qu_info_srv_fe      DECIMAL(18, 0)  NULL,
                         qu_info_srv_fe_r    DECIMAL(10, 4)  NULL,
                         qu_info_srv_fe_am   DECIMAL(18, 0)  NULL,
                         qu_info_dd_am       DECIMAL(18, 0)  NULL,
                         qu_info_dd_ex_am    DECIMAL(18, 0)  NULL,
                         qu_info_dom_shi_fe  DECIMAL(18, 0)  NULL,
                         qu_info_int_shi_fe  DECIMAL(18, 0)  NULL,
                         qu_info_ttl_shi_fe  DECIMAL(18, 0)  NULL,
                         qu_info_tax         DECIMAL(18, 0)  NULL,
                         qu_info_tp          DECIMAL(18, 0)  NULL,
                         qu_info_dis_tp      DECIMAL(18, 0)  NULL,
                         qu_info_ps          TEXT            NULL,
                         qu_info_dsr_dt      DATETIME        NULL,

                         PRIMARY KEY (qu_info_id)
);

DROP TABLE IF EXISTS QU_DETAIL;

CREATE TABLE QU_DETAIL (
                           qu_dt_id        BIGINT          NOT NULL AUTO_INCREMENT,
                           qu_info_id      BIGINT          NOT NULL,
                           qu_id           BIGINT          NOT NULL,
                           ng_id           BIGINT          NOT NULL,
                           st_id           BIGINT          NULL,
                           st_nm           VARCHAR(50)     NULL,
                           qu_dt_qn        INT             NULL,
                           fa_id           BIGINT          NULL,
                           fa_nm           VARCHAR(100)    NULL,
                           un_g_id         BIGINT          NULL,
                           un_g_nm         VARCHAR(20)     NULL,
                           qu_u_qn         INT             NULL,
                           qu_dt_fg_pr     DECIMAL(18, 4)  NULL,
                           qu_dt_kr_pr     DECIMAL(18, 0)  NULL,
                           qu_dt_pr        DECIMAL(18, 0)  NULL,
                           qu_dt_re        TEXT            NULL,

                           PRIMARY KEY (qu_dt_id)
);

DROP TABLE IF EXISTS QU_SHIP_FEE;

CREATE TABLE QU_SHIP_FEE (
                             qsf_id      BIGINT          NOT NULL AUTO_INCREMENT,
                             qu_info_id  BIGINT          NULL,
                             qu_id       BIGINT          NULL,
                             ng_id       BIGINT          NULL,
                             fa_id       BIGINT          NULL,
                             qsf_fa_nm   VARCHAR(100)    NULL,
                             qsf_fa_c_cd VARCHAR(10)     NULL,
                             qsf_trsp_ty VARCHAR(10)     NULL,
                             qsf_un_qn   INT             NULL,
                             qsf_sr_am   DECIMAL(18, 0)  NULL,
                             qsf_sr_yn   BOOLEAN         NULL,
                             qsf_sr_des  TEXT            NULL,
                             qsf_prt_am  DECIMAL(18, 0)  NULL,
                             qsf_cst_am  DECIMAL(18, 0)  NULL,
                             qsf_hs_cd   DECIMAL(18, 0)  NULL,
                             qsf_ins_yn  BOOLEAN         NULL,
                             qsf_ins_am  DECIMAL(18, 0)  NULL,
                             qsf_cif_am  DECIMAL(18, 0)  NULL,
                             qsf_dty_r   DECIMAL(10, 4)  NULL,
                             qsf_dty     DECIMAL(18, 0)  NULL,
                             qsf_vat     DECIMAL(18, 0)  NULL,
                             qsf_dsc_r   DECIMAL(10, 4)  NULL,
                             qsf_dsc_am  DECIMAL(18, 0)  NULL,
                             qsf_ttl     DECIMAL(18, 0)  NULL,
                             qsf_cr_dt   DATETIME        NULL,
                             qsf_up_dt   DATETIME        NULL,

                             PRIMARY KEY (qsf_id)
);

-- =====================
-- 백시현 END
-- =====================



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
-- =====================
-- 고희권 END
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
    `noti_rea_yn`     BOOLEAN NULL,
    `noti_del_yn`     BOOLEAN NULL,
    `noti_cre_dt`     DATETIME NULL,
    `noti_upd_dt`     DATETIME NULL,
    `noti_upd_mem_id` VARCHAR(4) NULL,
    `mem_id`          BIGINT NULL
);

CREATE TABLE `ORDER_BASE`
(
    `ord_base_id`     BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    `ord_base_rcv_nm` VARCHAR(10) NULL,
    `ord_base_adr` VARCHAR(20) NULL,
    `ord_base_adr_dt` VARCHAR(20) NULL,
    `ord_base_cre_dt` DATETIME NULL,
    `ord_base_upd_dt` DATETIME NULL,
    `ord_base_st`     ENUM('PREPARING', 'DELIVERING', 'DELIVERIED', 'CANCELLED'),
    `ord_base_no`     VARCHAR(10) NULL,
    `ord_base_msg`    TEXT NULL,
    `qu_dt_id`        BIGINT NULL,
    `qu_info_id`      BIGINT NULL,
    `qu_id`           BIGINT NULL,
    `ng_id`           BIGINT NULL,
    `mem_id`          BIGINT NOT NULL
);

CREATE TABLE `RECIEVER` (
    `rc_id`	BIGINT	GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    `rc_nm`	VARCHAR(50)	NULL,
    `rc_phn`	VARCHAR(20)	NULL,
    `rc_msg`	TEXT	NULL,
    `rc_adr`	VARCHAR(20)	NULL,
    `rc_adr_dt`	VARCHAR(20)	NULL,
    `rc_cre_dt`	DATETIME	NULL,
    `rc_upd_dt`	DATETIME	NULL,
    `rc_del_yn`	BOOLEAN NULL,
    `mem_id`	BIGINT	NOT NULL
);

-- =====================
-- 임 욱 END
-- =====================
