package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.GeneerinenArviointiasteikko;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneerinenArviointiasteikkoRepository extends JpaWithVersioningRepository<GeneerinenArviointiasteikko, Long> {
}
