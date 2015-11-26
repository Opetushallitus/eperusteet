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

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.*;

import fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author jhyoty
 */
@Audited
@Table(name = "yl_tekstiosa")
@Entity
public class TekstiOsa implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    private Long id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen otsikko;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml
    private TekstiPalanen teksti;

    // ----------------------------------------------------------------------------------------------------
    // Relations back to Peruste. Need to be known if only this entity is modified.
    // Read only, meant to be filled by Hibernate
    // ----------------------------------------------------------------------------------------------------

    // Oppiaine tehtava, tavoitteet, arviointi
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(mappedBy = "tehtava", fetch = FetchType.LAZY)
    private Oppiaine oppiaineByTehtava;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(mappedBy = "tavoitteet", fetch = FetchType.LAZY)
    private Oppiaine oppiaineByTavoitteet;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(mappedBy = "arviointi", fetch = FetchType.LAZY)
    private Oppiaine oppiaineByArviointi;
    // OppiaineenVuosiluokkaKokonaisuus: tehtava, tyotavat, ohjaus, arviointi, sisaltoalueinfo
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(mappedBy = "tehtava", fetch = FetchType.LAZY)
    private OppiaineenVuosiluokkaKokonaisuus oavlkByTehtava;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(mappedBy = "tyotavat", fetch = FetchType.LAZY)
    private OppiaineenVuosiluokkaKokonaisuus oavlkByTyotavat;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(mappedBy = "ohjaus", fetch = FetchType.LAZY)
    private OppiaineenVuosiluokkaKokonaisuus oavlkByOhjaus;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(mappedBy = "arviointi", fetch = FetchType.LAZY)
    private OppiaineenVuosiluokkaKokonaisuus oavlkByArviointi;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(mappedBy = "sisaltoalueinfo", fetch = FetchType.LAZY)
    private OppiaineenVuosiluokkaKokonaisuus oavlkBySisaltoalueinfo;
    // VuosiluokkaKokonaisuus: siirtymaEdellisesta, tehtava, siirtymaSeuraavaan,
    // paikallisestiPaatettavatAsiat, laajaalainenOsaaminen
    @Getter @RelatesToPeruste @NotAudited
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "siirtymaEdellisesta")
    private Set<VuosiluokkaKokonaisuus> vlkBySiirtymaEdellisesta;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "tehtava")
    private VuosiluokkaKokonaisuus vlkByTehtava;
    @Getter @RelatesToPeruste @NotAudited
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "siirtymaSeuraavaan")
    private Set<VuosiluokkaKokonaisuus> vlkBySiirtymaSeuraavaan;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "paikallisestiPaatettavatAsiat")
    private VuosiluokkaKokonaisuus vlkByPaikallisestiPaatettavatAsiat;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "laajaalainenOsaaminen")
    private VuosiluokkaKokonaisuus vlkByLaajalainenOsaaminen;
    // Lukiokurssi: tavoitteet, keskeinenSisalto, tavoitteetJaKeskeinenSisalto
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "tavoitteet")
    private Lukiokurssi lukioKurssiByTavoitteet;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "keskeinenSisalto")
    private Lukiokurssi lukioKurssiByKeskeinenSisalto;
    @Getter @RelatesToPeruste @NotAudited
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "tavoitteetJaKeskeinenSisalto")
    private Lukiokurssi lukioKurssiByTavoitteetJaKeskeinenSisalto;


    public TekstiOsa() {
    }

    public TekstiOsa(TekstiPalanen otsikko, TekstiPalanen teksti) {
        this.otsikko = otsikko;
        this.teksti = teksti;
    }

    public TekstiOsa(TekstiOsa other) {
        this.otsikko = other.getOtsikko();
        this.teksti = other.getTeksti();
    }

}
