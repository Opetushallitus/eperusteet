DROP TABLE IF EXISTS kayttajaprofiili_peruste;
DROP INDEX IF EXISTS ix_suosikki;

create table suosikki (
        id bigint not null primary key,
        kayttajaprofiili_id bigint,
        peruste_id bigint,
        suoritustapakoodi varchar(255),
        suosikki_order integer
    );

CREATE UNIQUE INDEX ix_suosikki 
  ON suosikki(kayttajaprofiili_id, peruste_id, suoritustapakoodi);

alter table suosikki 
        add constraint FK_suosikki_peruste
        foreign key (peruste_id) 
        references peruste;

alter table suosikki 
        add constraint FK_suosikki_kayttajaprofiili
        foreign key (Kayttajaprofiili_id) 
        references kayttajaprofiili;