package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.yl.AIPEOppiaine;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIPEOppiaineRepository extends JpaWithVersioningRepository<AIPEOppiaine, Long> {

}
