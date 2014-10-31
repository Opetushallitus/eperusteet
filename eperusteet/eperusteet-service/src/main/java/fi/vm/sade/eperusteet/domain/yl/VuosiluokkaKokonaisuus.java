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
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "yl_vlkokonaisuus")
@Audited
public class VuosiluokkaKokonaisuus extends AbstractAuditedReferenceableEntity {

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    @CollectionTable(name = "yl_vlkok_vuosiluokat")
    @Column(name = "vuosiluokka")
    private Set<Vuosiluokka> vuosiluokat = EnumSet.noneOf(Vuosiluokka.class);

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa tehtava;

    //TODO: siirtymä (kumpaankin suuntaan -- jaettu vuosiluokkakokonaisuuksien välillä (paitsi ensimmäinen ja viimeinen)
    //TODO: vuosiluokat josta kokonaisuus koostuu.
    @OneToMany(mappedBy = "vuosiluokkaKokonaisuus")
    private Set<OppiaineenVuosiluokkaKokonaisuus> oppiaineet = new HashSet<>();

    @OneToMany(mappedBy = "vuosiluokkaKokonaisuus", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VuosiluokkaKokonaisuudenLaajaalainenOsaaminen> laajaalaisetOsaamiset = new HashSet<>();

    public Set<VuosiluokkaKokonaisuudenLaajaalainenOsaaminen> getLaajaalaisetOsaamiset() {
        return new HashSet<>(laajaalaisetOsaamiset);
    }

    public Set<OppiaineenVuosiluokkaKokonaisuus> getOppiaineet() {
        return new HashSet<>(oppiaineet);
    }

    public void setLaajaalaisetOsaamiset(Set<VuosiluokkaKokonaisuudenLaajaalainenOsaaminen> laajaalainenOsaamiset) {

        if (laajaalainenOsaamiset == null) {
            this.laajaalaisetOsaamiset.clear();
            return;
        }

        this.laajaalaisetOsaamiset.retainAll(laajaalainenOsaamiset);
        this.laajaalaisetOsaamiset.addAll(laajaalainenOsaamiset);
        for (VuosiluokkaKokonaisuudenLaajaalainenOsaaminen v : laajaalainenOsaamiset) {
            v.setVuosiluokkaKokonaisuus(this);
        }
    }

    //hiberate javaassist proxy "workaround"
    //ilman equals-metodia objectX.equals(proxy-objectX) on aina false
    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

}
