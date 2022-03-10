alter table koto_taitotaso add column suullinenVastaanottaminen_id bigint;
alter table koto_taitotaso add column suullinenTuottaminen_id bigint;
alter table koto_taitotaso add column vuorovaikutusJaMeditaatio_id bigint;

alter table koto_taitotaso_aud add column suullinenVastaanottaminen_id bigint;
alter table koto_taitotaso_aud add column suullinenTuottaminen_id bigint;
alter table koto_taitotaso_aud add column vuorovaikutusJaMeditaatio_id bigint;

alter table koto_taitotaso
    add constraint FK_flbke9y4yqyco6lccnlunqwlu
        foreign key (suullinenTuottaminen_id)
            references tekstipalanen;

alter table koto_taitotaso
    add constraint FK_suhq9st3qhuq98921dwa97f6i
        foreign key (suullinenVastaanottaminen_id)
            references tekstipalanen;

alter table koto_taitotaso
    add constraint FK_pkd02mdqfy4k9tjxsch2cusd5
        foreign key (vuorovaikutusJaMeditaatio_id)
            references tekstipalanen;