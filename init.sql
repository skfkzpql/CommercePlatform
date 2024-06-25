CREATE DATABASE commerceplatform;
CREATE USER 'commerceuser'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON commerceplatform.* TO 'commerceuser'@'localhost';
FLUSH PRIVILEGES;