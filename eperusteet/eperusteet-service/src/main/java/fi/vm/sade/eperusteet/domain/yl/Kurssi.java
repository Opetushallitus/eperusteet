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
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.UUID;

/**
 * User: tommiratamaa
 * Date: 9.9.15
 * Time: 10.22
 */
@Entity
@Table(name = "yl_kurssi", schema = "public")
@Audited
@Inheritance(strategy = InheritanceType.JOINED)
public class Kurssi extends AbstractAuditedReferenceableEntity {

    @Getter
    @Column(nullable = false, unique = true, updatable = false)
    private UUID tunniste =UUID.randomUUID();

    @Getter
    @Setter
    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nimi_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @ValidHtml
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kuvaus_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    @Column(name = "koodi_uri")
    private String koodiUri;

    @Getter
    @Setter
    @Column(name = "koodi_arvo")
    private String koodiArvo;
}
