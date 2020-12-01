alter table yl_oppiaineen_vlkok add column opetuksenTavoitteetOtsikko_id int8;
alter table yl_oppiaineen_vlkok_aud add column opetuksenTavoitteetOtsikko_id int8;

alter table yl_oppiaineen_vlkok
    add constraint FK_bewp4yfhas245i63cfq39ovot
    foreign key (opetuksenTavoitteetOtsikko_id)
    references tekstipalanen;
