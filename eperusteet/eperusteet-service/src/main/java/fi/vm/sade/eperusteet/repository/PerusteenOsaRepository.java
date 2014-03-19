package fi.vm.sade.eperusteet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;

/**
 *
 * @author jhyoty
 */
@Repository
public interface PerusteenOsaRepository extends JpaRepository<PerusteenOsa, Long>, PerusteenOsaRepositoryCustom {
}
