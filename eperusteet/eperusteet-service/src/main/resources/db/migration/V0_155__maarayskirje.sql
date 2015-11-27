CREATE TABLE maarayskirje (
    id INT8 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE maarayskirje_aud (
    id INT8 NOT NULL,
    REV INT4 NOT NULL,
    REVTYPE INT2,
    REVEND INT4,
    PRIMARY KEY (id, REV)
);

ALTER TABLE maarayskirje_aud
    ADD CONSTRAINT FK_maarayskirje_aud_revinfo_rev
    FOREIGN KEY (REV)
    REFERENCES revinfo;

ALTER TABLE maarayskirje_aud
    ADD CONSTRAINT FK_maarayskirje_aud_revinfo_revend
    FOREIGN KEY (REVEND)
    REFERENCES revinfo;

CREATE TABLE maarayskirje_url (
    maarayskirje_id INT8 NOT NULL,
    url TEXT,
    url_KEY INT4,
    PRIMARY KEY (maarayskirje_id, url_KEY)
);

ALTER TABLE maarayskirje_url
    ADD CONSTRAINT FK_maarayskirje_url_maarayskirje
    FOREIGN KEY (maarayskirje_id)
    REFERENCES maarayskirje;

CREATE TABLE maarayskirje_url_aud (
    REV INT4 NOT NULL,
    maarayskirje_id INT8 NOT NULL,
    url TEXT NOT NULL,
    url_KEY INT4 NOT NULL,
    REVTYPE INT2,
    REVEND INT4,
    PRIMARY KEY (REV, maarayskirje_id, url, url_KEY)
);

ALTER TABLE maarayskirje_url_aud
    ADD CONSTRAINT FK_maarayskirje_url_aud_revinfo_rev
    FOREIGN KEY (REV) 
    REFERENCES revinfo;

ALTER TABLE maarayskirje_url_aud
    ADD CONSTRAINT FK_maarayskirje_url_aud_revinfo_revend
    FOREIGN KEY (REVEND) 
    REFERENCES revinfo;

ALTER TABLE peruste ADD COLUMN maarayskirje_id BIGINT;

ALTER TABLE peruste_aud ADD COLUMN maarayskirje_id BIGINT;

ALTER TABLE peruste
    ADD CONSTRAINT FK_peruste_maarayskirje
    FOREIGN KEY (maarayskirje_id)
    REFERENCES maarayskirje;

ALTER TABLE peruste_aud
    ADD CONSTRAINT FK_peruste_aud_maarayskirje
    FOREIGN KEY (maarayskirje_id)
    REFERENCES maarayskirje;
