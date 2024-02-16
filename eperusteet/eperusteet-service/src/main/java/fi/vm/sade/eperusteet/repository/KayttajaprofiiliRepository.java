package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Kayttajaprofiili;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KayttajaprofiiliRepository extends JpaRepository<Kayttajaprofiili, Long> {

    @Query("SELECT k FROM Kayttajaprofiili k LEFT JOIN FETCH k.suosikit where k.oid = ?1")
    Kayttajaprofiili findOneEager(String oid);

    Kayttajaprofiili findOneByOid(String oid);
}
