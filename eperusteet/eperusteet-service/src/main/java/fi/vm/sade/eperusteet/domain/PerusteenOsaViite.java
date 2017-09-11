/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.yl.EsiopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.TPOOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 *
 * @author jhyoty
 *
 */
@Entity
@Audited
@Table(name = "perusteenosaviite")
@NamedNativeQuery(
    name = "PerusteenOsaViite.findRootsByPerusteenOsaId",
    resultSetMapping = "PerusteenOsaViite.rootId",
    query
    = "with recursive vanhemmat(id,vanhempi_id,perusteenosa_id) as "
    + "(select pv.id, pv.vanhempi_id, pv.perusteenosa_id from perusteenosaviite pv "
    + "where pv.perusteenosa_id = ?1  "
    + "union all "
    + "select pv.id, pv.vanhempi_id, v.perusteenosa_id "
    + "from perusteenosaviite pv, vanhemmat v where pv.id = v.vanhempi_id) "
    + "select id from vanhemmat where vanhempi_id is null")
@SqlResultSetMapping(
    name = "PerusteenOsaViite.rootId",
    columns = {@ColumnResult(name="id", type=Long.class)}
)
public class PerusteenOsaViite implements ReferenceableEntity, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @RelatesToPeruste
    @ManyToOne
    @Getter
    @Setter
    private PerusteenOsaViite vanhempi;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private Suoritustapa suoritustapa;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private PerusopetuksenPerusteenSisalto perusopetuksenPerusteenSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private LukiokoulutuksenPerusteenSisalto lukiokoulutuksenPerusteenSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private EsiopetuksenPerusteenSisalto esiopetuksenPerusteenSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private AIPEOpetuksenSisalto aipeSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private TPOOpetuksenSisalto tpoOpetuksenSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private OpasSisalto opasSisalto;

    @ManyToOne
    @Getter
    @Setter
    private PerusteenOsa perusteenOsa;

    @OneToMany(mappedBy = "vanhempi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderColumn
    @Getter
    @Setter
    @BatchSize(size = 100)
    @ElementCollection
    private List<PerusteenOsaViite> lapset = new ArrayList<>();

    public PerusteenOsaViite() {
    }

    public PerusteenOsaViite(Suoritustapa suoritustapa) {
        this.suoritustapa= suoritustapa;
    }

    public PerusteenOsaViite(PerusopetuksenPerusteenSisalto sisalto) {
        this.perusopetuksenPerusteenSisalto = sisalto;
    }

    public PerusteenOsaViite(LukiokoulutuksenPerusteenSisalto sisalto) {
        this.lukiokoulutuksenPerusteenSisalto = sisalto;
    }

    public PerusteenOsaViite(EsiopetuksenPerusteenSisalto sisalto) {
        this.esiopetuksenPerusteenSisalto = sisalto;
    }

    public PerusteenOsaViite(OpasSisalto sisalto) {
        this.opasSisalto = sisalto;
    }

    public PerusteenOsaViite(AIPEOpetuksenSisalto sisalto) {
        this.aipeSisalto = sisalto;
    }

    public PerusteenOsaViite(TPOOpetuksenSisalto sisalto) {
        this.tpoOpetuksenSisalto = sisalto;
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }

    public PerusteenOsaViite getRoot() {
        PerusteenOsaViite root = this;
        while (root.getVanhempi() != null) {
            root = root.getVanhempi();
        }
        return root;
    }

    public PerusteenOsaViite kloonaa() {
        PerusteenOsaViite pov = new PerusteenOsaViite();
        if (getPerusteenOsa() != null) {
            pov.setPerusteenOsa(getPerusteenOsa());
        }

        List<PerusteenOsaViite> uudetLapset = new ArrayList<>();
        for (PerusteenOsaViite lapsi : lapset) {
            PerusteenOsaViite kloonattu = lapsi.kloonaa();
            kloonattu.setVanhempi(pov);
            uudetLapset.add(kloonattu);
        }
        pov.setLapset(uudetLapset);
        return pov;
    }
}
