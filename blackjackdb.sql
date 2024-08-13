
CREATE DATABASE IF NOT EXISTS blackjackdb;

USE blackjackdb;

CREATE TABLE IF NOT EXISTS players (
	id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    score INT DEFAULT 0,
    total_wins INT DEFAULT 0,
    total_losses INT DEFAULT 0
);
