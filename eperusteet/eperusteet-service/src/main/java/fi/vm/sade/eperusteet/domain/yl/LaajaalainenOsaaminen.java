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

import fi.vm.sade.eperusteet.domain.AIPEOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.AbstractReferenceableEntity;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name="yl_laajaalainen_osaaminen")
public class LaajaalainenOsaaminen extends AbstractReferenceableEntity {

    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen nimi;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen kuvaus;

    @Getter
    @NotAudited
    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_perusop_perusteen_sisalto_yl_laajaalainen_osaaminen",
            joinColumns = @JoinColumn(name = "laajaalaisetosaamiset_id", updatable = false, nullable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_perusop_perusteen_sisalto_id", nullable = false, updatable = false))
    private Set<PerusopetuksenPerusteenSisalto> perusopetuksenPerusteenSisallot = new HashSet<>();


    @Getter
    @NotAudited
    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_aipe_opetuksensisalto_yl_laajaalainen_osaaminen",
            joinColumns = @JoinColumn(name = "laajaalaisetosaamiset_id", updatable = false, nullable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_aipe_opetuksensisalto_id", nullable = false, updatable = false))
    private Set<AIPEOpetuksenSisalto> aipeSisallot = new HashSet<>();

    @Getter
    @NotAudited
    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_opetuksen_tavoite_yl_laajaalainen_osaaminen",
            joinColumns = @JoinColumn(name = "laajattavoitteet_id", nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_opetuksen_tavoite_id", nullable = false, updatable = false))
    private Set<OpetuksenTavoite> opetuksenTavoitteet = new HashSet<>();

    public LaajaalainenOsaaminen kloonaa() {
        LaajaalainenOsaaminen uusiLaaja = new LaajaalainenOsaaminen();
        uusiLaaja.setKuvaus(kuvaus);
        uusiLaaja.setNimi(nimi);
        return uusiLaaja;
    }
}
