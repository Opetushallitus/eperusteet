ALTER TABLE koulutuksenosa ADD COLUMN arvioinninKuvaus_id int8;
ALTER TABLE koulutuksenosa ADD COLUMN laajaAlaisenOsaamisenKuvaus_id int8;
ALTER TABLE koulutuksenosa ADD COLUMN tavoitteenKuvaus_id int8;

ALTER TABLE koulutuksenosa_aud ADD COLUMN arvioinninKuvaus_id int8;
ALTER TABLE koulutuksenosa_aud ADD COLUMN laajaAlaisenOsaamisenKuvaus_id int8;
ALTER TABLE koulutuksenosa_aud ADD COLUMN tavoitteenKuvaus_id int8;

alter table koulutuksenosa
    add constraint FK_tijy5iff70pywvluo4kenvndk
    foreign key (arvioinninKuvaus_id)
    references tekstipalanen;

alter table koulutuksenosa
    add constraint FK_qql3kp58p6xq5g87h2sdawba9
    foreign key (laajaAlaisenOsaamisenKuvaus_id)
    references tekstipalanen;

alter table koulutuksenosa
    add constraint FK_ghrqfo30fqkk2u4eh9ehbw3sr
    foreign key (tavoitteenKuvaus_id)
    references tekstipalanen;
