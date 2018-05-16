/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * Global version for peruste related changes.
 *
 * Not audited nor versioned in purpose. Seperate from Peruste: won't affect it or it's muokattu timestamp
 * nor cause locking issues.
 *
 * User: tommiratamaa
 * Date: 12.11.2015
 * Time: 15.22
 */
@Entity
@Getter
@Setter
@Table(name = "peruste_version", schema = "public")
public class PerusteVersion {
    @Id
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    @GeneratedValue(generator = "peruste_version_id_seq")
    @SequenceGenerator(name = "peruste_version_id_seq", sequenceName = "peruste_version_id_seq")
    private Long id;

    // NOTE: do not annotate this as @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peruste_id", nullable = false)
    private Peruste peruste;

    @Column(name = "aikaleima", nullable = false)
    private Date aikaleima = new Date();

    public PerusteVersion() {
    }

    public PerusteVersion(Peruste peruste) {
        this.peruste = peruste;
    }
}
