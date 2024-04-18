package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.JulkaisuPerusteTila;
import fi.vm.sade.eperusteet.repository.JulkaisuPerusteTilaRepository;
import fi.vm.sade.eperusteet.service.JulkaisuPerusteTilaService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@Profile("!docker")
public class JulkaisuPerusteTilaServiceImpl implements JulkaisuPerusteTilaService {

    @Autowired
    private JulkaisuPerusteTilaRepository julkaisuPerusteTilaRepository;

    @Override
    @IgnorePerusteUpdateCheck
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveJulkaisuPerusteTila(JulkaisuPerusteTila julkaisuPerusteTila) {
        julkaisuPerusteTilaRepository.save(julkaisuPerusteTila);
    }
}
