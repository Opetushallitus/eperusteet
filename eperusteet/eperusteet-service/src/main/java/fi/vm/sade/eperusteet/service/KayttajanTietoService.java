package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.concurrent.Future;

public interface KayttajanTietoService {

    @PreAuthorize("isAuthenticated()")
    KayttajanTietoDto haeKirjautaunutKayttaja();

    @PreAuthorize("isAuthenticated()")
    KayttajanTietoDto hae(String oid);

    @PreAuthorize("isAuthenticated()")
    List<KayttajanTietoDto> haeKayttajatiedot(List<String> oid);

    @PreAuthorize("isAuthenticated()")
    Future<KayttajanTietoDto> haeAsync(String oid);

    @PreAuthorize("isAuthenticated()")
    List<KayttajanProjektitiedotDto> haePerusteprojektit(String oid);

    @PreAuthorize("isAuthenticated()")
    KayttajanProjektitiedotDto haePerusteprojekti(String oid, Long projektiId);

    @PreAuthorize("hasPermission(#organisaatioOid, 'organisaatio', new String[]{'LUKU'})")
    JsonNode getOrganisaatioVirkailijat(String organisaatioOid);
}
