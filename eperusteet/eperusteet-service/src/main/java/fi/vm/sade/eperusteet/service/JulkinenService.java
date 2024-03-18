package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.julkinen.JulkiEtusivuDto;
import fi.vm.sade.eperusteet.dto.julkinen.TietoaPalvelustaDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface JulkinenService {

    Page<JulkiEtusivuDto> haeEtusivu(String nimi, String kieli, Integer sivu, Integer sivukoko);

    List<JulkiEtusivuDto> getJulkisivuDatat();

    TietoaPalvelustaDto getTietoaPalvelusta();
}
