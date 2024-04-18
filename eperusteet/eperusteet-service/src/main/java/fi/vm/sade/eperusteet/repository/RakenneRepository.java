package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RakenneRepository extends JpaWithVersioningRepository<RakenneModuuli, Long> {
    @Query("SELECT distinct s.rakenne.id FROM Suoritustapa s, Peruste p LEFT JOIN p.suoritustavat s WHERE p.id = ?1 AND s.suoritustapakoodi = ?2")
    Long getRakenneIdWithPerusteAndSuoritustapa(Long perusteId, Suoritustapakoodi suoritustapakoodi);
}
