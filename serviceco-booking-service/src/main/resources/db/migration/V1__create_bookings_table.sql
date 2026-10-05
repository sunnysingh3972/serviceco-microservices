CREATE TABLE bookings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    service_skill VARCHAR(100) NOT NULL,
    booking_date DATETIME NOT NULL,
    duration_hours INT NOT NULL,
    hourly_rate DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,

    PRIMARY KEY (id),

    INDEX idx_booking_provider (provider_id),
    INDEX idx_booking_customer (customer_id),
    INDEX idx_booking_status (status),
    INDEX idx_booking_date (booking_date)
);