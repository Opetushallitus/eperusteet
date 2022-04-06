package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.vm.sade.eperusteet.domain.Palaute;
import fi.vm.sade.eperusteet.dto.PalauteDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PalauteService {

    @PreAuthorize("permitAll()")
    PalauteDto lahetaPalaute(PalauteDto palaute) throws JsonProcessingException;

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    List<Object> getPalautteet(String palautekanava);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    List<PalauteDto> getPalauteStatus(String palautekanava);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    PalauteDto paivitaPalaute(PalauteDto palaute);
}
