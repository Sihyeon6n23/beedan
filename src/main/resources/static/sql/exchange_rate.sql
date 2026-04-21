-- 환율 이력 테이블
CREATE TABLE exchange_rate (
                               id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                               currency    VARCHAR(10)    NOT NULL,        -- 'JPY', 'USD', 'EUR', 'CNY'
                               rate        DECIMAL(18, 6) NOT NULL,        -- KRW 기준 환율 (예: 1 JPY = 9.52 KRW)
                               base        VARCHAR(10)    NOT NULL DEFAULT 'KRW',
                               fetched_at  DATETIME       NOT NULL,        -- API에서 받아온 시각
                               created_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               INDEX idx_currency_fetched (currency, fetched_at DESC)
);