update tutkinnonosaviite_aud set revtype = 2 where poistettu = true and revend is null;
delete from tutkinnonosaviite where poistettu = true;
alter table tutkinnonosaviite drop column poistettu;
alter table tutkinnonosaviite_aud drop column poistettu;

