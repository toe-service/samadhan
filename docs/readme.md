
INSERT INTO samadhan.rides (
ride_id,
ride_status,
ride_otp,
driver_response,
driver_declination_reason,
driver_id,
user_id,
ride_response_time,
ride_start_time,
ride_end_time
) VALUES (
'RIDE-12345',   -- ride_id (unique)
TRUE,           -- ride_status
4567,           -- ride_otp
FALSE,          -- driver_response
NULL,           -- driver_declination_reason
2,              -- driver_id (must exist in drivers table)
1,              -- user_id (must exist in users table)
'2025-02-10 12:30:00', -- ride_response_time
'2025-02-10 12:40:00', -- ride_start_time
'2025-02-10 13:10:00'  -- ride_end_time
);
