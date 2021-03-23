ALTER TABLE osaamistaso ADD COLUMN koodi_id int8;

alter table osaamistaso
    add constraint FK_9lj71d2u08e63vas5i0w4p5nj
    foreign key (koodi_id)
    references koodi;
