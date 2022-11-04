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
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.tuva.TutkintoonvalmentavaSisalto;
import fi.vm.sade.eperusteet.domain.vst.VapaasivistystyoSisalto;
import fi.vm.sade.eperusteet.domain.yl.EsiopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.TpoOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
public class PerusteenOsaViite implements
        ReferenceableEntity,
        Serializable,
        Copyable<PerusteenOsaViite>,
        HistoriaTapahtuma {

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
    private Lops2019Sisalto lops2019Sisalto;

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
    private TpoOpetuksenSisalto tpoOpetuksenSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private OpasSisalto opasSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private VapaasivistystyoSisalto vstSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private TutkintoonvalmentavaSisalto tuvaSisalto;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    private PerusteenOsa perusteenOsa;

    @OneToMany(mappedBy = "vanhempi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderColumn
    @Getter
    @Setter
    private List<PerusteenOsaViite> lapset = new ArrayList<>();

    public PerusteenOsaViite() {
    }

    public PerusteenOsaViite(final Suoritustapa suoritustapa) {
        this.suoritustapa= suoritustapa;
    }

    public PerusteenOsaViite(final PerusopetuksenPerusteenSisalto sisalto) {
        this.perusopetuksenPerusteenSisalto = sisalto;
    }

    public PerusteenOsaViite(final LukiokoulutuksenPerusteenSisalto sisalto) {
        this.lukiokoulutuksenPerusteenSisalto = sisalto;
    }

    public PerusteenOsaViite(final Lops2019Sisalto sisalto) {
        this.lops2019Sisalto = sisalto;
    }

    public PerusteenOsaViite(final EsiopetuksenPerusteenSisalto sisalto) {
        this.esiopetuksenPerusteenSisalto = sisalto;
    }

    public PerusteenOsaViite(final OpasSisalto sisalto) {
        this.opasSisalto = sisalto;
    }

    public PerusteenOsaViite(final AIPEOpetuksenSisalto sisalto) {
        this.aipeSisalto = sisalto;
    }

    public PerusteenOsaViite(final TpoOpetuksenSisalto sisalto) {
        this.tpoOpetuksenSisalto = sisalto;
    }

    public PerusteenOsaViite(final VapaasivistystyoSisalto sisalto) {
        this.vstSisalto = sisalto;
    }

    public PerusteenOsaViite(final TutkintoonvalmentavaSisalto sisalto) {
        this.tuvaSisalto = sisalto;
    }

    @Override
    public Reference getReference() {
        return new Reference(id);
    }

    public PerusteenOsaViite getRoot() {
        PerusteenOsaViite root = this;
        while (root.getVanhempi() != null) {
            root = root.getVanhempi();
        }
        return root;
    }

    @Override
    public PerusteenOsaViite copy(final boolean deep) {
        final PerusteenOsaViite pov = new PerusteenOsaViite();
        if (this.getPerusteenOsa() != null) {
            pov.setPerusteenOsa(this.getPerusteenOsa().copy());
        }

        final List<PerusteenOsaViite> uudetLapset = new ArrayList<>();
        for (final PerusteenOsaViite lapsi : lapset) {
            final PerusteenOsaViite kloonattu = lapsi.copy();
            kloonattu.setVanhempi(pov);
            uudetLapset.add(kloonattu);
        }
        pov.setLapset(uudetLapset);
        return pov;
    }

    public NavigationNodeDto constructNavigation(DtoMapper mapper) {
        NavigationType type = NavigationType.viite;
        PerusteenOsa po = this.getPerusteenOsa();
        if (po instanceof TekstiKappale) {
            TekstiKappale tk = (TekstiKappale) po;
            if (tk.isLiite()) {
                type = NavigationType.liite;
            }
            else if (tk.getTunniste() != null) {
                switch (tk.getTunniste()) {
                    case NORMAALI:
                        type = NavigationType.viite;
                        break;
                    case LAAJAALAINENOSAAMINEN:
                        type = NavigationType.laajaalaiset;
                        break;
                    case RAKENNE:
                        type = NavigationType.muodostuminen;
                        break;
                }
            }
        }

        return NavigationNodeDto
                .of(type, this.getPerusteenOsa() != null
                                ? mapper.map(
                        this.getPerusteenOsa().getNimi(),
                        LokalisoituTekstiDto.class)
                                : null,
                        getId())
                .addAll(getLapset().stream()
                    .map(node -> node.constructNavigation(mapper))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
    }



    @Override
    public Date getLuotu() {
        return this.perusteenOsa.getLuotu();
    }

    @Override
    public Date getMuokattu() {
        return this.perusteenOsa.getMuokattu();
    }

    @Override
    public String getLuoja() {
        return this.perusteenOsa.getLuoja();
    }

    @Override
    public String getMuokkaaja() {
        return this.perusteenOsa.getMuokkaaja();
    }

    @Override
    public TekstiPalanen getNimi() {
        return this.perusteenOsa.getNimi();
    }

    @Override
    public NavigationType getNavigationType() {
        if (this.perusteenOsa != null) {
            return this.perusteenOsa.getNavigationType();
        }
        return NavigationType.viite;
    }
}
