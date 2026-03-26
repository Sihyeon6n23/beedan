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
('KAP-001', 1, 'Kapital', '1', 'Outwear',      '25FW Mens Coach Jacket',   1749.99, '¥', 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=600&q=80', FALSE, FALSE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 10),
('KAP-002', 1, 'Kapital', '1', 'Outwear',      'Boro Padded Jacket',       2100.00, '¥', 'https://images.unsplash.com/photo-1548126032-079a0fb0099d?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 8,  5),
('KAP-003', 1, 'Kapital', '1', 'Outwear',      'Teru Teru Parka',          1980.00, '¥', 'https://images.unsplash.com/photo-1539109136-da888-59a3bc9f3a10?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6,  3),
('KAP-004', 1, 'Kapital', '2', 'Knitwear',     'Boro Mix Sweater',          890.00, '¥', 'https://images.unsplash.com/photo-1576566588028-4147f3842f27?auto=format&fit=crop&w=600&q=80', FALSE, FALSE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 14, 9),
('KAP-005', 1, 'Kapital', '2', 'Knitwear',     'Sashiko Knit Vest',         620.00, '¥', 'https://images.unsplash.com/photo-1583744946564-b52ac1c389c8?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 7,  4),
('KAP-006', 1, 'Kapital', '3', 'Tops',         'Gauze Boro Shirt',          540.00, '¥', 'https://images.unsplash.com/photo-1596755389378-c31d21fd1273?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5,  2),
('KAP-007', 1, 'Kapital', '3', 'Tops',         'Remake Flannel Shirt',      760.00, '¥', 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9,  6),
('KAP-008', 1, 'Kapital', '4', 'Bottoms',      'Kendo Pants',               980.00, '¥', 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11, 7),
('KAP-009', 1, 'Kapital', '4', 'Bottoms',      'Boro Remake Jeans',        1200.00, '¥', 'https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 13, 8),

-- Nanamica (¥)
('NAN-001', 2, 'Nanamica', '1', 'Outwear',     'GORE-TEX Cruiser Jacket',  3200.00, '¥', 'https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 20, 12),
('NAN-002', 2, 'Nanamica', '1', 'Outwear',     'Mountain Parka',           2800.00, '¥', 'https://images.unsplash.com/photo-1544923246-77307dd654cb?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 18, 10),
('NAN-003', 2, 'Nanamica', '1', 'Outwear',     'Club Coat',                3600.00, '¥', 'https://images.unsplash.com/photo-1539533113208-f6df8cc8b543?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 15, 8),
('NAN-004', 2, 'Nanamica', '3', 'Tops',        'COOLMAX Rugby Shirt',       640.00, '¥', 'https://images.unsplash.com/photo-1571945153237-4929e783af4a?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9,  5),
('NAN-005', 2, 'Nanamica', '3', 'Tops',        'SUVIN Jersey Tee',          320.00, '¥', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 12, 7),
('NAN-006', 2, 'Nanamica', '4', 'Bottoms',     'COOLMAX Wide Chino',       1100.00, '¥', 'https://images.unsplash.com/photo-1506629082955-511b1aa562c8?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 6),
('NAN-007', 2, 'Nanamica', '4', 'Bottoms',     'Straight Tapered Chino',    980.00, '¥', 'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 8,  4),
('NAN-008', 2, 'Nanamica', '5', 'Accessories', 'Ruck Sack',                1400.00, '¥', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 16, 9),
('NAN-009', 2, 'Nanamica', '5', 'Accessories', 'Briefcase M',              1800.00, '¥', 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11, 6),

-- Kenzo (€)
('KNZ-001', 3, 'Kenzo', '1', 'Outwear',        'Boke Flower Peacoat',       890.00, '€', 'https://images.unsplash.com/photo-1520975954732-35dd22299614?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 22, 14),
('KNZ-002', 3, 'Kenzo', '1', 'Outwear',        'Tiger Mountain Parka',      720.00, '€', 'https://images.unsplash.com/photo-1467043237213-65f2da53396f?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 17, 11),
('KNZ-003', 3, 'Kenzo', '3', 'Tops',           'Tiger Print Tee',           180.00, '€', 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 30, 20),
('KNZ-004', 3, 'Kenzo', '3', 'Tops',           'Boke Flower Shirt',         260.00, '€', 'https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 19, 13),
('KNZ-005', 3, 'Kenzo', '2', 'Knitwear',       'Tiger Embroidery Sweater',  420.00, '€', 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 14, 9),
('KNZ-006', 3, 'Kenzo', '5', 'Accessories',    'Tiger Cap',                 120.00, '€', 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 25, 18),
('KNZ-007', 3, 'Kenzo', '5', 'Accessories',    'Boke Flower Scarf',         150.00, '€', 'https://images.unsplash.com/photo-1601924994987-69e26d50dc26?auto=format&fit=crop&w=600&q=80', TRUE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 6);

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
-- =====================
-- 장 준 END
-- =====================




-- =====================
-- 임 욱 START
-- =====================
INSERT INTO ORDER_BASE (ord_base_nm, ord_base_rcv_nm, ord_base_adr_da, ord_base_adr_dt, qu_dt_id, qu_info_id, qu_id, ng_id)
VALUES ('홍길동', '주문자와 동일', '서울특별시 금천구 가산디지털2로 95', '3층 305호 구디아카데미', 1, 1, 1, 1);


INSERT INTO NOTIFICATION (noti_ttl, noti_con, noti_rea_yn, noti_del_yn, noti_cre_dt, noti_upd_mem_id, mem_id)
VALUES
    ('주문이 승인 되었습니다.', '요청하신 주문이 승인 처리 되었습니다.', FALSE, FALSE, NOW(), NULL, 1),
    ('상품 배송이 완료되었습니다.', '요청하신 상품 배송이 완료 되었습니다.', FALSE, FALSE, NOW(), NULL, 1),
    ('견적 요청이 반려되었습니다.', '견적 상세를 통해 견적을 수정해주세요.', FALSE, FALSE, NOW(), NULL, 1),
    ('상품 배송이 완료되었습니다.', '요청하신 상품 배송이 완료 되었습니다.', TRUE, FALSE, NOW(), NULL, 1),
    ('상품 배송이 완료되었습니다.', '요청하신 상품 배송이 완료 되었습니다.', FALSE, TRUE, NOW(), NULL, 1);
-- =====================
-- 임 욱 END
-- =====================