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

package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "koulutuskoodi_status_info")
public class KoulutuskoodiStatusInfo {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Suoritustapakoodi suoritustapa;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    private TutkinnonOsaViite viite;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KoulutuskoodiStatusInfo oInfo = (KoulutuskoodiStatusInfo) o;
        TutkinnonOsaViite oViite = oInfo.getViite();
        if (viite == null || oViite == null) return false;
        return suoritustapa == oInfo.suoritustapa &&
                Objects.equals(this.viite.getId(), oInfo.viite.getId());
    }
}
