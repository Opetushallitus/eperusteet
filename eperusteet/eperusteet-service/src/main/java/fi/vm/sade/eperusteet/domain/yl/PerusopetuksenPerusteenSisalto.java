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
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;

/**
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "yl_perusop_perusteen_sisalto")
public class PerusopetuksenPerusteenSisalto extends AbstractOppiaineOpetuksenSisalto {

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

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinTable
    private Set<LaajaalainenOsaaminen> laajaalaisetosaamiset = new HashSet<>();

    @Getter
    @OneToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    @JoinTable
    private Set<Oppiaine> oppiaineet = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable
    private Set<VuosiluokkaKokonaisuus> vuosiluokkakokonaisuudet = new HashSet<>();

    public void addLaajaalainenosaaminen(LaajaalainenOsaaminen osaaminen) {
        laajaalaisetosaamiset.add(osaaminen);
    }

    public void addVuosiluokkakokonaisuus(VuosiluokkaKokonaisuus kokonaisuus) {
        vuosiluokkakokonaisuudet.add(kokonaisuus);
    }

    public boolean containsLaajaalainenosaaminen(LaajaalainenOsaaminen osaaminen) {
        return laajaalaisetosaamiset.contains(osaaminen);
    }

    public void removeVuosiluokkakokonaisuus(VuosiluokkaKokonaisuus kokonaisuus) {
        vuosiluokkakokonaisuudet.remove(kokonaisuus);
    }

    public void removeLaajaalainenosaaminen(LaajaalainenOsaaminen osaaminen) {
        laajaalaisetosaamiset.remove(osaaminen);
    }

    public boolean containsVuosiluokkakokonaisuus(VuosiluokkaKokonaisuus kokonaisuus) {
        return kokonaisuus != null && vuosiluokkakokonaisuudet.contains(kokonaisuus);
    }

    public LaajaalainenOsaaminen getLaajaalainenosaaminen(long id) {
        for (LaajaalainenOsaaminen l : laajaalaisetosaamiset) {
            if (id == l.getId()) {
                return l;
            }
        }
        return null;
    }

    //kopion palauttaminen on tarkoituksellista!
    public Set<LaajaalainenOsaaminen> getLaajaalaisetosaamiset() {
        return new HashSet<>(laajaalaisetosaamiset);
    }

    public void setLaajaalaisetosaamiset(Set<LaajaalainenOsaaminen> laajaalaisetOsaamiset) {
        if (laajaalaisetOsaamiset == null) {
            this.laajaalaisetosaamiset.clear();
        } else {
            this.laajaalaisetosaamiset.retainAll(laajaalaisetOsaamiset);
            this.laajaalaisetosaamiset.addAll(laajaalaisetOsaamiset);
        }
    }

    public Set<Oppiaine> getOppiaineetCopy() {
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
