drop table users if exists;
CREATE TABLE users(id INTEGER NOT NULL AUTO_INCREMENT,
                   surname TEXT NOT NULL,
                   name TEXT NOT NULL,
                   patronymic TEXT,
                   login TEXT NULL,
                   password TEXT NULL);