-- =====================
-- 고희권 START
-- =====================
-- 1. 최고 관리자 계정 (ROOT)
INSERT INTO member (
    mem_lgn_id, mem_lgn_pw, mem_aut, mem_nm, mem_eml, mem_stt, mem_cre_dt
) VALUES (
             'root',
             '$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2', -- 1234 (BCrypt)
             'ROOT',
             '최고관리자',
             'admin@example.com',
             'ACTIVE',
             NOW()
         );

-- 2. 일반 관리자 계정 (ADMIN)
INSERT INTO member (
    mem_lgn_id, mem_lgn_pw, mem_aut, mem_nm, mem_mb_phn, mem_eml, mem_stt, mem_cre_dt
) VALUES (
             'admin',
             '$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2', -- 1234 (BCrypt)
             'ADMIN',
             '홍만수',
             '010-1234-5678',
             'user01@example.com',
             'ACTIVE',
             NOW()
         );

-- 3. 비즈니스/기업 사용자 계정 (USER)
INSERT INTO member (
    mem_lgn_id, mem_lgn_pw, mem_aut, mem_nm, mem_biz_no, mem_biz_ttl, mem_biz_adr, mem_stt, mem_cre_dt
) VALUES (
             'user',
             '$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2', -- 1234 (BCrypt)
             'USER',
             '김철수',
             '123-45-67890',
             '(주)테스트컴퍼니',
             '서울시 강남구 테헤란로',
             'ACTIVE',
             NOW()
         );

-- 4
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
-- 5
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

-- 6
INSERT INTO member (
    mem_lgn_id, mem_lgn_pw, mem_aut, mem_nm, mem_biz_no, mem_biz_ttl, mem_biz_adr, mem_stt, mem_cre_dt
) VALUES (
             'user01',
             '$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2', -- 1234 (BCrypt)
             'USER',
             '정우석',
             '634-84-65672',
             '(주)유저컴퍼니',
             '서울시 강남구 테헤란로2',
             'ACTIVE',
             NOW()
         );

-- 7
INSERT INTO member (
    mem_lgn_id, mem_lgn_pw, mem_aut, mem_nm, mem_biz_no, mem_biz_ttl, mem_biz_adr, mem_stt, mem_cre_dt
) VALUES (
             'user02',
             '$2a$10$Tx1CyrrWN2qWI48xREs/a.H0N2WGc8jrLsdhBY/lWv53c0z1AFl/2', -- 1234 (BCrypt)
             'USER',
             '정아현',
             '723-03-32892',
             '(주)유정컴퍼니',
             '서울시 강남구 테헤란로3',
             'ACTIVE',
             NOW()
         );
