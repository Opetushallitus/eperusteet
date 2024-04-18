CREATE TABLE if not exists cas_client_session
(
    MAPPING_ID TEXT PRIMARY KEY ,
    SESSION_ID TEXT NOT NULL UNIQUE
);