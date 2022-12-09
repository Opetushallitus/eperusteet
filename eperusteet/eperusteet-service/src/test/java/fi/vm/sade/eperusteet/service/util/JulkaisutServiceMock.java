package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.JulkaisuPerusteTila;
import fi.vm.sade.eperusteet.domain.JulkaisuTila;
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
    public void teeJulkaisu(long projektiId, JulkaisuBaseDto julkaisuBaseDto) {
    }

    @Override
    public JulkaisuTila viimeisinJulkaisuTila(Long perusteId) {
        return null;
    }

    @Override
    public void teeJulkaisuAsync(long projektiId, JulkaisuBaseDto julkaisuBaseDto) {

    }

    @Override
    public JulkaisuBaseDto aktivoiJulkaisu(long projektiId, int revision) {
        return null;
    }

    @Override
    public Page<PerusteenJulkaisuData> getJulkisetJulkaisut(List<String> koulutustyyppi, String nimi, String kieli, String tyyppi, boolean tulevat, boolean voimassa, boolean siirtyma, boolean poistuneet, boolean koulutusvienti, String diaarinumero, String koodi, Integer sivu, Integer sivukoko) {
        return null;
    }

    @Override
    public Date viimeisinPerusteenJulkaisuaika(Long perusteId) {
        return null;
    }

    @Override
    public boolean onkoMuutoksia(long perusteId) {
        return false;
    }

    @Override
    public void kooditaValiaikaisetKoodit(Long perusteId) {

    }

    @Override
    public void nollaaJulkaisuTila(Long perusteId) {

    }

    @Override
    public void saveJulkaisuPerusteTila(JulkaisuPerusteTila julkaisuPerusteTila) {

    }

    @Override
    public int seuraavaVapaaJulkaisuNumero(long perusteId) {
        return 0;
    }
}
