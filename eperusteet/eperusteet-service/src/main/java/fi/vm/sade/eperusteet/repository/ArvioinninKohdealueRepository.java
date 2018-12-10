package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Tarvitaan ammattitaitovaatimuksien liittämisen apuna
 */
@Repository
public interface ArvioinninKohdealueRepository extends JpaWithVersioningRepository<ArvioinninKohdealue, Long> {
    @Query("SELECT COUNT(ak) FROM ArvioinninKohdealue ak WHERE ak.koodi != null")
    long koodillisetCount();
}
