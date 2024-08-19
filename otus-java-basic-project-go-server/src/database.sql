CREATE DATABASE otus_java_basic_project
    WITH
    OWNER = otus
    ENCODING = 'UTF8'
    LOCALE_PROVIDER = 'libc'
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

CREATE SCHEMA authentication
    AUTHORIZATION pg_database_owner;

CREATE TABLE authentication."user"
(
    user_id serial NOT NULL,
    login text NOT NULL,
    password_salted_hash text NOT NULL,
    password_salt text NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE (login)
);

ALTER TABLE IF EXISTS authentication."user"
    OWNER to pg_database_owner;

INSERT INTO authentication."user" VALUES (1, 'root', 'dOEWvx9pNtXQuwKbq9NH2tmvU6Y5q+pH4THym026q6U=', 'n\poeDfqvujZkBfT');
INSERT INTO authentication."user" VALUES (2, 'hikaru', 'FsdXFovasGCPlLJeynBXzJZ6vUF8OaAIn0Kjgwj6mPg=', 'FELvSxAippT]Ejkx');
INSERT INTO authentication."user" VALUES (3, 'lee', 'Sy/0z2ReF7S4tWZ9QquRO55/Ttyf/ZG202IYUr5Vd/A=', 'Z\asIdgkbANLQKVs');

SELECT pg_catalog.setval('authentication.user_user_id_seq', 3, true);