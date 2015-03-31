package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jhyoty
 */
@Repository
public interface PerusteenOsaRepository extends JpaWithVersioningRepository<PerusteenOsa, Long> {
    @Query("SELECT p.tila from PerusteenOsa p WHERE p.id = ?1")
    PerusteTila getTila(Long id);
}
