update perusteenosaviite
set perusteenosa_id = null
where id in (with recursive vanhemmat(id, vanhempi_id, perusteenosa_id) as
                                (select pv.id, pv.vanhempi_id, pv.perusteenosa_id
                                 from perusteenosaviite pv
                                 where pv.id =
                                       (select sisalto_id from yl_perusop_perusteen_sisalto where peruste_id = 7473970)
                                 union all
                                 select pv.id, pv.vanhempi_id, pv.perusteenosa_id
                                 from perusteenosaviite pv,
                                      vanhemmat v
                                 where pv.vanhempi_id = v.id)
             select id
             from vanhemmat);

update perusteenosaviite
set vanhempi_id = null
where vanhempi_id in (select sisalto_id from yl_perusop_perusteen_sisalto where peruste_id = 7473970);

update yl_oppiaine
set tehtava_id    = null,
    arviointi_id  = null,
    tavoitteet_id = null
where id in (select oppi.id
             from yl_perusop_perusteen_sisalto_yl_oppiaine sis_op
                      inner join yl_oppiaine oppi on oppi.id = sis_op.oppiaineet_id
                      inner join yl_perusop_perusteen_sisalto sisalto
                                 on sis_op.yl_perusop_perusteen_sisalto_id = sisalto.id
             where sisalto.peruste_id = 7473970
             union all
             select oppimaara.id
             from yl_perusop_perusteen_sisalto_yl_oppiaine sis_op
                      inner join yl_oppiaine oppi on oppi.id = sis_op.oppiaineet_id
                      inner join yl_oppiaine oppimaara on oppimaara.oppiaine_id = oppi.id
                      inner join yl_perusop_perusteen_sisalto sisalto
                                 on sis_op.yl_perusop_perusteen_sisalto_id = sisalto.id
             where sisalto.peruste_id = 7473970);
;

update yl_oppiaineen_vlkok
set arviointi_id       = null,
    ohjaus_id          = null,
    tehtava_id         = null,
    tyotavat_id        = null,
    sisaltoalueinfo_id = null
where oppiaine_id in (select oppi.id
                      from yl_perusop_perusteen_sisalto_yl_oppiaine sis_op
                               inner join yl_oppiaine oppi on oppi.id = sis_op.oppiaineet_id
                               inner join yl_perusop_perusteen_sisalto sisalto
                                          on sis_op.yl_perusop_perusteen_sisalto_id = sisalto.id
                      where sisalto.peruste_id = 7473970
                      union all
                      select oppimaara.id
                      from yl_perusop_perusteen_sisalto_yl_oppiaine sis_op
                               inner join yl_oppiaine oppi on oppi.id = sis_op.oppiaineet_id
                               inner join yl_oppiaine oppimaara on oppimaara.oppiaine_id = oppi.id
                               inner join yl_perusop_perusteen_sisalto sisalto
                                          on sis_op.yl_perusop_perusteen_sisalto_id = sisalto.id
                      where sisalto.peruste_id = 7473970);

delete
from yl_perusop_perusteen_sisalto_yl_laajaalainen_osaaminen
where yl_perusop_perusteen_sisalto_id = (select id from yl_perusop_perusteen_sisalto where peruste_id = 7473970)