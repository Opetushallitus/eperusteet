package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.dto.julkinen.OpetussuunnitelmaEtusivuDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface YlopsClient {

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    JsonNode getTilastot();

    List<OpetussuunnitelmaEtusivuDto> getOpetussuunnitelmatEtusivu();
}
