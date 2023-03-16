package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.JulkaisuPerusteTila;
import fi.vm.sade.eperusteet.domain.JulkaisuTila;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.tika.mime.MimeTypeException;
import org.springframework.data.domain.Page;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.HttpMediaTypeNotSupportedException;

public interface JulkaisutService {
    @PreAuthorize("permitAll()")
    List<JulkaisuBaseDto> getJulkaisut(long id);

    @PreAuthorize("permitAll()")
    List<JulkaisuBaseDto> getJulkisetJulkaisut(long id);

    @PreAuthorize("hasPermission(#projektiId, 'perusteprojekti', 'TILANVAIHTO')")
    void teeJulkaisu(@P("projektiId") long projektiId, JulkaisuBaseDto julkaisuBaseDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    JulkaisuTila viimeisinJulkaisuTila(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#projektiId, 'perusteprojekti', 'TILANVAIHTO')")
    void teeJulkaisuAsync(@P("projektiId") long projektiId, JulkaisuBaseDto julkaisuBaseDto);

    @PreAuthorize("hasPermission(#projektiId, 'perusteprojekti', 'TILANVAIHTO')")
    JulkaisuBaseDto aktivoiJulkaisu(@P("projektiId") long projektiId, int revision) throws HttpMediaTypeNotSupportedException, MimeTypeException, IOException;

    @PreAuthorize("permitAll()")
    Page<PerusteenJulkaisuData> getJulkisetJulkaisut(
            List<String> koulutustyyppi, String nimi, String kieli, String tyyppi, boolean tulevat,
            boolean voimassa, boolean siirtyma, boolean poistuneet, boolean koulutusvienti, String diaarinumero,
            String koodi, Integer sivu, Integer sivukoko);

    @PreAuthorize("permitAll()")
    Date viimeisinPerusteenJulkaisuaika(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    boolean onkoMuutoksia(long perusteId);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void kooditaValiaikaisetKoodit(Long perusteId);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void nollaaJulkaisuTila(Long perusteId);

    @PreAuthorize("isAuthenticated()")
    void saveJulkaisuPerusteTila(JulkaisuPerusteTila julkaisuPerusteTila);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    int seuraavaVapaaJulkaisuNumero(long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    void updateJulkaisu(Long perusteId, JulkaisuBaseDto julkaisuBaseDto) throws HttpMediaTypeNotSupportedException, MimeTypeException, IOException;
}
