package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.vst.VapaasivistystyoSisalto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VapaasivistystyoSisaltoRepository extends JpaWithVersioningRepository<VapaasivistystyoSisalto, Long> {
}
