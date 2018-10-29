/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.domain.yl.lukio;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.tekstihaku.TekstihakuCollection;
import fi.vm.sade.eperusteet.domain.tekstihaku.TekstihakuCtx;
import fi.vm.sade.eperusteet.domain.yl.NimettyKoodillinen;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import static java.util.stream.Stream.concat;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * User: tommiratamaa
 * Date: 19.10.15
 * Time: 10.46
 */
@Entity
@Audited
@Table(name = "yl_lukio_opetussuunnitelma_rakenne", schema = "public")
public class LukioOpetussuunnitelmaRakenne extends PerusteenOsa {
    public static Predicate<LukioOpetussuunnitelmaRakenne> inPeruste(long perusteId) {
        return rakenne -> rakenne.getSisalto().getPeruste().getId().equals(perusteId);
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @Getter
    @Setter
    @JoinColumn(name="viite_id", nullable = false)
    private PerusteenOsaViite viite = new PerusteenOsaViite();

    @RelatesToPeruste
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "sisalto_id", nullable = false)
    private LukiokoulutuksenPerusteenSisalto sisalto;

    @Getter
    @Audited
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine",
            joinColumns = @JoinColumn(name = "rakenne_id", nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "oppiaine_id", nullable = false, updatable = false))
    private Set<Oppiaine> oppiaineet = new HashSet<>(0);

    @Getter
    @Audited
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "opetussuunnitelma")
    private Set<Lukiokurssi> kurssit = new HashSet<>(0);

    public LukioOpetussuunnitelmaRakenne kloonaa(LukiokoulutuksenPerusteenSisalto sisalto) {
        LukioOpetussuunnitelmaRakenne kopio = new LukioOpetussuunnitelmaRakenne();
        kopio.sisalto = sisalto;
        kopio.oppiaineet.addAll(this.oppiaineet.stream().map(Oppiaine::kloonaa).collect(toList()));
        kopio.kurssit.addAll(this.kurssit.stream().map(k -> k.kloonaa(kopio)).collect(toList()));
        return kopio;
    }

    @Override
    public PerusteenOsa copy() {
        return kloonaa(null);
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(getId());
    }

    public Stream<Lukiokurssi> kurssit() {
        return getKurssit().stream();
    }
    public Stream<Oppiaine> oppiaineet() {
        return getOppiaineet().stream();
    }
    public Stream<Oppiaine> oppiaineetMaarineen() {
        return oppiaineet().flatMap(Oppiaine::maarineen);
    }
    public Stream<NimettyKoodillinen> koodilliset() {
        return concat(oppiaineetMaarineen(), kurssit());
    }

    @Override
    public void getTekstihaku(TekstihakuCollection haku) {

    }

    @Override
    public TekstihakuCtx partialContext() {
        return null;
    }
}
