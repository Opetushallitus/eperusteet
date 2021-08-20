package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.GeneerinenArviointiasteikko;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneerinenArviointiasteikkoRepository extends JpaWithVersioningRepository<GeneerinenArviointiasteikko, Long> {

    List<GeneerinenArviointiasteikko> findByJulkaistuTrue();
}
