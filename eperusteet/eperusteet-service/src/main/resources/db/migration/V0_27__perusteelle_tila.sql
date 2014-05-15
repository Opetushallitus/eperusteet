alter table only peruste
        add tila varchar(255);
alter table only peruste_aud
        add tila varchar(255);

update peruste set tila = 'VALMIS';
update peruste_aud set tila = 'VALMIS';

alter table only peruste
        alter tila set not null;



