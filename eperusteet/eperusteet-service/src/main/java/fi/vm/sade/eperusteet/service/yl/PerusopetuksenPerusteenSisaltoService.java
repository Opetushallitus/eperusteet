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

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import java.util.List;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author jhyoty
 */
public interface PerusopetuksenPerusteenSisaltoService extends OppiainePerusteenSisaltoService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<LaajaalainenOsaaminenDto> getLaajaalaisetOsaamiset(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<VuosiluokkaKokonaisuusDto> getVuosiluokkaKokonaisuudet(@P("perusteId") Long perusteId);

    //TBD: lukitus

}
