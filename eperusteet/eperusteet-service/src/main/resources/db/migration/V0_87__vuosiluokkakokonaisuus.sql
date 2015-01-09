alter table yl_vlkokonaisuus
    add column laajaalainenosaaminen_id int8,
    add column paikallisestipaatettavatasiat_id int8,
    add column siirtymaedellisesta_id int8,
    add column siirtymaseuraavaan_id int8;

alter table yl_vlkokonaisuus_aud
    add column laajaalainenosaaminen_id int8,
    add column paikallisestipaatettavatasiat_id int8,
    add column siirtymaedellisesta_id int8,
    add column siirtymaseuraavaan_id int8;

alter table yl_vlkokonaisuus
    add constraint FK_dj9xvhhus7bgc909cb7tue0q9
    foreign key (laajaalainenOsaaminen_id)
    references yl_tekstiosa;

alter table yl_vlkokonaisuus
    add constraint FK_jb0de65mhafdayigdxhaensr3
    foreign key (paikallisestiPaatettavatAsiat_id)
    references yl_tekstiosa;

alter table yl_vlkokonaisuus
    add constraint FK_rcabgshd3i07c3vq8q1vudy15
    foreign key (siirtymaEdellisesta_id)
    references yl_tekstiosa;

alter table yl_vlkokonaisuus
    add constraint FK_6k9lohkhli783p1h53iphylxw
    foreign key (siirtymaSeuraavaan_id)
    references yl_tekstiosa;