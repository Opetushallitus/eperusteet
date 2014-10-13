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
package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "yl_perusop_perusteen_sisalto")
public class PerusopetuksenPerusteenSisalto extends AbstractAuditedReferenceableEntity {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @NotNull
    @JoinColumn(nullable = false, updatable = false)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @JoinColumn
    private PerusteenOsaViite sisalto = new PerusteenOsaViite();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable
    private Set<LaajaalainenOsaaminen> laajaAlalaisetOsaamiset = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable
    private Set<Oppiaine> oppiaineet = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable
    private Set<VuosiluokkaKokonaisuus> vuosiluokkakokonaisuudet = new HashSet<>();

    public void addOppiaine(Oppiaine oppiaine) {
        oppiaineet.add(oppiaine);
    }

    public void addVuosiluokkakokonaisuus(VuosiluokkaKokonaisuus kokonaisuus) {
        vuosiluokkakokonaisuudet.add(kokonaisuus);
    }

    public boolean containsOppiaine(Oppiaine aine) {
        return aine != null && oppiaineet.contains(aine);
    }

    public boolean containsVuosiluokkakokonaisuus(VuosiluokkaKokonaisuus kokonaisuus) {
        return kokonaisuus != null && vuosiluokkakokonaisuudet.contains(kokonaisuus);
    }

    //kopion palauttaminen on tarkoituksellista!
    public Set<LaajaalainenOsaaminen> getLaajaAlalaisetOsaamiset() {
        return new HashSet<>(laajaAlalaisetOsaamiset);
    }

    public void setLaajaAlalaisetOsaamiset(Set<LaajaalainenOsaaminen> laajaAlalaisetOsaamiset) {
        if (laajaAlalaisetOsaamiset == null) {
            this.laajaAlalaisetOsaamiset.clear();
        }
        this.laajaAlalaisetOsaamiset.retainAll(laajaAlalaisetOsaamiset);
        this.laajaAlalaisetOsaamiset.addAll(laajaAlalaisetOsaamiset);
    }

    public Set<Oppiaine> getOppiaineet() {
        return new HashSet<>(oppiaineet);
    }

    public void setOppiaineet(Set<Oppiaine> oppiaineet) {
        if (oppiaineet == null) {
            this.oppiaineet.clear();
        }
        this.oppiaineet.retainAll(oppiaineet);
        this.oppiaineet.addAll(oppiaineet);
    }

    public Set<VuosiluokkaKokonaisuus> getVuosiluokkakokonaisuudet() {
        return new HashSet<>(vuosiluokkakokonaisuudet);
    }

    public void setVuosiluokkakokonaisuudet(Set<VuosiluokkaKokonaisuus> vuosiluokkakokonaisuudet) {
        if (vuosiluokkakokonaisuudet == null) {
            this.vuosiluokkakokonaisuudet.clear();
            return;
        }
        this.vuosiluokkakokonaisuudet.retainAll(vuosiluokkakokonaisuudet);
        this.vuosiluokkakokonaisuudet.addAll(vuosiluokkakokonaisuudet);
    }

}
