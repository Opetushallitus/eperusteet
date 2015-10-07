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
package fi.vm.sade.eperusteet.service.yl;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
public class OppiaineLockContext {

    Long perusteId;
    OppiaineOpetuksenSisaltoTyyppi tyyppi;
    Long oppiaineId;
    Long kokonaisuusId;

    public OppiaineLockContext() {
    }

    public OppiaineLockContext(OppiaineOpetuksenSisaltoTyyppi tyyppi, Long perusteId, Long oppiaineId, Long kokonaisuusId) {
        assert tyyppi != null;

        this.tyyppi = tyyppi;
        this.perusteId = perusteId;
        this.oppiaineId = oppiaineId;
        this.kokonaisuusId = kokonaisuusId;
    }


    public static OppiaineLockContext of(OppiaineOpetuksenSisaltoTyyppi tyyppi, Long perusteId, Long oppiaineId, Long kokonaisuusId) {
        return new OppiaineLockContext(tyyppi, perusteId, oppiaineId, kokonaisuusId);
    }

    public void setOsanId(Long id) {
        oppiaineId = id;
    }
}
