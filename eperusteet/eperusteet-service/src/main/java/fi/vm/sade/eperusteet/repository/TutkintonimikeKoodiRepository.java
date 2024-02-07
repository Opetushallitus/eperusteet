package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.TutkintonimikeKoodi;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TutkintonimikeKoodiRepository extends JpaRepository<TutkintonimikeKoodi, Long> {
    List<TutkintonimikeKoodi> findByPerusteId(Long perusteId);

    @Query("SELECT DISTINCT peruste from TutkintonimikeKoodi t WHERE t.tutkintonimikeUri = ?1")
    Stream<Peruste> findAllByTutkintonimikeUri(String tutkintonimikeUri);
}
