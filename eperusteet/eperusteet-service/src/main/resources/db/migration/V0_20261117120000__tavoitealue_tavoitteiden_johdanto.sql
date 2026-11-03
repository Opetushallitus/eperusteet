alter table tavoitealue
    add column tavoitteidenJohdanto_id int8;

alter table tavoitealue_AUD
    add column tavoitteidenJohdanto_id int8;

alter table tavoitealue
    add constraint FK_tavoitealue_tavoitteiden_johdanto
    foreign key (tavoitteidenJohdanto_id)
    references tekstipalanen;
