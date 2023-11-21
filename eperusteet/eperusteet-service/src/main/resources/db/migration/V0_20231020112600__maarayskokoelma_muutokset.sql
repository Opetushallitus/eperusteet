alter table julkaistu_peruste add column muutosmaarays_id int8;

alter table julkaistu_peruste
    add constraint FK_f42w7efkhcps01lmsqfc1c76b
    foreign key (muutosmaarays_id)
    references maarays;