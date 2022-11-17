package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.JulkaisuPerusteTila;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JulkaisuPerusteTilaRepository extends JpaRepository<JulkaisuPerusteTila, Long> {
}
