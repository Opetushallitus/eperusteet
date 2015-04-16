alter table arviointi_arvioinninkohdealue
    drop constraint uk_arviointi_arvioinninkohdealue;

alter table arviointi_arvioinninkohdealue
    add constraint uk_arviointi_arvioinninkohdealue
        unique(arvioinninkohdealue_id) deferrable initially deferred;

alter table arvioinninkohdealue_arvioinninkohde
    drop constraint uk_arvioinninkohdealue_arvioinninkohde;

alter table arvioinninkohdealue_arvioinninkohde
    add constraint uk_arvioinninkohdealue_arvioinninkohde
        unique(arvioinninkohde_id) deferrable initially deferred;

alter table arvioinninkohde_osaamistasonkriteeri
    drop constraint uk_arvioinninkohde_osaamistasonkriteeri;

alter table arvioinninkohde_osaamistasonkriteeri
    add constraint uk_arvioinninkohde_osaamistasonkriteeri
        unique(osaamistasonkriteerit_id) deferrable initially deferred;
