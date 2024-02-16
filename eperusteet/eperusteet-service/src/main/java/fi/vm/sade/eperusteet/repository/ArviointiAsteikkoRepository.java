package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArviointiAsteikkoRepository extends JpaRepository<ArviointiAsteikko, Long> {

}
