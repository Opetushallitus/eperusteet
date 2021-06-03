package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.OpasSisalto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpasSisaltoRepository extends JpaRepository<OpasSisalto, Long> {
}
