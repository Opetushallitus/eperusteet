CREATE TABLE perusteen_tutkintonimikkeet (
    id int8 NOT NULL PRIMARY KEY,
    peruste_id int8 NOT NULL REFERENCES peruste(id),
    tutkinnon_osa_koodi_uri VARCHAR(255),
    tutkinnon_osa_koodi_arvo VARCHAR(255),
    osaamisala_koodi_uri VARCHAR(255) NOT NULL,
    osaamisala_koodi_arvo VARCHAR(255) NOT NULL,
    tutkintonimike_koodi_uri VARCHAR(255) NOT NULL,
    tutkintonimike_koodi_arvo VARCHAR(255) NOT NULL,
    CONSTRAINT u_perusteen_tutkintonimikkeet UNIQUE(tutkinnon_osa_koodi_uri, osaamisala_koodi_uri, tutkintonimike_koodi_uri)
);