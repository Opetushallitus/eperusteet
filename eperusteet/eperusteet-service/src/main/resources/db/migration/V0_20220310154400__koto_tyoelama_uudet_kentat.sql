alter table koto_taitotaso add column opiskelijanTyoelamataidot_id bigint;
alter table koto_taitotaso add column tyoelamaOpintoMinimiLaajuus smallint;
alter table koto_taitotaso add column tyoelamaOpintoMaksimiLaajuus smallint;

alter table koto_taitotaso_aud add column opiskelijanTyoelamataidot_id bigint;
alter table koto_taitotaso_aud add column tyoelamaOpintoMinimiLaajuus smallint;
alter table koto_taitotaso_aud add column tyoelamaOpintoMaksimiLaajuus smallint;

alter table koto_taitotaso
    add constraint FK_4h76eyjxiji0c4kejakptk1iu
        foreign key (opiskelijanTyoelamataidot_id)
            references tekstipalanen;
