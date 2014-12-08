package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Tiedote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author mikkom
 */
@Repository
public interface TiedoteRepository extends JpaRepository<Tiedote, Long> {
    @Override
    @Query("SELECT t FROM Tiedote t ORDER BY t.luotu DESC")
    List<Tiedote> findAll();
}
