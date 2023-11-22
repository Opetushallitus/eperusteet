package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.JulkaisuPerusteTila;
import fi.vm.sade.eperusteet.repository.JulkaisuPerusteTilaRepository;
import fi.vm.sade.eperusteet.service.JulkaisuPerusteTilaService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Profile("docker")
public class JulkaisuPerusteTilaServiceMock implements JulkaisuPerusteTilaService {

    @Autowired
    private JulkaisuPerusteTilaRepository julkaisuPerusteTilaRepository;

    @Override
    @IgnorePerusteUpdateCheck
    public void saveJulkaisuPerusteTila(JulkaisuPerusteTila julkaisuPerusteTila) {
        julkaisuPerusteTilaRepository.save(julkaisuPerusteTila);
    }
}
