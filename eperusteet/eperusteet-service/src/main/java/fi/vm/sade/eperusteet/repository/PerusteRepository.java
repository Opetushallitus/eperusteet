package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import java.util.Date;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author jhyoty
 */
@Repository
public interface PerusteRepository extends JpaWithVersioningRepository<Peruste, Long>, PerusteRepositoryCustom {
    @Query("SELECT s.sisalto FROM Suoritustapa s, Peruste p LEFT JOIN p.suoritustavat s WHERE p.id = ?1 AND s.suoritustapakoodi = ?2")
    PerusteenOsaViite findSisaltoByIdAndSuoritustapakoodi(Long id, Suoritustapakoodi suoritustapakoodi);

    @Query("SELECT s FROM Suoritustapa s, Peruste p LEFT JOIN p.suoritustavat s WHERE p.id = ?1 AND s.suoritustapakoodi = ?2")
    Suoritustapa findSuoritustapaByIdAndSuoritustapakoodi(Long id, Suoritustapakoodi suoritustapakoodi);

    @Query("SELECT p FROM Suoritustapa s, Peruste p LEFT JOIN p.suoritustavat s WHERE p.id = ?1 AND s.suoritustapakoodi = ?2")
    Peruste findPerusteByIdAndSuoritustapakoodi(Long id, Suoritustapakoodi suoritustapakoodi);

    List<Peruste> findAllByKoulutustyyppi(String koulutustyyppi);

    @Query("SELECT p from Peruste p WHERE p.koulutustyyppi IS NOT NULL and p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS'")
    List<Peruste> findAllPerusteet();

    @Query("SELECT p from Peruste p " +
            "LEFT JOIN p.kielet k " +
            "WHERE p.paatospvm IS NOT NULL and p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS' and k IN (?1) ORDER BY p.paatospvm DESC")
    List<Peruste> findAllUusimmat(Set<Kieli> kielet, Pageable pageable);

    @Query("SELECT p from Peruste p WHERE p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS' and p.diaarinumero = ?1")
    List<Peruste> findAllByDiaarinumero(Diaarinumero diaarinumero);

    @Query("SELECT p from Peruste p WHERE p.tyyppi = 'AMOSAA_YHTEINEN' and p.tila = 'VALMIS'")
    List<Peruste> findAllAmosaaYhteisetPohjat();

    @Query("SELECT p from Peruste p WHERE p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS' and p.diaarinumero IN (?1)")
    Set<Peruste> findAllByDiaarinumerot(Set<Diaarinumero> diaarinumero);

//    @Query("SELECT DISTINCT p FROM Peruste p LEFT JOIN FETCH p.korvattavatDiaarinumerot diaari WHERE p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS' and ?1 = diaari")
    @Query("SELECT DISTINCT p FROM Peruste p LEFT JOIN FETCH p.korvattavatDiaarinumerot diaari WHERE p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS' and diaari.diaarinumero = ?1")
    Set<Peruste> findAllKorvaavatByDiaarinumero(String diaarinumero);

    Peruste findOneByDiaarinumeroAndTila(Diaarinumero diaarinumero, PerusteTila tila);

    List<Peruste> findByDiaarinumeroAndTila(Diaarinumero diaarinumero, PerusteTila tila);

    @Query("SELECT DISTINCT p from Peruste p LEFT JOIN p.osaamisalat o WHERE ?1 = o.uri")
    Stream<Peruste> findAllByOsaamisala(String osaamisalaUri);

    @Query("SELECT DISTINCT p.id FROM Peruste p " +
        "LEFT JOIN p.suoritustavat s " +
        "LEFT JOIN p.perusopetuksenPerusteenSisalto ps " +
        "LEFT JOIN p.lukiokoulutuksenPerusteenSisalto ls " +
        "LEFT JOIN p.esiopetuksenPerusteenSisalto eps " +
        "WHERE p.tila = ?2 AND (s.sisalto.id IN (?1) OR ps.sisalto.id IN (?1) OR eps.sisalto.id IN (?1) OR ls.id IN (?1) )")
    Set<Long> findBySisaltoRoots(Iterable<? extends Number> rootIds, PerusteTila tila);

    @Query("SELECT DISTINCT p.id FROM Peruste p JOIN p.suoritustavat s JOIN s.tutkinnonOsat to WHERE p.tila = ?2 AND to.tutkinnonOsa.id = ?1")
    Set<Long> findByTutkinnonosaId(Long id, PerusteTila tila);

    @Query("SELECT p.tila from Peruste p WHERE p.id = ?1")
    PerusteTila getTila(Long id);

    @Query("select new fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto(v.aikaleima) from PerusteVersion v " +
            "   where v.peruste.id = ?1")
    PerusteVersionDto getGlobalPerusteVersion(long perusteId);

    @Query("select v from PerusteVersion v where v.peruste.id = ?1")
    PerusteVersion getPerusteVersionEntityByPeruste(long perusteId);

    @Query("select p from Peruste p where p.tila = 'VALMIS' AND p.tyyppi = 'NORMAALI' AND p.koulutustyyppi IN ('koulutustyyppi_1', 'koulutustyyppi_11', 'koulutustyyppi_12', 'koulutustyyppi_5', 'koulutustyyppi_18')")
    List<Peruste> findAllAmosaa();

    @Query("SELECT p FROM Peruste p " +
            "WHERE p.perusteprojekti.tila = :projektitila " +
            "AND tyyppi = :perustetyyppi " +
            "AND koulutustyyppi IN :koulutustyypit")
    List<Peruste> findByTilaTyyppiKoulutustyyppi(
            @Param("projektitila") ProjektiTila projektitila, @Param("perustetyyppi") PerusteTyyppi perustetyyppi, @Param("koulutustyypit") List<String> koulutustyypit);

    @Query("SELECT p FROM Peruste p " +
            "WHERE p.perusteprojekti.tila = :projektitila " +
            "AND globalVersion.aikaleima >= :aikaleima " +
            "AND tyyppi = :perustetyyppi " +
            "AND koulutustyyppi IN (:koulutustyypit)")
    List<Peruste> findByTilaVersioaikaleimaTyyppiKoulutustyyppi(
            @Param("projektitila") ProjektiTila projektitila, @Param("aikaleima") Date aikaleima, @Param("perustetyyppi") PerusteTyyppi perustetyyppi, @Param("koulutustyypit") List<String> koulutustyypit);

}
