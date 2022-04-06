package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Maarays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaaraysRepository extends JpaRepository<Maarays, Long> {
}
