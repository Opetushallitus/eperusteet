package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Tiedote;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TiedoteRepository extends JpaRepository<Tiedote, Long> {

    @Query("SELECT t FROM Tiedote t WHERE (t.julkinen = ?1 OR t.julkinen = TRUE) AND t.muokattu >= ?2 AND (?3 = TRUE OR t.luotu >= ?4) ORDER BY t.muokattu DESC")
    List<Tiedote> findAll(boolean vainJulkiset, Date alkaen, boolean haeVanhentuneet, LocalDateTime vanhenemisraja);

    @Query("SELECT t FROM Tiedote t WHERE (t.julkinen = ?1 OR t.julkinen = TRUE) AND t.perusteprojekti = ?3 AND t.muokattu >= ?2 AND (?4 = TRUE OR t.luotu >= ?5) ORDER BY t.muokattu DESC")
    List<Tiedote> findAllByPerusteprojekti(boolean vainJulkiset, Date alkaen, Perusteprojekti perusteprojekti, boolean haeVanhentuneet, LocalDateTime vanhenemisraja);
}
