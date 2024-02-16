package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TekstiPalanenRepository extends JpaRepository<TekstiPalanen, Long>{
}
