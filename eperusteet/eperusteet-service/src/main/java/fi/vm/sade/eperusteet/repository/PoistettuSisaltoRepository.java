package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.PoistettuSisalto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface PoistettuSisaltoRepository extends JpaWithVersioningRepository<PoistettuSisalto, Long> {

    List<PoistettuSisalto> findAllByPerusteId(Long perusteId);
}