-- =====================
-- 고희권 END
-- =====================

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
('KAP-002', 1, 'Kapital', '2', 'Jacket',      'Boro Padded Jacket',       2100.00, '¥', 'https://images.unsplash.com/photo-1548126032-079a0fb0099d?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 8,  5),
('KAP-003', 1, 'Kapital', '2', 'Jacket',      'Teru Teru Parka',          1980.00, '¥', 'https://images.unsplash.com/photo-1539109136-da888-59a3bc9f3a10?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6,  3),
('KAP-004', 1, 'Kapital', '4', 'Knitwear',     'Boro Mix Sweater',          890.00, '¥', 'https://images.unsplash.com/photo-1576566588028-4147f3842f27?auto=format&fit=crop&w=600&q=80', FALSE, FALSE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 14, 9),
('KAP-005', 1, 'Kapital', '4', 'Knitwear',     'Sashiko Knit Vest',         620.00, '¥', 'https://images.unsplash.com/photo-1583744946564-b52ac1c389c8?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 7,  4),
('KAP-006', 1, 'Kapital', '7', 'T-Shirt',         'Gauze Boro Shirt',          540.00, '¥', 'https://images.unsplash.com/photo-1596755389378-c31d21fd1273?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5,  2),
('KAP-007', 1, 'Kapital', '7', 'T-Shirt',         'Remake Flannel Shirt',      760.00, '¥', 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9,  6),
('KAP-008', 1, 'Kapital', '8', 'Bottoms',      'Kendo Pants',               980.00, '¥', 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11, 7),
('KAP-009', 1, 'Kapital', '8', 'Bottoms',      'Boro Remake Jeans',        1200.00, '¥', 'https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 13, 8),

-- Nanamica (¥)
('NAN-001', 2, 'Nanamica', '2', 'Jacket',     'GORE-TEX Cruiser Jacket',  3200.00, '¥', 'https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 20, 12),
('NAN-002', 2, 'Nanamica', '2', 'Jacket',     'Mountain Parka',           2800.00, '¥', 'https://images.unsplash.com/photo-1544923246-77307dd654cb?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 18, 10),
('NAN-003', 2, 'Nanamica', '2', 'Coat',     'Club Coat',                3600.00, '¥', 'https://images.unsplash.com/photo-1539533113208-f6df8cc8b543?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 15, 8),
('NAN-004', 2, 'Nanamica', '7', 'T-Shirt',        'COOLMAX Rugby Shirt',       640.00, '¥', 'https://images.unsplash.com/photo-1571945153237-4929e783af4a?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9,  5),
('NAN-005', 2, 'Nanamica', '7', 'T-Shirt',        'SUVIN Jersey Tee',          320.00, '¥', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 12, 7),
('NAN-006', 2, 'Nanamica', '8', 'Bottoms',     'COOLMAX Wide Chino',       1100.00, '¥', 'https://images.unsplash.com/photo-1506629082955-511b1aa562c8?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 6),
('NAN-007', 2, 'Nanamica', '8', 'Bottoms',     'Straight Tapered Chino',    980.00, '¥', 'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 8,  4),
('NAN-008', 2, 'Nanamica', '11', 'Bags / Accessories', 'Ruck Sack',                1400.00, '¥', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 16, 9),
('NAN-009', 2, 'Nanamica', '11', 'Bags / Accessories', 'Briefcase M',              1800.00, '¥', 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 11, 6),

-- Kenzo (€)
('KNZ-001', 3, 'Kenzo', '2', 'Jacket',        'Boke Flower Peacoat',       890.00, '€', 'https://images.unsplash.com/photo-1520975954732-35dd22299614?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 22, 14),
('KNZ-002', 3, 'Kenzo', '2', 'Jacket',        'Tiger Mountain Parka',      720.00, '€', 'https://images.unsplash.com/photo-1467043237213-65f2da53396f?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 17, 11),
('KNZ-003', 3, 'Kenzo', '7', 'T-Shirt',           'Tiger Print Tee',           180.00, '€', 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 30, 20),
('KNZ-004', 3, 'Kenzo', '7', 'T-Shirt',           'Boke Flower Shirt',         260.00, '€', 'https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 19, 13),
('KNZ-005', 3, 'Kenzo', '4', 'Knitwear',       'Tiger Embroidery Sweater',  420.00, '€', 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 14, 9),
('KNZ-006', 3, 'Kenzo', '11', 'Bags / Accessories',    'Tiger Cap',                 120.00, '€', 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 25, 18),
('KNZ-007', 3, 'Kenzo', '11', 'Bags / Accessories',    'Boke Flower Scarf',         150.00, '€', 'https://images.unsplash.com/photo-1601924994987-69e26d50dc26?auto=format&fit=crop&w=600&q=80', FALSE, TRUE, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10, 6);

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
-- REQUIREMENT
-- =====================
INSERT INTO requirement (req_ttl, req_con, req_ref, req_pr, req_stt, req_rep_yn, req_per_yn, req_cre_dt, req_upd_dt, req_del_yn, mem_id) VALUES
('Kapital Boro Jacket 입고 요청', '카피탈 보로 자켓 시즌 신상 입고 요청합니다. 사이즈 M, L 둘 다 가능합니다.', 'https://kapital.jp/boro-jacket', 2100, 'SUBMITTED', FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 3),
('Nanamica GORE-TEX 코트 문의', '나나미카 고어텍스 코트 재입고 가능한지 확인 부탁드립니다.', 'https://nanamica.com/goretex-coat', 3600, 'SUBMITTED', FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 3),
('Kenzo 타이거 스웨터 요청', '겐조 타이거 자수 스웨터 XL 사이즈 입고 요청합니다.', 'https://kenzo.com/tiger-sweater', 1500, 'SUBMITTED', TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 3),
('Kapital 인디고 데님 팬츠', '카피탈 인디고 데님 팬츠 32인치 입고 가능한지 문의합니다.', NULL, 980, 'SUBMITTED', TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 3),
('Nanamica 러그비 셔츠 입고 요청', '나나미카 쿨맥스 러그비 셔츠 L 사이즈 입고 부탁드립니다.', 'https://nanamica.com/rugby-shirt', 640, 'SUBMITTED', FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 3),
('Kapital 사시코 니트 조끼', '사시코 니트 조끼 프리사이즈 입고 요청합니다.', NULL, 620, 'DRAFT', FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 3),
('Kenzo 플라워 프린트 셔츠', '겐조 플라워 프린트 셔츠 M 사이즈 요청합니다. 참고 링크 첨부합니다.', 'https://kenzo.com/flower-shirt', 890, 'DRAFT', FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 3),
('Nanamica 와이드 치노 팬츠', '쿨맥스 와이드 치노 30인치 입고 가능할까요?', 'https://nanamica.com/wide-chino', 1100, 'SUBMITTED', TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 3);

-- =====================
-- 최준희 END
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
    (6, '입고/출고 일정', 2, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (7, '견적 요청 방법', 2, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (8, '최소 발주 수량', 2, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (9, '비밀번호 변경', 2, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4),
    (10, '사업자 정보 변경', 2, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4);

INSERT INTO chatbot_response
(cb_res_id, cb_res_ttl, cb_res_con, cb_res_lnk_btn_nm, cb_res_lnk_url, cb_res_use_yn, cb_res_cre_dt, cb_res_upd_dt, cb_tp_id)
VALUES
    (1, '배송 조회 안내', '마이페이지의 주문/배송 내역에서 진행 상태를 확인하실 수 있습니다. 국내 배송 시작 이후에는 송장 정보가 순차적으로 반영되며, 현지 출고 직후에는 상태 반영까지 다소 시간이 걸릴 수 있습니다.', '주문 내역 보기', '/order/list', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5),
    (2, '통관 및 입고 일정 안내', '통관과 입고 일정은 상품 종류, 현지 출고 시점, 세관 상황에 따라 달라질 수 있습니다. 급한 일정이 있는 경우 상담사 연결을 통해 주문번호와 희망 납기일을 함께 남겨주시면 확인 후 안내드립니다.', NULL, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6),
    (3, '견적 요청 방법 안내', '원하시는 상품명, 브랜드, 수량, 옵션 정보를 준비하신 뒤 견적 요청 페이지에서 접수해 주세요. 요청 내용이 구체적일수록 상담과 산출이 더 빠르게 진행됩니다.', '견적 요청하기', '/quote/write', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 7),
    (4, '최소 발주 수량 안내', '최소 발주 수량은 브랜드와 상품마다 다를 수 있습니다. 대량 구매나 정기 발주를 검토 중이신 경우 상담사 연결을 통해 예상 수량을 알려주시면 확인 후 안내드립니다.', NULL, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 8),
    (5, '결제 안내', '결제 가능 수단과 결제 진행 상태는 결제 안내 화면에서 확인하실 수 있습니다. 카드 결제 오류나 입금 확인 지연이 있는 경우 상담사 연결을 통해 주문 정보와 함께 문의해 주세요.', '결제 내역 보기', '/payment/check', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    (6, '비밀번호 변경 안내', '비밀번호를 변경하려면 마이페이지의 비밀번호 변경 메뉴를 이용해 주세요. 현재 비밀번호 확인 후 새 비밀번호를 등록할 수 있으며, 계정 접근에 문제가 있는 경우에는 상담사 연결을 통해 추가 안내를 받을 수 있습니다.', '비밀번호 변경으로 이동', '/mypage/changepw', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9),
    (7, '사업자 정보 변경 안내', '상호, 대표자명, 사업장 주소 등 사업자 정보 변경이 필요한 경우 마이페이지에서 수정 가능한 항목을 먼저 확인해 주세요. 증빙서류 확인이 필요한 변경 건은 상담사 연결 후 처리됩니다.', '사업자 정보 변경으로 이동', '/mypage/changebiz', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10);

ALTER TABLE chatbot_topic ALTER COLUMN cb_tp_id RESTART WITH 100;
ALTER TABLE chatbot_response ALTER COLUMN cb_res_id RESTART WITH 100;

-- user(3) 사용자 시연용: 활성방 1개(ONGOING) + 종료방 3개
INSERT INTO chat_room (
    ch_ro_ttl, ch_ro_stt, ch_ro_cre_dt, ch_ro_asg_dt, ch_ro_last_ms_dt, mem_id, mem_ad_id
) VALUES (
             '배송 문의',
             'ONGOING',
             DATEADD('MINUTE', -50, CURRENT_TIMESTAMP),
             DATEADD('MINUTE', -48, CURRENT_TIMESTAMP),
             DATEADD('MINUTE', -10, CURRENT_TIMESTAMP),
             3,
             2
         );

INSERT INTO chat_message (
    ch_ms_sen_ty, ch_ms_con, ch_ms_cre_dt, ch_ro_id, mem_id
) VALUES
      (
          'USER',
          '배송 일정 확인 부탁드립니다.',
          DATEADD('MINUTE', -30, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '배송 문의' AND mem_id = 3 AND ch_ro_stt = 'ONGOING'),
          3
      ),
      (
          'ADMIN',
          '현재 출고 일정 확인 중이며 곧 다시 안내드리겠습니다.',
          DATEADD('MINUTE', -10, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '배송 문의' AND mem_id = 3 AND ch_ro_stt = 'ONGOING'),
          2
      );

INSERT INTO chat_room_read_status (
    ch_ro_re_st_last_dt, ch_ro_re_st_unr_yn, ch_ro_id, mem_id, ch_ms_last_id
) VALUES (
             NULL,
             TRUE,
             (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '배송 문의' AND mem_id = 3 AND ch_ro_stt = 'ONGOING'),
             3,
             NULL
         );

INSERT INTO chat_room (
    ch_ro_ttl, ch_ro_stt, ch_ro_cre_dt, ch_ro_asg_dt, ch_ro_cls_dt, ch_ro_cls_rsn, ch_ro_last_ms_dt, mem_id,
    mem_ad_id
) VALUES
      (
          '결제 문의',
          'CLOSED',
          DATEADD('DAY', -1, CURRENT_TIMESTAMP),
          DATEADD('DAY', -1, CURRENT_TIMESTAMP),
          DATEADD('DAY', -1, CURRENT_TIMESTAMP),
          'ADMIN',
          DATEADD('DAY', -1, CURRENT_TIMESTAMP),
          3,
          2
      ),
      (
          '견적 문의',
          'CLOSED',
          DATEADD('DAY', -2, CURRENT_TIMESTAMP),
          DATEADD('DAY', -2, CURRENT_TIMESTAMP),
          DATEADD('DAY', -2, CURRENT_TIMESTAMP),
          'USER',
          DATEADD('DAY', -2, CURRENT_TIMESTAMP),
          3,
          2
      ),
      (
          '회원정보 문의',
          'CLOSED',
          DATEADD('DAY', -3, CURRENT_TIMESTAMP),
          DATEADD('DAY', -3, CURRENT_TIMESTAMP),
          DATEADD('DAY', -3, CURRENT_TIMESTAMP),
          'ADMIN',
          DATEADD('DAY', -3, CURRENT_TIMESTAMP),
          3,
          2
      );

INSERT INTO chat_message (
    ch_ms_sen_ty, ch_ms_con, ch_ms_cre_dt, ch_ro_id, mem_id
) VALUES
      (
          'USER',
          '결제 승인 오류가 발생했습니다.',
          DATEADD('DAY', -1, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '결제 문의' AND mem_id = 3 AND ch_ro_stt = 'CLOSED'),
          3
      ),
      (
          'ADMIN',
          '결제 시스템 확인 후 상담을 종료했습니다.',
          DATEADD('DAY', -1, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '결제 문의' AND mem_id = 3 AND ch_ro_stt = 'CLOSED'),
          2
      ),
      (
          'USER',
          '견적서를 다시 받아볼 수 있을까요?',
          DATEADD('DAY', -2, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '견적 문의' AND mem_id = 3 AND ch_ro_stt = 'CLOSED'),
          3
      ),
      (
          'USER',
          '사업자 정보 변경 관련 서류 제출이 완료되었습니다.',
          DATEADD('DAY', -3, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '회원정보 문의' AND mem_id = 3 AND ch_ro_stt = 'CLOSED'),
          3
      ),
      (
          'ADMIN',
          '서류 확인 후 상담을 종료했습니다.',
          DATEADD('DAY', -3, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '회원정보 문의' AND mem_id = 3 AND ch_ro_stt = 'CLOSED'),
          2
      );

-- user(6) 사용자 : 활성방 1개(OPEN) + 종료방 2개
INSERT INTO chat_room (
    ch_ro_ttl, ch_ro_stt, ch_ro_cre_dt, ch_ro_last_ms_dt, mem_id, mem_ad_id
) VALUES (
             '기타 문의',
             'OPEN',
             DATEADD('MINUTE', -40, CURRENT_TIMESTAMP),
             DATEADD('MINUTE', -40, CURRENT_TIMESTAMP),
             6,
             NULL
         );

INSERT INTO chat_message (
    ch_ms_sen_ty, ch_ms_con, ch_ms_cre_dt, ch_ro_id, mem_id
) VALUES (
             'USER',
             '새 문의하기로 생성된 채팅방 테스트 메시지입니다.',
             DATEADD('MINUTE', -40, CURRENT_TIMESTAMP),
             (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '기타 문의' AND mem_id = 6 AND ch_ro_stt = 'OPEN'),
             6
         );

INSERT INTO chat_room (
    ch_ro_ttl, ch_ro_stt, ch_ro_cre_dt, ch_ro_asg_dt, ch_ro_cls_dt, ch_ro_cls_rsn, ch_ro_last_ms_dt, mem_id,
    mem_ad_id
) VALUES
      (
          '배송 문의',
          'CLOSED',
          DATEADD('DAY', -4, CURRENT_TIMESTAMP),
          DATEADD('DAY', -4, CURRENT_TIMESTAMP),
          DATEADD('DAY', -4, CURRENT_TIMESTAMP),
          'ADMIN',
          DATEADD('DAY', -4, CURRENT_TIMESTAMP),
          6,
          2
      ),
      (
          '견적 문의',
          'CLOSED',
          DATEADD('DAY', -5, CURRENT_TIMESTAMP),
          DATEADD('DAY', -5, CURRENT_TIMESTAMP),
          DATEADD('DAY', -5, CURRENT_TIMESTAMP),
          'USER',
          DATEADD('DAY', -5, CURRENT_TIMESTAMP),
          6,
          2
      );

INSERT INTO chat_message (
    ch_ms_sen_ty, ch_ms_con, ch_ms_cre_dt, ch_ro_id, mem_id
) VALUES
      (
          'USER',
          '배송 일정 재안내 부탁드립니다.',
          DATEADD('DAY', -4, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '배송 문의' AND mem_id = 6 AND ch_ro_stt = 'CLOSED'),
          6
      ),
      (
          'ADMIN',
          '배송 관련 안내 후 상담을 종료했습니다.',
          DATEADD('DAY', -4, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '배송 문의' AND mem_id = 6 AND ch_ro_stt = 'CLOSED'),
          2
      ),
      (
          'USER',
          '견적 관련 다시 문의드립니다.',
          DATEADD('DAY', -5, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '견적 문의' AND mem_id = 6 AND ch_ro_stt = 'CLOSED'),
          6
      );

-- user(7) 사용자 : 활성방 1개(ONGOING) + 종료방 2개
INSERT INTO chat_room (
    ch_ro_ttl, ch_ro_stt, ch_ro_cre_dt, ch_ro_asg_dt, ch_ro_last_ms_dt, mem_id, mem_ad_id
) VALUES (
             '결제 문의',
             'ONGOING',
             DATEADD('MINUTE', -35, CURRENT_TIMESTAMP),
             DATEADD('MINUTE', -34, CURRENT_TIMESTAMP),
             DATEADD('MINUTE', -12, CURRENT_TIMESTAMP),
             7,
             2
         );

INSERT INTO chat_message (
    ch_ms_sen_ty, ch_ms_con, ch_ms_cre_dt, ch_ro_id, mem_id
) VALUES
      (
          'USER',
          '결제 진행 상태 확인 부탁드립니다.',
          DATEADD('MINUTE', -25, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '결제 문의' AND mem_id = 7 AND ch_ro_stt = 'ONGOING'),
          7
      ),
      (
          'ADMIN',
          '현재 결제 상태 확인 중입니다.',
          DATEADD('MINUTE', -12, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '결제 문의' AND mem_id = 7 AND ch_ro_stt = 'ONGOING'),
          2
      );

INSERT INTO chat_room (
    ch_ro_ttl, ch_ro_stt, ch_ro_cre_dt, ch_ro_asg_dt, ch_ro_cls_dt, ch_ro_cls_rsn, ch_ro_last_ms_dt, mem_id,
    mem_ad_id
) VALUES
      (
          '회원정보 문의',
          'CLOSED',
          DATEADD('DAY', -6, CURRENT_TIMESTAMP),
          DATEADD('DAY', -6, CURRENT_TIMESTAMP),
          DATEADD('DAY', -6, CURRENT_TIMESTAMP),
          'ADMIN',
          DATEADD('DAY', -6, CURRENT_TIMESTAMP),
          7,
          2
      ),
      (
          '기타 문의',
          'CLOSED',
          DATEADD('DAY', -7, CURRENT_TIMESTAMP),
          DATEADD('DAY', -7, CURRENT_TIMESTAMP),
          DATEADD('DAY', -7, CURRENT_TIMESTAMP),
          'USER',
          DATEADD('DAY', -7, CURRENT_TIMESTAMP),
          7,
          2
      );

INSERT INTO chat_message (
    ch_ms_sen_ty, ch_ms_con, ch_ms_cre_dt, ch_ro_id, mem_id
) VALUES
      (
          'USER',
          '비밀번호 변경 관련 문의드립니다.',
          DATEADD('DAY', -6, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '회원정보 문의' AND mem_id = 7 AND ch_ro_stt = 'CLOSED'),
          7
      ),
      (
          'ADMIN',
          '회원정보 관련 안내 후 상담을 종료했습니다.',
          DATEADD('DAY', -6, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '회원정보 문의' AND mem_id = 7 AND ch_ro_stt = 'CLOSED'),
          2
      ),
      (
          'USER',
          '기타 문의 내용입니다.',
          DATEADD('DAY', -7, CURRENT_TIMESTAMP),
          (SELECT ch_ro_id FROM chat_room WHERE ch_ro_ttl = '기타 문의' AND mem_id = 7 AND ch_ro_stt = 'CLOSED'),
          7
      );
-- =====================
-- 장준 END
-- =====================




-- =====================
-- 임 욱 START
-- =====================
INSERT INTO ORDER_BASE (ord_base_rcv_nm, ord_base_adr, ord_base_adr_dt, mem_id, ord_base_no, ord_base_stt, ord_base_cre_dt)
VALUES
('홍길동', '서울특별시 금천구 가산디지털2로 95', '3층 301호 구디아카데미', 3, '1234', 'PREPARING', CURRENT_TIMESTAMP),
('갑을병', '서울특별시 금천구 가산디지털2로 95', '3층 302호 구디아카데미', 3, '1234', 'DELIVERING', CURRENT_TIMESTAMP),
('김구디', '서울특별시 금천구 가산디지털2로 95', '3층 303호 구디아카데미', 3, '1234', 'DELIVERING', CURRENT_TIMESTAMP),
('병정무', '서울특별시 금천구 가산디지털2로 95', '3층 304호 구디아카데미', 3, '1234', 'DELIVERED', CURRENT_TIMESTAMP),
('임꺽정', '서울특별시 금천구 가산디지털2로 95', '3층 305호 구디아카데미', 3, '1234', 'CANCELLED', CURRENT_TIMESTAMP);

INSERT INTO NOTIFICATION (noti_ttl, noti_con, noti_rea_yn, noti_del_yn, noti_upd_mem_id, mem_id)
VALUES
    ('주문이 승인 되었습니다.', '요청하신 주문이 승인 처리 되었습니다.', FALSE, FALSE,  NULL, 3),
    ('상품 배송이 완료되었습니다.', '요청하신 상품 배송이 완료 되었습니다.', FALSE, FALSE, NULL, 3),
    ('견적 요청이 반려되었습니다.', '견적 상세를 통해 견적을 수정해주세요.', FALSE, FALSE,  NULL, 3),
    ('상품 배송이 완료되었습니다.', '요청하신 상품 배송이 완료 되었습니다.', TRUE, FALSE, NULL, 3),
    ('상품 배송이 완료되었습니다.', '요청하신 상품 배송이 완료 되었습니다.', FALSE, TRUE, NULL, 3);

INSERT INTO RECEIVER (rc_nm, rc_phn, rc_msg, rc_pos_cd, rc_adr, rc_adr_dt, rc_rgn, rc_cre_dt, rc_upd_dt, rc_adr_df_yn, rc_del_yn, mem_id)
VALUES
    ('홍길동', '010-1234-5678', '배송 전에 연락바랍니다.', '08505','서울특별시 금천구 가산디지털2로 95', '3층 301호 구디아카데미', 'SEOUL', CURRENT_TIMESTAMP, null, TRUE, FALSE, 3),
    ('김구디', '010-2345-6789', '안전 배송 부탁합니다.', '08505', '서울특별시 금천구 가산디지털2로 95', '3층 303호 구디아카데미', 'SEOUL', CURRENT_TIMESTAMP, null, FALSE, FALSE, 3),
    ('임꺽정', '010-3456-7890', '13시 ~ 15시까지 부재중입니다. 부재 중 방문 시 연락바랍니다.', '08505', '서울특별시 금천구 가산디지털2로 95', '3층 305호 구디아카데미', 'SEOUL', CURRENT_TIMESTAMP, null, FALSE, FALSE, 3);
-- =====================
-- 임 욱 END
-- =====================

