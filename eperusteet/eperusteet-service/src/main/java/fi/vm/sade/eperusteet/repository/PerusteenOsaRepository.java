package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jhyoty
 */
@Repository
public interface PerusteenOsaRepository extends JpaRepository<PerusteenOsa, Long> {
   
}
