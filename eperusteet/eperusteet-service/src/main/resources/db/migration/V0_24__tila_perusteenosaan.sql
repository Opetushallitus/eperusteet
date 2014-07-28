alter table only perusteenosa
        add tila varchar(255);

update perusteenosa set tila = 'LUONNOS';

alter table only perusteenosa
        alter tila set not null;
