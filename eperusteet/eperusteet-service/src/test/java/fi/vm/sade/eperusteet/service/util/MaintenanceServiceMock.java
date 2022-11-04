package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.dto.YllapitoDto;
import fi.vm.sade.eperusteet.service.MaintenanceService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("test")
public class MaintenanceServiceMock implements MaintenanceService {

    @Override
    public void addMissingOsaamisalakuvaukset() {
    }

    @Override
    public void runValidointi() {
    }

    @Override
    public void teeJulkaisut(boolean julkaiseKaikki, String tyyppi, String koulutustyyppi, String tiedote) {
    }

    @Override
    public void teeJulkaisu(long perusteId, String tiedote) {

    }

    @Override
    public List<YllapitoDto> getSallitutYllapidot() {
        return null;
    }

    @Override
    public void clearCache(String cache) {
    }
}
