DROP TABLE IF EXISTS hits;
DROP SEQUENCE IF EXISTS hits_id_seq;

CREATE SEQUENCE hits_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE hits (
    id BIGINT DEFAULT nextval('hits_id_seq') PRIMARY KEY,
    app VARCHAR(255) NOT NULL,
    uri VARCHAR(512) NOT NULL,
    ip VARCHAR(45) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_hits_uri_timestamp ON hits(uri, timestamp);
CREATE INDEX idx_hits_timestamp ON hits(timestamp);
CREATE INDEX idx_hits_app ON hits(app);