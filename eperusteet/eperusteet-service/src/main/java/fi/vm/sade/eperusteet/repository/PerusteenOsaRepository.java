package fi.vm.sade.eperusteet.repository;

import org.springframework.stereotype.Repository;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;

/**
 *
 * @author jhyoty
 */
@Repository
public interface PerusteenOsaRepository extends JpaWithVersioningRepository<PerusteenOsa, Long> {
}
