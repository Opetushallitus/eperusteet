package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.julkinen.OpetussuunnitelmaEtusivuDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface AmosaaClient {

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    List<Object> getTilastot();

    @PreAuthorize("hasPermission(null, 'arviointiasteikko', 'MUOKKAUS')")
    void updateArvioinnit();

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void paivitaAmosaaKoulutustoimijat();

    List<OpetussuunnitelmaEtusivuDto> getOpetussuunnitelmatEtusivu();
}
