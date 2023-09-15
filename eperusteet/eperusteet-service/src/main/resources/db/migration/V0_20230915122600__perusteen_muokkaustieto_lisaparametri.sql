create table perusteen_muokkaustieto_lisaparametrit (
    PerusteenMuokkaustieto_id int8 not null,
    kohde varchar(255),
    kohde_id int8
);

alter table perusteen_muokkaustieto_lisaparametrit
    add constraint FK_sfxe0tqr2cvpnv9a8bw7yctao
    foreign key (PerusteenMuokkaustieto_id)
    references perusteen_muokkaustieto;