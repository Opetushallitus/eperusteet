package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import java.util.List;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PerusopetuksenPerusteenSisaltoService extends OppiainePerusteenSisaltoService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<LaajaalainenOsaaminenDto> getLaajaalaisetOsaamiset(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<VuosiluokkaKokonaisuusDto> getVuosiluokkaKokonaisuudet(@P("perusteId") Long perusteId);
}
