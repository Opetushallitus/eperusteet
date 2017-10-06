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
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author jhyoty
 */
@Entity
@Table(name = "yl_vlkokonaisuus")
@Audited
public class VuosiluokkaKokonaisuus extends AbstractAuditedReferenceableEntity {

    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

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
    @ManyToOne(cascade = CascadeType.ALL, optional = true)
    private TekstiOsa siirtymaEdellisesta;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa tehtava;

    @Getter
    @Setter
    @ManyToOne(cascade = CascadeType.ALL, optional = true)
    private TekstiOsa siirtymaSeuraavaan;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa paikallisestiPaatettavatAsiat;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa laajaalainenOsaaminen;

    @OneToMany(mappedBy = "vuosiluokkaKokonaisuus")
    private Set<OppiaineenVuosiluokkaKokonaisuus> oppiaineet = new HashSet<>();

    @OneToMany(mappedBy = "vuosiluokkaKokonaisuus", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VuosiluokkaKokonaisuudenLaajaalainenOsaaminen> laajaalaisetOsaamiset = new HashSet<>();

    @RelatesToPeruste
    @Getter
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_perusop_perusteen_sisalto_yl_vlkokonaisuus",
            joinColumns = @JoinColumn(name = "vuosiluokkakokonaisuudet_id", nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_perusop_perusteen_sisalto_id", nullable = false, updatable = false))
    private Set<PerusopetuksenPerusteenSisalto> perusopetuksenPerusteenSisallot = new HashSet<>();

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

    public VuosiluokkaKokonaisuus kloonaa() {
        VuosiluokkaKokonaisuus vlk = new VuosiluokkaKokonaisuus();
        vlk.setLaajaalainenOsaaminen(laajaalainenOsaaminen == null ? null : new TekstiOsa(laajaalainenOsaaminen));
        vlk.setNimi(nimi);
        vlk.setTehtava(tehtava);
        vlk.setVuosiluokat(new HashSet<>(vuosiluokat));
        vlk.tunniste = this.tunniste;
        return vlk;
    }

}
