package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;

public interface SuoritustapaRepository extends JpaWithVersioningRepository<Suoritustapa, Long> {

    @Query("SELECT s FROM Peruste p JOIN p.suoritustavat s WHERE p.id = ?1 AND s.suoritustapakoodi = ?2")
    Suoritustapa findByPerusteAndKoodi(Long perusteId, Suoritustapakoodi koodi);

}
