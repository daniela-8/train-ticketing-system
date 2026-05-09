CREATE USER IF NOT EXISTS 'tickets_user'@'localhost' IDENTIFIED BY 'tickets_pass';

GRANT ALL PRIVILEGES ON tickets_db.* TO 'tickets_user'@'localhost';
FLUSH PRIVILEGES;