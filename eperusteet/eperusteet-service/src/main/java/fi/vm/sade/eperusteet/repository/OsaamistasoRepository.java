package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Osaamistaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OsaamistasoRepository extends JpaRepository<Osaamistaso, Long> {
}
