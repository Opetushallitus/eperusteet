package fi.vm.sade.eperusteet.repository.liite;

import fi.vm.sade.eperusteet.domain.liite.Liite;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LiiteRepository extends JpaRepository<Liite, UUID>, LiiteRepositoryCustom {
    Liite findById(UUID id);

    @Query("SELECT l FROM Peruste o JOIN o.liitteet l WHERE o.id = ?1")
    List<Liite> findByPerusteId(Long perusteId);

    @Query("SELECT l FROM Peruste o JOIN o.liitteet l WHERE o.id = ?1 AND l.mime IN ?2")
    List<Liite> findByPerusteIdAndMimeIn(Long perusteId, Set<String> tyypit);

    @Query("SELECT l FROM Peruste o JOIN o.liitteet l WHERE o.id = ?1 AND l.id = ?2")
    Liite findOne(Long perusteId, UUID id);

    @Transactional
    @Query("UPDATE Liite l SET l.lisatieto = ?2 WHERE l.id = ?1")
    @Modifying
    void updateLisatieto(UUID id, String lisatieto);
}
