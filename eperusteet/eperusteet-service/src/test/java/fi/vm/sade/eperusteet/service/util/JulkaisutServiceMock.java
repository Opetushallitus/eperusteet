package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.JulkaisuPerusteTila;
import fi.vm.sade.eperusteet.domain.JulkaisuTila;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.JulkaisuSisaltoTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import fi.vm.sade.eperusteet.service.JulkaisutService;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class JulkaisutServiceMock implements JulkaisutService {

    @Override
    public List<JulkaisuBaseDto> getJulkaisutJaViimeisinStatus(long id) {
        return null;
    }

    @Override
    public List<JulkaisuBaseDto> getJulkaisut(long id) {
        return null;
    }

    @Override
    public List<JulkaisuBaseDto> getJulkisetJulkaisut(long id) {
        return null;
    }

    @Override
    public CompletableFuture<Void> teeJulkaisu(long projektiId, JulkaisuBaseDto julkaisuBaseDto) {
        return null;
    }

    @Override
    public void paivitaMaarayskokoelmaanPerusteenTiedot(long perusteId) {
        
    }

    @Override
    public JulkaisuTila viimeisinJulkaisuTila(Long perusteId) {
        return null;
    }

    @Override
    public CompletableFuture<Void> teeJulkaisuAsync(long projektiId, JulkaisuBaseDto julkaisuBaseDto) {

        return null;
    }

    @Override
    public Set<Long> generoiJulkaisuPdf(PerusteKaikkiDto peruste) {
        return null;
    }

    @Override
    public JulkaisuBaseDto aktivoiJulkaisu(long projektiId, int revision) {
        return null;
    }

    @Override
    public Page<PerusteenJulkaisuData> getJulkisetJulkaisut(List<String> koulutustyyppi, String nimi, String nimiTaiKoodi, String kieli, String tyyppi, boolean tulevat, boolean voimassa, boolean siirtyma, boolean poistuneet, String diaarinumero, String koodi, JulkaisuSisaltoTyyppi julkaisuSisaltoTyyppi, Integer sivu, Integer sivukoko) {
        return null;
    }

    @Override
    public Date viimeisinPerusteenJulkaisuaika(Long perusteId) {
        return null;
    }

    @Override
    public boolean julkaisemattomiaMuutoksia(long perusteId) { return false; }

    @Override
    public void kooditaValiaikaisetKoodit(Long perusteId) {

    }

    @Override
    public void nollaaJulkaisuTila(Long perusteId) {

    }

    @Override
    public int seuraavaVapaaJulkaisuNumero(long perusteId) {
        return 0;
    }

    @Override
    public void updateJulkaisu(Long perusteId, JulkaisuBaseDto julkaisuBaseDto) {

    }

    @Override
    public List<PerusteenJulkaisuData> getKaikkiPerusteet() {
        return null;
    }
}
