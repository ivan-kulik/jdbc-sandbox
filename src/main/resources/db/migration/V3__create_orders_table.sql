CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(60) NOT NULL,
    total_price DECIMAL NOT NULL CHECK (total_price > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE
);