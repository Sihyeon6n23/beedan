-- =====================
-- 최준희 START
-- =====================

-- =====================
-- BRAND
-- =====================
INSERT INTO brand VALUES
(1, 'Kapital'),
(2, 'Nanamica'),
(3, 'Kenzo');
-- =====================
-- CATEGORY
-- =====================
INSERT INTO category VALUES
(1, 'Coat'),
(2, 'Jacket'),
(3, 'Hoodie'),
(4, 'Knitwear'),
(5, 'Shirt'),
(7, 'T-Shirt'),
(8, 'Bottoms'),
(9, 'Headwears'),
(10, 'Shoes'),
(11, 'Bags / Accessories');

-- =====================
-- STOCK
-- =====================
INSERT INTO stock (st_cd, br_id, st_br_nm, cat_id, st_cat_nm, st_nm, st_pr, st_cur, st_img_url, st_exp_yn, st_use_yn, st_del_yn, st_req_yn, st_req_mem_id, st_cra_dt, st_cre_dt, st_upd_dt, st_wis_cnt, st_pur_cnt) VALUES

-- Kapital (¥)
('KAP-001', 1, 'Kapital', '2', 'Jacket',      '25FW Mens Coach Jacket',   1749.99, '¥', 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=600&q=80', FALSE, FALSE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 10),
('KAP-002', 1, 'Kapital', '2', 'Jacket',      'Boro Padded Jacket',       2100.00, '¥', 'https://images.unsplash.com/photo-1548126032-079a0fb0099d?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 8,  5),
('KAP-003', 1, 'Kapital', '2', 'Jacket',      'Teru Teru Parka',          1980.00, '¥', 'https://images.unsplash.com/photo-1539109136-da888-59a3bc9f3a10?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6,  3),
('KAP-004', 1, 'Kapital', '4', 'Knitwear',     'Boro Mix Sweater',          890.00, '¥', 'https://images.unsplash.com/photo-1576566588028-4147f3842f27?auto=format&fit=crop&w=600&q=80', FALSE, FALSE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 14, 9),
('KAP-005', 1, 'Kapital', '4', 'Knitwear',     'Sashiko Knit Vest',         620.00, '¥', 'https://images.unsplash.com/photo-1583744946564-b52ac1c389c8?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 7,  4),
('KAP-006', 1, 'Kapital', '7', 'T-Shirt',         'Gauze Boro Shirt',          540.00, '¥', 'https://images.unsplash.com/photo-1596755389378-c31d21fd1273?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5,  2),
('KAP-007', 1, 'Kapital', '7', 'T-Shirt',         'Remake Flannel Shirt',      760.00, '¥', 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9,  6),
('KAP-008', 1, 'Kapital', '8', 'Bottoms',      'Kendo Pants',               980.00, '¥', 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11, 7),
('KAP-009', 1, 'Kapital', '8', 'Bottoms',      'Boro Remake Jeans',        1200.00, '¥', 'https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 13, 8),

-- Nanamica (¥)
('NAN-001', 2, 'Nanamica', '2', 'Jacket',     'GORE-TEX Cruiser Jacket',  3200.00, '¥', 'https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 20, 12),
('NAN-002', 2, 'Nanamica', '2', 'Jacket',     'Mountain Parka',           2800.00, '¥', 'https://images.unsplash.com/photo-1544923246-77307dd654cb?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 18, 10),
('NAN-003', 2, 'Nanamica', '2', 'Coat',     'Club Coat',                3600.00, '¥', 'https://images.unsplash.com/photo-1539533113208-f6df8cc8b543?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 15, 8),
('NAN-004', 2, 'Nanamica', '7', 'T-Shirt',        'COOLMAX Rugby Shirt',       640.00, '¥', 'https://images.unsplash.com/photo-1571945153237-4929e783af4a?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9,  5),
('NAN-005', 2, 'Nanamica', '7', 'T-Shirt',        'SUVIN Jersey Tee',          320.00, '¥', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 12, 7),
('NAN-006', 2, 'Nanamica', '8', 'Bottoms',     'COOLMAX Wide Chino',       1100.00, '¥', 'https://images.unsplash.com/photo-1506629082955-511b1aa562c8?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 6),
('NAN-007', 2, 'Nanamica', '8', 'Bottoms',     'Straight Tapered Chino',    980.00, '¥', 'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 8,  4),
('NAN-008', 2, 'Nanamica', '11', 'Bags / Accessories', 'Ruck Sack',                1400.00, '¥', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 16, 9),
('NAN-009', 2, 'Nanamica', '11', 'Bags / Accessories', 'Briefcase M',              1800.00, '¥', 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11, 6),

-- Kenzo (€)
('KNZ-001', 3, 'Kenzo', '2', 'Jacket',        'Boke Flower Peacoat',       890.00, '€', 'https://images.unsplash.com/photo-1520975954732-35dd22299614?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 22, 14),
('KNZ-002', 3, 'Kenzo', '2', 'Jacket',        'Tiger Mountain Parka',      720.00, '€', 'https://images.unsplash.com/photo-1467043237213-65f2da53396f?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 17, 11),
('KNZ-003', 3, 'Kenzo', '7', 'T-Shirt',           'Tiger Print Tee',           180.00, '€', 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 30, 20),
('KNZ-004', 3, 'Kenzo', '7', 'T-Shirt',           'Boke Flower Shirt',         260.00, '€', 'https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 19, 13),
('KNZ-005', 3, 'Kenzo', '4', 'Knitwear',       'Tiger Embroidery Sweater',  420.00, '€', 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 14, 9),
('KNZ-006', 3, 'Kenzo', '11', 'Bags / Accessories',    'Tiger Cap',                 120.00, '€', 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 25, 18),
('KNZ-007', 3, 'Kenzo', '11', 'Bags / Accessories',    'Boke Flower Scarf',         150.00, '€', 'https://images.unsplash.com/photo-1601924994987-69e26d50dc26?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 6);

-- =====================
-- CART (샘플)
-- mem_id 1: 5개 담음
-- mem_id 2: 4개 담음
-- mem_id 3: 3개 담음
-- =====================
INSERT INTO cart (mem_id, ca_st_qn, st_id) VALUES
-- 회원 1
(1, 2,  1),   -- Kapital 25FW Mens Coach Jacket
(1, 1,  4),   -- Kapital Boro Mix Sweater
(1, 3, 13),   -- Nanamica COOLMAX Rugby Shirt
(1, 1, 19),   -- Kenzo Boke Flower Peacoat
(1, 2, 23),   -- Kenzo Tiger Print Tee

-- 회원 2
(2, 1, 10),   -- Nanamica GORE-TEX Cruiser Jacket
(2, 2, 14),   -- Nanamica SUVIN Jersey Tee
(2, 1, 21),   -- Kenzo Tiger Mountain Parka
(2, 3, 25),   -- Kenzo Tiger Cap

-- 회원 3
(3, 1,  2),   -- Kapital Boro Padded Jacket
(3, 2,  8),   -- Kapital Kendo Pants
(3, 1, 17);

-- =====================
-- 시퀀스 리셋 (data.sql 직접 삽입 후 AUTO_INCREMENT 충돌 방지)
-- =====================

ALTER TABLE brand    ALTER COLUMN br_id  RESTART WITH 100;
ALTER TABLE category ALTER COLUMN cat_id RESTART WITH 100;
ALTER TABLE stock    ALTER COLUMN st_id  RESTART WITH 100;
ALTER TABLE cart     ALTER COLUMN ca_id  RESTART WITH 100;


-- =====================
-- 최준희 END
-- =====================

-- =====================
-- 백시현 START
-- =====================

INSERT INTO BUYER_GRADE_POLICY
(bgp_gr, bgp_min_ord_cnt, bgp_min_tt_am, bgp_ef_fr_dt, bgp_ac_yn, bgp_des)
VALUES
    ('VIP',      10, 30000000, CURRENT_TIMESTAMP(), TRUE, 'VIP: 10회 이상 OR 3,000만원 이상'),
    ('PREMIUM',   3,  5000000, CURRENT_TIMESTAMP(), TRUE, 'PREMIUM: 3회 이상 OR 500만원 이상'),
    ('STANDARD', NULL,   NULL, CURRENT_TIMESTAMP(), TRUE, 'STANDARD: 기본 등급');


INSERT INTO BUYER
(mem_biz_no, bgp_gr, mem_biz_ttl, by_ord_cnt, by_ttl_am, by_fr_dt, by_lt_dt)
VALUES
    ('123-45-67890', 'STANDARD', '주식회사 가나다', 0, 0, NULL, NULL),

    ('234-56-78901', 'PREMIUM', '주식회사 라마바', 5, 8000000, '2025-01-10 00:00:00', '2025-12-01 00:00:00'),

    ('345-67-89012', 'VIP', '주식회사 사아자', 12, 35000000, '2024-06-01 00:00:00', '2025-11-15 00:00:00');



INSERT INTO FEE_POLICY
(bgp_gr, fp_fee_ty, fp_calc_ty, fp_val, fp_ac_yn, fp_ef_fr_dt, fp_des)
VALUES
    ('STANDARD', 'SERVICE_COMMISSION', 'RATE',  0.0500, TRUE, CURRENT_TIMESTAMP(), '서비스 수수료 5%'),
    ('PREMIUM',  'SERVICE_COMMISSION', 'RATE',  0.0400, TRUE, CURRENT_TIMESTAMP(), '서비스 수수료 4%'),
    ('VIP',      'SERVICE_COMMISSION', 'RATE',  0.0300, TRUE, CURRENT_TIMESTAMP(), '서비스 수수료 3%'),
    ('STANDARD', 'SHIPPING',           'RATE',  0.0000, TRUE, CURRENT_TIMESTAMP(), '배송비 할인 없음'),
    ('PREMIUM',  'SHIPPING',           'RATE',  0.0500, TRUE, CURRENT_TIMESTAMP(), '배송비 5% 할인'),
    ('VIP',      'SHIPPING',           'RATE',  0.1000, TRUE, CURRENT_TIMESTAMP(), '배송비 10% 할인'),
    ('STANDARD', 'CUSTOMS',            'RATE',  0.0000, TRUE, CURRENT_TIMESTAMP(), '통관 할인 없음'),
    ('PREMIUM',  'CUSTOMS',            'RATE',  0.0500, TRUE, CURRENT_TIMESTAMP(), '통관 5% 할인'),
    ('VIP',      'CUSTOMS',            'RATE',  0.1000, TRUE, CURRENT_TIMESTAMP(), '통관 10% 할인');

INSERT INTO NEGOTIATION (ng_nm, ng_cre_dt, mem_id) VALUES
                                                       ('협상 1호', CURRENT_TIMESTAMP(), 1),
                                                       ('협상 2호', CURRENT_TIMESTAMP(), 1),
                                                       ('협상 3호', CURRENT_TIMESTAMP(), 2);

INSERT INTO UNIT_GROUP (un_g_nm, un_g_qn, un_g_yn, un_g_cr_dt, un_g_up_dt) VALUES
    ('다스', 12, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 다스(un_g_id=2 가정) 기준 할인 정책
INSERT INTO UNIT_DISCOUNT
(un_g_id, un_d_min_qn, un_d_min_am, un_d_qn_dr, un_d_am_dr, un_d_ov_ty, un_d_des, un_d_cr_dt, un_d_up_dt)
VALUES
    (1, 5,  NULL,    0.0300, 0.0000, 'HIGHER', '5다스 이상 수량 3% 할인',   CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (1, 10, NULL,    0.0500, 0.0000, 'HIGHER', '10다스 이상 수량 5% 할인',  CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (1, NULL, 500000, 0.0000, 0.0300, 'HIGHER', '50만원 이상 금액 3% 할인', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO FACTORY (br_id, fa_nm, fa_ad, fa_cty, fa_c_cd, fa_yn, fa_cr_dt, fa_up_dt) VALUES
                                                                                          (1, '도쿄 1공장', '도쿄 시부야구 1-1', '도쿄',   'JP', TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                          (1, '오사카 1공장', '오사카 난바 2-2', '오사카', 'JP', TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                          (2, '상하이 1공장', '상하이 푸동 3-3', '상하이', 'CN', TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO EXCHANGE_RATE (er_cr, er_ra, er_ba, er_f_dt, er_cr_dt) VALUES
                                                                       ('JPY', 9.012345, 'KRW', '2026-03-24 09:00:00', CURRENT_TIMESTAMP()),
                                                                       ('JPY', 9.123456, 'KRW', '2026-03-25 09:00:00', CURRENT_TIMESTAMP()),
                                                                       ('JPY', 9.234567, 'KRW', '2026-03-26 09:00:00', CURRENT_TIMESTAMP()),
                                                                       ('USD', 1380.123456, 'KRW', '2026-03-26 09:00:00', CURRENT_TIMESTAMP()),
                                                                       ('EUR', 1500.234567, 'KRW', '2026-03-26 09:00:00', CURRENT_TIMESTAMP()),
                                                                       ('CNY', 190.345678, 'KRW', '2026-03-26 09:00:00', CURRENT_TIMESTAMP());


INSERT INTO SHIPPING_RATE
(sr_c_cd, sr_trsp_ty, sr_sm_qn, sr_sm_am, sr_md_qn, sr_md_am, sr_lg_qn, sr_lg_am, sr_yn, sr_cr_dt, sr_up_dt)
VALUES
    ('JP', 'SEA', 1, 50000,  5, 80000,  10, 120000, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    ('JP', 'AIR', 1, 100000, 5, 150000, 10, 200000, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    ('CN', 'SEA', 1, 40000,  5, 70000,  10, 100000, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    ('CN', 'AIR', 1, 80000,  5, 120000, 10, 160000, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO PORT_CUSTOMS_RATE
(pcr_ty, pcr_sm_am, pcr_md_am, pcr_lg_am, pcr_yn, pcr_cr_dt, pcr_up_dt)
VALUES
    ('PORT',    30000, 50000, 70000, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    ('CUSTOMS', 40000, 60000, 80000, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    ('HS_CODE', 10000, 10000, 10000, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO DOMESTIC_DELIVERY_RATE
(ddr_rgn, ddr_am, ddr_e_am, ddr_yn, ddr_cr_dt, ddr_up_dt)
VALUES
    ('SEOUL',    3000, 0,    TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    ('GYEONGGI', 3000, 0,    TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    ('METRO',    3000, 0,    TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    ('PROVINCE', 3000, 0,    TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    ('JEJU',     3000, 3000, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    ('ISLAND',   3000, 5000, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO QU_BASE (ng_id, qu_sid, qu_rid, qu_stt, qu_op_yn, qu_cre_dt, qu_upd_dt) VALUES
                                                                                        (1, 1, 2, 'TEMP_SAVE',  FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                        (1, 1, 2, 'SUBMITTED',  TRUE,  CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                        (2, 2, 1, 'APPROVED',   TRUE,  CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());


INSERT INTO QU_INFO
(qu_id, ng_id, qu_info_exc_rt, qu_info_cur_cd, bgp_id, fp_id)
VALUES
    (1, 1, 9.234567, 'JPY', 1, 1),
    (2, 1, 1380.123456, 'USD', 2, 2);



INSERT INTO QU_DETAIL
(qu_info_id, qu_id, ng_id, st_id, st_nm, qu_dt_qn,
 fa_id, fa_nm, un_g_id, un_g_nm, qu_u_qn, qu_dt_fg_pr)
VALUES
    (1, 1, 1, 1, '면 티셔츠',  100, 1, '도쿄 1공장',  2, '다스', 5, 1200.0000),
    (1, 1, 1, 2, '청바지',     50,  1, '도쿄 1공장',  2, '다스', 3, 2500.0000),
    (1, 1, 1, 3, '후드티',     30,  3, '상하이 1공장',2, '다스', 2, 1800.0000);



-- =====================
-- 백시현 END
-- =====================



-- =====================
-- 고희권 START
-- =====================
-- 1. 관리자 계정 (ROOT)
INSERT INTO member (
    mem_lgn_id, mem_lgn_pw, mem_aut, mem_nm, mem_eml, mem_stt, mem_cre_dt
) VALUES (
             'admin',
             '$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2', -- 1234 (BCrypt)
             'ROOT',
             '최고관리자',
             'admin@example.com',
             'ACTIVE',
             NOW()
         );

-- 2. 일반 사용자 계정 (USER)
INSERT INTO member (
    mem_lgn_id, mem_lgn_pw, mem_aut, mem_nm, mem_mb_phn, mem_eml, mem_stt, mem_cre_dt
) VALUES (
             'user01',
             '$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2', -- 1234 (BCrypt)
             'USER',
             '홍길동',
             '010-1234-5678',
             'user01@example.com',
             'ACTIVE',
             NOW()
         );

-- 3. 비즈니스/기업 사용자 계정 (ADMIN 혹은 USER)
INSERT INTO member (
    mem_lgn_id, mem_lgn_pw, mem_aut, mem_nm, mem_biz_no, mem_biz_ttl, mem_biz_adr, mem_stt, mem_cre_dt
) VALUES (
             'biz_user',
             '$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2', -- 1234 (BCrypt)
             'ADMIN',
             '김철수',
             '123-45-67890',
             '(주)테스트컴퍼니',
             '서울시 강남구 역삼동',
             'ACTIVE',
             NOW()
         );

INSERT INTO member (
    mem_lgn_id, mem_lgn_pw, mem_aut, mem_nm, mem_eml, mem_stt, mem_cre_dt
) VALUES (
             'admin1',
             '$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2', -- 1234 (BCrypt)
             'ROOT',
             '최고관리자',
             'admin@example.com',
             'INACTIVE',
             NOW()
         );

INSERT INTO member (
    mem_lgn_id, mem_lgn_pw, mem_aut, mem_nm, mem_eml, mem_stt, mem_cre_dt
) VALUES (
             'admin2',
             '$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2', -- 1234 (BCrypt)
             'ROOT',
             '최고관리자',
             'admin@example.com',
             'PENDING',
             NOW()
         );
-- =====================
-- 고희권 END
-- =====================



-- =====================
-- 장 준 START
-- =====================
INSERT INTO chatbot_topic
(cb_tp_id, cb_tp_nm, cb_tp_lvl, cb_tp_ord, cb_tp_use_yn, cb_tp_cre_dt, cb_tp_upd_dt, cb_tp_prn_id)
VALUES
    (1, '배송 문의', 1, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
    (2, '견적 문의', 1, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
    (3, '결제 문의', 1, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
    (4, '회원정보 문의', 1, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
    (5, '배송 조회', 2, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (6, '통관/입고 일정', 2, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (7, '견적 요청 방법', 2, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (8, '최소 발주 수량', 2, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (9, '로그인/비밀번호', 2, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4),
    (10, '사업자 정보 변경', 2, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4);

INSERT INTO chatbot_response
(cb_res_id, cb_res_ttl, cb_res_con, cb_res_lnk_btn_nm, cb_res_lnk_url, cb_res_use_yn, cb_res_cre_dt, cb_res_upd_dt, cb_tp_id)
VALUES
    (1, '배송 조회 안내', '마이페이지의 주문/배송 내역에서 진행 상태를 확인하실 수 있습니다. 국내 배송 시작 이후에는 송장 정보가 순차적으로 반영되며, 현지 출고 직후에는 상태 반영까지 다소 시간이 걸릴 수 있습니다.', '주문 내역 보기', '/member/order/list', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5),
    (2, '통관 및 입고 일정 안내', '통관과 입고 일정은 상품 종류, 현지 출고 시점, 세관 상황에 따라 달라질 수 있습니다. 급한 일정이 있는 경우 상담사 연결을 통해 주문번호와 희망 납기일을 함께 남겨주시면 확인 후 안내드립니다.', NULL, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6),
    (3, '견적 요청 방법 안내', '원하시는 상품명, 브랜드, 수량, 옵션 정보를 준비하신 뒤 견적 요청 페이지에서 접수해 주세요. 요청 내용이 구체적일수록 상담과 산출이 더 빠르게 진행됩니다.', '견적 요청하기', '/quote/write', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 7),
    (4, '최소 발주 수량 안내', '최소 발주 수량은 브랜드와 상품마다 다를 수 있습니다. 대량 구매나 정기 발주를 검토 중이신 경우 상담사 연결을 통해 예상 수량을 알려주시면 확인 후 안내드립니다.', NULL, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 8),
    (5, '결제 안내', '결제 가능 수단과 결제 진행 상태는 결제 안내 화면에서 확인하실 수 있습니다. 카드 결제 오류나 입금 확인 지연이 있는 경우 상담사 연결을 통해 주문 정보와 함께 문의해 주세요.', '결제 내역 보기', '/payment/check', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    (6, '로그인/비밀번호 안내', '로그인이 되지 않거나 비밀번호를 분실하신 경우 로그인 화면의 찾기 기능을 이용해 주세요. 반복 로그인 실패로 계정이 잠긴 경우에는 상담사 연결을 통해 본인 확인 후 안내받으실 수 있습니다.', '로그인 페이지로 이동', '/auth/signin', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9),
    (7, '사업자 정보 변경 안내', '상호, 대표자명, 사업장 주소 등 사업자 정보 변경이 필요한 경우 마이페이지에서 수정 가능한 항목을 먼저 확인해 주세요. 증빙서류 확인이 필요한 변경 건은 상담사 연결 후 처리됩니다.', '마이페이지로 이동', '/mypage/main', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10);

ALTER TABLE chatbot_topic ALTER COLUMN cb_tp_id RESTART WITH 100;
ALTER TABLE chatbot_response ALTER COLUMN cb_res_id RESTART WITH 100;
-- =====================
-- 장 준 END
-- =====================




-- =====================
-- 임 욱 START
-- =====================
INSERT INTO ORDER_BASE (ord_base_rcv_nm, ord_base_adr, ord_base_adr_dt, mem_id, ord_base_no, ord_base_st)
VALUES ('주문자와 동일', '서울특별시 금천구 가산디지털2로 95', '3층 305호 구디아카데미', 1, '1234', 'PREPARING');

INSERT INTO NOTIFICATION (noti_ttl, noti_con, noti_rea_yn, noti_del_yn, noti_upd_mem_id, mem_id)
VALUES
    ('주문이 승인 되었습니다.', '요청하신 주문이 승인 처리 되었습니다.', FALSE, FALSE,  NULL, 1),
    ('상품 배송이 완료되었습니다.', '요청하신 상품 배송이 완료 되었습니다.', FALSE, FALSE, NULL, 1),
    ('견적 요청이 반려되었습니다.', '견적 상세를 통해 견적을 수정해주세요.', FALSE, FALSE,  NULL, 1),
    ('상품 배송이 완료되었습니다.', '요청하신 상품 배송이 완료 되었습니다.', TRUE, FALSE, NULL, 1),
    ('상품 배송이 완료되었습니다.', '요청하신 상품 배송이 완료 되었습니다.', FALSE, TRUE, NULL, 1);
-- =====================
-- 임 욱 END
-- =====================
