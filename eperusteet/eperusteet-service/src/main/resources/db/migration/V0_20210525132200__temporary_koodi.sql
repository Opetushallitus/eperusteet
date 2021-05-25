alter table koodi add column nimi_id int8;

alter table koodi
    add constraint FK_5awewwdjbjid1x3kbpuxxfvx8
    foreign key (nimi_id)
    references tekstipalanen;

alter table perusteen_tutkintonimikkeet add column nimi_id int8;
alter table perusteen_tutkintonimikkeet alter column tutkintonimike_koodi_arvo drop not null;

alter table perusteen_tutkintonimikkeet
    add constraint FK_pgjppwcjbf4gl7l0xona61n6w
    foreign key (nimi_id)
    references tekstipalanen;
