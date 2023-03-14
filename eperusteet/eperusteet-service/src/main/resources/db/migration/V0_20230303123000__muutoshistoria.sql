ALTER TABLE julkaistu_peruste ADD COLUMN julkinen boolean NOT NULL DEFAULT FALSE;
ALTER TABLE julkaistu_peruste ADD COLUMN muutosmaarays_voimaan TIMESTAMP;
ALTER TABLE julkaistu_peruste ADD COLUMN julkinen_tiedote_id bigint;
ALTER TABLE julkaistu_peruste
    ADD CONSTRAINT FK_julkinen_tiedote
        FOREIGN KEY (julkinen_tiedote_id)
            REFERENCES tekstipalanen;

create table julkaisu_liite (
                                id int8 not null,
                                kieli varchar(255) not null,
                                julkaistu_peruste_id int8 not null,
                                liite_id uuid not null,
                                primary key (id)
);

alter table julkaisu_liite
    add constraint FK_lxmxvvawjv9wwx6swbhxi26us
        foreign key (julkaistu_peruste_id)
            references julkaistu_peruste;

alter table julkaisu_liite
    add constraint FK_m2l0286yybgxq924oyxoiwfyh
        foreign key (liite_id)
            references liite;
