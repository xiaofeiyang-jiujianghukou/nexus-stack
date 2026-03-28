CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY,
    level VARCHAR(50) DEFAULT 'NORMAL',
    register_time BIGINT
);

CREATE TABLE IF NOT EXISTS members (
    user_id BIGINT PRIMARY KEY,
    is_member TINYINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS orders (
    order_id BIGINT PRIMARY KEY,
    user_id BIGINT,
    amount DECIMAL(10,2),
    ts BIGINT,
    INDEX idx_user_id (user_id)
);
