CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    price INT NOT NULL CHECK (price > 0),
    stock INT NOT NULL CHECK (stock >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_products_name UNIQUE (name)
);