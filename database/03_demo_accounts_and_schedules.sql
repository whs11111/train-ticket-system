-- Import into the new train_ticket_demo database after 01_schema.sql and 02_routes.sql.
-- All accounts below are fictional classroom demo accounts, not original user data.
SET NAMES utf8mb4;
START TRANSACTION;

INSERT INTO sys_user (username, password, name, phone, idnum, role)
SELECT 'demo_admin', 'Demo123456', '演示管理员', '00000000001', '000000000000000001', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'demo_admin');
INSERT INTO sys_user (username, password, name, phone, idnum, role)
SELECT 'demo_user', 'Demo123456', '演示用户', '00000000002', '000000000000000002', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'demo_user');
INSERT INTO sys_user (username, password, name, phone, idnum, role)
SELECT 'demo_staff', 'Demo123456', '演示工作人员', '00000000003', '000000000000000003', 'STAFF'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'demo_staff');

-- Generate the import day and the following two days rather than expired fixed dates.
INSERT INTO train_schedule (train_id, run_date)
SELECT t.train_id, DATE_ADD(CURDATE(), INTERVAL d.offset_days DAY)
FROM train t
CROSS JOIN (SELECT 0 AS offset_days UNION ALL SELECT 1 UNION ALL SELECT 2) d
WHERE NOT EXISTS (
    SELECT 1 FROM train_schedule existing
    WHERE existing.train_id = t.train_id
      AND existing.run_date = DATE_ADD(CURDATE(), INTERVAL d.offset_days DAY)
);

COMMIT;
