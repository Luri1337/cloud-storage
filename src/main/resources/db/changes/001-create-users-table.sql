create table users(
    id BIGSERIAL PRIMARY KEY ,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(250) NOT NULL
);
