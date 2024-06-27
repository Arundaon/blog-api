CREATE DATABASE mydatabase;

USE mydatabase;

CREATE TABLE users(
                      id INT NOT NULL AUTO_INCREMENT,
                      name    VARCHAR(16) NOT NULL,
                      password VARCHAR(100) NOT NULL,
                      email VARCHAR(100) NOT NULL,
                      PRIMARY KEY (id),
                      UNIQUE(email)
);

CREATE TABLE posts(
                      id INT NOT NULL AUTO_INCREMENT,
                       content VARCHAR(2048) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       author_id INT NOT NULL,
                       PRIMARY KEY (id),
                       FOREIGN KEY fk_author (author_id) REFERENCES users (id)
);
