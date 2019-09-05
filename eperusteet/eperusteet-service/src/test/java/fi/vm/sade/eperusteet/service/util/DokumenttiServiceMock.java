package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.GeneratorVersion;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.service.exception.DokumenttiException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class DokumenttiServiceMock implements DokumenttiService {
    @Override
    public void setStarted(DokumenttiDto dto) {

    }

    @Override
    public void generateWithDto(DokumenttiDto dto) throws DokumenttiException {

    }

    @Override
    public void generateWithDtoSynchronous(DokumenttiDto dto) throws DokumenttiException {

    }

    @Override
    public DokumenttiDto createDtoFor(long id, Kieli kieli, Suoritustapakoodi suoritustapakoodi, GeneratorVersion version) {
        return null;
    }

    @Override
    public byte[] get(Long id) {
        return new byte[0];
    }

    @Override
    public Long getDokumenttiId(Long perusteId, Kieli kieli, Suoritustapakoodi suoritustapakoodi, GeneratorVersion generatorVersion) {
        return null;
    }

    @Override
    public DokumenttiDto query(Long id) {
        return null;
    }

    @Override
    public DokumenttiDto findLatest(Long id, Kieli kieli, Suoritustapakoodi suoritustapakoodi) {
        return null;
    }

    @Override
    public DokumenttiDto findLatest(Long id, Kieli kieli, Suoritustapakoodi suoritustapakoodi, GeneratorVersion version) {
        return null;
    }

    @Override
    public void paivitaDokumentit() {

    }
}
