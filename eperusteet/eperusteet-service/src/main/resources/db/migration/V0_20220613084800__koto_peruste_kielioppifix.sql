alter table koto_taitotaso add column vuorovaikutusJaMediaatio_id bigint;
alter table koto_taitotaso_aud add column vuorovaikutusJaMediaatio_id bigint;
alter table koto_taitotaso
    add constraint FK_pkd02mdqfy4k9tjxsch2cusd6
        foreign key (vuorovaikutusJaMediaatio_id)
            references tekstipalanen;

update koto_taitotaso set vuorovaikutusJaMediaatio_id = vuorovaikutusJaMeditaatio_id;

alter table koto_taitotaso drop column vuorovaikutusJaMeditaatio_id;
alter table koto_taitotaso_aud drop column vuorovaikutusJaMeditaatio_id;
