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

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * User: tommiratamaa
 * Date: 9.9.15
 * Time: 10.05
 */
@Entity
@Audited
@Table(name = "yl_aihekokonaisuus", schema = "public")
public class Aihekokonaisuus extends AbstractAuditedReferenceableEntity {

    public static Predicate<Aihekokonaisuus> inPeruste(long perusteId) {
        return aihekokonaisuus -> aihekokonaisuus.getAihekokonaisuudet().getSisalto().getPeruste().getId().equals(perusteId);
    }

    @Column(nullable = false, unique = true, updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "otsikko_id", nullable = true)
    private TekstiPalanen otsikko;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "yleiskuvaus_id")
    private TekstiPalanen yleiskuvaus;

    @Getter
    @Setter
    private Long jnro;

    @RelatesToPeruste
    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "aihekokonaisuudet_id", nullable = false)
    private Aihekokonaisuudet aihekokonaisuudet;

    public Aihekokonaisuus kloonaa() {
        Aihekokonaisuus klooni = new Aihekokonaisuus();
        klooni.setJnro(this.getJnro());
        klooni.setOtsikko(this.getOtsikko());
        klooni.setAihekokonaisuudet(this.getAihekokonaisuudet());
        klooni.setYleiskuvaus(this.getYleiskuvaus());

        return klooni;
    }
}
