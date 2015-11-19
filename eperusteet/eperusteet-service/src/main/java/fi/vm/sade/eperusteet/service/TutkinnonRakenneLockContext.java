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
package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
public class TutkinnonRakenneLockContext {

    private Long perusteId;
    private Suoritustapakoodi koodi;

    public TutkinnonRakenneLockContext() {
    }

    public TutkinnonRakenneLockContext(Long perusteId, Suoritustapakoodi koodi) {
        this.perusteId = perusteId;
        this.koodi = koodi;
    }

    public void setOsanId(Long perusteId) {
        this.perusteId = perusteId;
    }

    public void setSuoritustapa(Suoritustapakoodi koodi) {
        this.koodi = koodi;
    }

    public static TutkinnonRakenneLockContext of(Long perusteId, Suoritustapakoodi koodi) {
        return new TutkinnonRakenneLockContext(perusteId, koodi);
    }
}
