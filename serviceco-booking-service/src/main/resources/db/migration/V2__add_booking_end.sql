ALTER TABLE bookings
ADD COLUMN booking_end DATETIME NULL;

UPDATE bookings
SET booking_end = DATE_ADD(
    booking_date,
    INTERVAL duration_hours HOUR
);

ALTER TABLE bookings
MODIFY COLUMN booking_end DATETIME NOT NULL;