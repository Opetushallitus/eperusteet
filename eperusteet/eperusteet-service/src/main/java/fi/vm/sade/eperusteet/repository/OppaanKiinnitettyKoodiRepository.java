package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.OppaanKiinnitettyKoodi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OppaanKiinnitettyKoodiRepository extends JpaRepository<OppaanKiinnitettyKoodi, Long> {
}
