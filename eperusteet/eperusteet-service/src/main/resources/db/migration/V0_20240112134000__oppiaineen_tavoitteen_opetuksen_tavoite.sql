create table yl_oppiaineen_tavoitteen_opetuksen_tavoite (
                                                            id int8 not null,
                                                            tavoite_id int8 not null,
                                                            primary key (id)
);

create table yl_oppiaineen_tavoitteen_opetuksen_tavoite_AUD (
                                                                id int8 not null,
                                                                REV int4 not null,
                                                                REVTYPE int2,
                                                                REVEND int4,
                                                                tavoite_id int8,
                                                                primary key (id, REV)
);

create table yl_oppiaineen_tavoite_yl_opetuksen_tavoite (
                                                            tavoitteet_id int8 not null,
                                                            yl_oppiaineen_tavoitteen_opetuksen_tavoite_id int8 not null,
                                                            oppiaineenTavoitteenOpetuksenTavoitteet_ORDER int4 not null,
                                                            primary key (tavoitteet_id, oppiaineenTavoitteenOpetuksenTavoitteet_ORDER)
);

create table yl_oppiaineen_tavoite_yl_opetuksen_tavoite_AUD (
                                                                REV int4 not null,
                                                                tavoitteet_id int8 not null,
                                                                yl_oppiaineen_tavoitteen_opetuksen_tavoite_id int8 not null,
                                                                oppiaineenTavoitteenOpetuksenTavoitteet_ORDER int4 not null,
                                                                REVTYPE int2,
                                                                REVEND int4,
                                                                primary key (REV, tavoitteet_id, yl_oppiaineen_tavoitteen_opetuksen_tavoite_id, oppiaineenTavoitteenOpetuksenTavoitteet_ORDER)
);

alter table yl_oppiaineen_tavoite_yl_opetuksen_tavoite
    add constraint FK_lr0mk68lss3fl4mx9ox2p7ycf
        foreign key (yl_oppiaineen_tavoitteen_opetuksen_tavoite_id)
            references yl_oppiaineen_tavoitteen_opetuksen_tavoite;

alter table yl_oppiaineen_tavoite_yl_opetuksen_tavoite
    add constraint FK_7op17kd7l3b5m001iwd05khyt
        foreign key (tavoitteet_id)
            references yl_opetuksen_tavoite;

alter table yl_oppiaineen_tavoite_yl_opetuksen_tavoite_AUD
    add constraint FK_tas6bdg03k8xoy03t9p9fve33
        foreign key (REV)
            references revinfo;

alter table yl_oppiaineen_tavoite_yl_opetuksen_tavoite_AUD
    add constraint FK_idmeun540ply1rqjrtieai1cs
        foreign key (REVEND)
            references revinfo;
