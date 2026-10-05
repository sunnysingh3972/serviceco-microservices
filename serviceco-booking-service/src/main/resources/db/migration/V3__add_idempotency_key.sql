ALTER TABLE bookings
ADD COLUMN idempotency_key VARCHAR(100) NULL;

CREATE UNIQUE INDEX uk_booking_customer_idempotency
ON bookings (customer_id, idempotency_key);