package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import java.util.Date;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class JulkaisutServiceMock implements JulkaisutService {
    @Override
    public List<JulkaisuBaseDto> getJulkaisut(long id) {
        return null;
    }

    @Override
    public JulkaisuBaseDto teeJulkaisu(long projektiId, JulkaisuBaseDto julkaisuBaseDto) {
        return null;
    }

    @Override
    public JulkaisuBaseDto aktivoiJulkaisu(long projektiId, int revision) {
        return null;
    }

    @Override
    public Page<PerusteenJulkaisuData> getJulkisetJulkaisut(List<String> koulutustyyppi, String nimi, String kieli, String tyyppi, boolean tulevat, boolean voimassa, boolean siirtyma, boolean poistuneet, boolean koulutusvienti, Integer sivu, Integer sivukoko) {
        return null;
    }

    @Override
    public Date viimeisinPerusteenJulkaisuaika(Long perusteId) {
        return null;
    }
}
