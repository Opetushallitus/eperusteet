package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Kommentti;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KommenttiRepository extends JpaRepository<Kommentti, Long> {
    @Query("SELECT k FROM Kommentti k WHERE k.ylinId = ?1")
    List<Kommentti> findAllByYlin(Long ylinId);

    @Query("SELECT k FROM Kommentti k WHERE k.parentId = ?1")
    List<Kommentti> findAllByParent(Long parentId);

    @Query("SELECT k FROM Kommentti k WHERE k.perusteprojektiId = ?1")
    List<Kommentti> findAllByPerusteprojekti(Long perusteprojektiId);

    @Query("SELECT k FROM Kommentti k WHERE k.perusteprojektiId = ?1 AND k.perusteenOsaId = ?2")
    List<Kommentti> findAllByPerusteenOsa(Long perusteprojektiId, Long PerusteenOsaId);

    @Query("SELECT k FROM Kommentti k WHERE k.perusteenOsaId = ?1")
    List<Kommentti> findAllByPerusteenOsa(Long PerusteenOsaId);

    @Query("SELECT k FROM Kommentti k WHERE k.perusteprojektiId = ?1 AND k.suoritustapa = ?2")
    List<Kommentti> findAllBySuoritustapa(Long perusteprojektiId, String suoritustapa);
}
