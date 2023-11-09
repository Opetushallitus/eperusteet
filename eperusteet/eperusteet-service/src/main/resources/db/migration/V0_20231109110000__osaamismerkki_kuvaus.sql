ALTER TABLE osaamismerkki ADD COLUMN kuvaus_id int8;
ALTER TABLE osaamismerkki_aud ADD COLUMN kuvaus_id int8;
ALTER TABLE osaamismerkki_kategoria ADD COLUMN kuvaus_id int8;
ALTER TABLE osaamismerkki_kategoria_aud ADD COLUMN kuvaus_id int8;

alter table osaamismerkki_kategoria
    add constraint FK_sbsl97w6o02yu6v5awwgvoajh
        foreign key (kuvaus_id)
            references tekstipalanen;

alter table osaamismerkki
    add constraint FK_15khqi5hupoi5tt2ets609shs
        foreign key (kuvaus_id)
            references tekstipalanen;
