package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Peruste;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jhyoty
 */
@Repository
public interface PerusteRepository extends JpaRepository<Peruste, Long>, PerusteRepositoryCustom {
    Peruste findOneByKoodiUri(String koodiUri);
}
