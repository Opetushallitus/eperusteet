package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OsaAlueRepository extends JpaWithVersioningRepository<OsaAlue, Long> {

}
