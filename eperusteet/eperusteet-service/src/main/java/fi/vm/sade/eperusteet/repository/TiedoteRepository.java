package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Tiedote;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author mikkom
 */
@Repository
public interface TiedoteRepository extends JpaRepository<Tiedote, Long> {
    @Query("SELECT t FROM Tiedote t WHERE (t.julkinen = ?1 OR t.julkinen = TRUE) AND t.muokattu >= ?2 " +
            "ORDER BY t.muokattu DESC")
    List<Tiedote> findAll(boolean vainJulkiset, Date alkaen);

    @Query("SELECT t FROM Tiedote t WHERE (t.julkinen = ?1 OR t.julkinen = TRUE) AND (t.perusteprojekti = ?3) AND t.muokattu >= ?2 " +
            "ORDER BY t.muokattu DESC")
    List<Tiedote> findAllByPerusteprojekti(boolean vainJulkiset, Date alkaen, Perusteprojekti perusteprojekti);
}
