package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.dto.julkinen.OpetussuunnitelmaEtusivuDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface YlopsClient {

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    JsonNode getTilastot();

    List<OpetussuunnitelmaEtusivuDto> getOpetussuunnitelmatEtusivu();

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'TILANVAIHTO')")
    void arkistoiPeruste(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'TILANVAIHTO')")
    void poistaArkistointi(@P("perusteId") Long perusteId);
}
