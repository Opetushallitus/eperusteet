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

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import fi.vm.sade.eperusteet.domain.yl.Kurssi;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * User: tommiratamaa
 * Date: 9.9.15
 * Time: 10.22
 */
@Entity
@Audited
@PrimaryKeyJoinColumn(name = "id")
@Table(name = "yl_lukiokurssi", schema = "public")
public class Lukiokurssi extends Kurssi {
    public static Predicate<Lukiokurssi> inPeruste(long perusteId) {
        return kurssi -> kurssi.getPerusteenSisalto().getPeruste().getId().equals(perusteId);
    }

    @Getter
    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LukiokurssiTyyppi tyyppi;

    @Getter
    @Setter
    @JoinColumn(name = "sisalto_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private LukiokoulutuksenPerusteenSisalto perusteenSisalto;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = WhitelistType.SIMPLIFIED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "kurssityypin_kuvaus_id")
    private TekstiPalanen kurssityypinKuvaus;

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "tavoitteet_id")
    private TekstiPalanen tavoitteet;

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "sisallot_id")
    private TekstiPalanen sisallot;

    @Getter
    @OneToMany(mappedBy = "kurssi", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<OppiaineLukiokurssi> oppiaineet = new HashSet<>(0);
}
