-- =====================
-- 최준희 START
-- =====================
CREATE TABLE IF NOT EXISTS stock (
                                     st_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     st_cd        VARCHAR(50),
    br_id        BIGINT,
    st_br_nm     VARCHAR(20),
    st_cat       VARCHAR(20),
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
    url_cur      VARCHAR(5)
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
-- =====================
-- 장 준 END
-- =====================




-- =====================
-- 임 욱 START
-- =====================
-- =====================
-- 임 욱 END
-- =====================