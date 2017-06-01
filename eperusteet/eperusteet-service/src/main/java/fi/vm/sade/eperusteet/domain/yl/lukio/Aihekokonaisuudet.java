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
package fi.vm.sade.eperusteet.domain.yl.lukio;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jsikio on 28.9.2015.
 */
@Entity
@Audited
@Table(name = "yl_aihekokonaisuudet", schema = "public")
public class Aihekokonaisuudet extends PerusteenOsa {

    @Column(name = "tunniste", nullable = false, unique = true, updatable = false)
    @Getter
    private UUID uuidTunniste = UUID.randomUUID();

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "otsikko_id")
    private TekstiPalanen otsikko;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "yleiskuvaus_id")
    private TekstiPalanen yleiskuvaus;

    @RelatesToPeruste
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "sisalto_id", nullable = false)
    private LukiokoulutuksenPerusteenSisalto sisalto;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @Getter
    @Setter
    @JoinColumn(name="viite_id", nullable = false)
    private PerusteenOsaViite viite = new PerusteenOsaViite();

    @Getter
    @OneToMany(mappedBy = "aihekokonaisuudet", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("jnro")
    private Set<Aihekokonaisuus> aihekokonaisuudet = new HashSet<>(0);

    public Aihekokonaisuudet kloonaa() {
        Aihekokonaisuudet klooni = new Aihekokonaisuudet();
        klooni.setYleiskuvaus(this.getYleiskuvaus());
        klooni.setOtsikko(this.getOtsikko());

        for( Aihekokonaisuus aihekokonaisuus : this.aihekokonaisuudet ) {
            klooni.aihekokonaisuudet.add( aihekokonaisuus.kloonaa() );
        }

        return klooni;
    }

    @Override
    public PerusteenOsa copy() {
        return kloonaa();
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(getId());
    }
}
