alter table only perusteprojekti
        add tila varchar(255);
alter table only perusteprojekti_aud
        add tila varchar(255);

update perusteprojekti set tila = 'LAADINTA';
update perusteprojekti_aud set tila = 'LAADINTA';

alter table only perusteprojekti
        alter tila set not null;
