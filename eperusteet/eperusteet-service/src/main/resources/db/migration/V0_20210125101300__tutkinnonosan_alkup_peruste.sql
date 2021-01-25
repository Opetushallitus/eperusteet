ALTER TABLE tutkinnonosa ADD COLUMN alkuperainen_peruste_id int8;

ALTER TABLE tutkinnonosa_aud ADD COLUMN alkuperainen_peruste_id int8;

alter table tutkinnonosa
    add constraint FK_2dulh8a1td87bpfg1emrfcc29
    foreign key (alkuperainen_peruste_id)
    references peruste;
