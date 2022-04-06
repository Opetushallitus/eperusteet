package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

public interface JulkaisutService {
    @PreAuthorize("permitAll()")
    List<JulkaisuBaseDto> getJulkaisut(long id);

    @PreAuthorize("hasPermission(#projektiId, 'perusteprojekti', 'TILANVAIHTO')")
    JulkaisuBaseDto teeJulkaisu(@P("projektiId") long projektiId, JulkaisuBaseDto julkaisuBaseDto);

    @PreAuthorize("hasPermission(#projektiId, 'perusteprojekti', 'TILANVAIHTO')")
    JulkaisuBaseDto aktivoiJulkaisu(@P("projektiId") long projektiId, int revision);

    @PreAuthorize("permitAll()")
    Page<PerusteenJulkaisuData> getJulkisetJulkaisut(
            List<String> koulutustyyppi, String nimi, String kieli, boolean tulevat,
            boolean voimassa, boolean siirtyma, boolean poistuneet, boolean koulutusvienti,
            Integer sivu, Integer sivukoko);

}
