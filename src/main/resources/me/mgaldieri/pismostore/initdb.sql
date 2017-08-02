SET FOREIGN_KEY_CHECKS=0;
SET IGNORECASE TRUE;

-- Drop tables at initialization (for test-safe db state)

DROP TABLE IF EXISTS User;
DROP TABLE IF EXISTS Session;
DROP TABLE IF EXISTS ApiKey;
DROP TABLE IF EXISTS Cart;
DROP TABLE IF EXISTS Payment;

-- Create tables

CREATE TABLE User (
id bigint PRIMARY KEY AUTO_INCREMENT,
name varchar(256),
email varchar(256) UNIQUE,
password varchar(256),
date_created timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Session (
session_id varchar(256) PRIMARY KEY,
user_id bigint,
FOREIGN KEY (user_id) REFERENCES User (id)
);

CREATE TABLE ApiKey (
id bigint PRIMARY KEY AUTO_INCREMENT,
token varchar(256),
date_created timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Cart (
id bigint PRIMARY KEY AUTO_INCREMENT,
user_id bigint,
product_id bigint,
qty int,
date_created timestamp DEFAULT CURRENT_TIMESTAMP,
date_updated timestamp AS CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES User (id)
);

CREATE TABLE Payment (
id bigint PRIMARY KEY AUTO_INCREMENT,
user_id bigint,
token varchar(256),
price_cents int,
date_created timestamp DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES User (id)
);

-- Insert seed data

MERGE INTO User (id, name, email, password) VALUES (1, 'User', 'user@email.com', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446'); -- password: 'user123'
MERGE INTO ApiKey (id, token) VALUES (1, '2289edc82ff62d5d8a82ad2ef7079871aef71713');

SET FOREIGN_KEY_CHECKS=1;