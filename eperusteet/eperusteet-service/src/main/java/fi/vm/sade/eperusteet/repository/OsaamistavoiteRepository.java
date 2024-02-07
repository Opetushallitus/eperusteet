package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.tutkinnonosa.Osaamistavoite;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OsaamistavoiteRepository extends JpaWithVersioningRepository<Osaamistavoite, Long> {

}
