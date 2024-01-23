package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.repository.JulkaistuPerusteDataStoreRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class JulkaistuPerusteDataStoreRepositoryMock implements JulkaistuPerusteDataStoreRepository {

    @Override
    public void syncPeruste(Long perusteId) {

    }
}
