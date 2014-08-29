alter table only tutkinnonosa
        add tyyppi varchar(255);
alter table only tutkinnonosa_aud
        add tyyppi varchar(255);

update tutkinnonosa set tyyppi = 'NORMAALI';
update tutkinnonosa_aud set tyyppi = 'NORMAALI';

alter table only tutkinnonosa
        alter tyyppi set not null;
alter table only tutkinnonosa_aud
        alter tyyppi set not null;
