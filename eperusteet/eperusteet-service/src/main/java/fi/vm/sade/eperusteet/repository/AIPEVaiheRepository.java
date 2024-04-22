package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.AIPEVaihe;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIPEVaiheRepository extends JpaWithVersioningRepository<AIPEVaihe, Long> {
}
