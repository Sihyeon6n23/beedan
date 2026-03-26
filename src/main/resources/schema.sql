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
