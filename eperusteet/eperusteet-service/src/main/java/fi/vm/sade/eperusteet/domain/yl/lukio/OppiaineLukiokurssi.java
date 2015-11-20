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
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * User: tommiratamaa
 * Date: 9.9.15
 * Time: 12.15
 */
@Entity
@Audited
@Table(name = "yl_oppaine_yl_lukiokurssi", schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"kurssi_id", "oppiaine_id"}),
                @UniqueConstraint(columnNames = {"oppiaine_id", "jarjestys"})
        })
public class OppiaineLukiokurssi extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kurssi_id", nullable = false)
    private Lukiokurssi kurssi;

    @Getter
    @Setter
    @RelatesToPeruste
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oppiaine_id", nullable = false)
    private Oppiaine oppiaine;

    @Getter
    @Setter
    @Column(nullable = false)
    private Integer jarjestys;
}
