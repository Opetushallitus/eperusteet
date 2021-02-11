package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepository;
import java.util.Date;
import org.springframework.data.domain.Page;
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

    @Query("SELECT p FROM Peruste p " +
            "LEFT JOIN p.suoritustavat s " +
            "LEFT JOIN p.perusopetuksenPerusteenSisalto ps " +
            "LEFT JOIN p.lukiokoulutuksenPerusteenSisalto ls " +
            "LEFT JOIN p.esiopetuksenPerusteenSisalto eps " +
            "LEFT JOIN p.oppaanSisalto opp " +
            "WHERE p.tila = ?2 AND (s.sisalto.id IN (?1) OR ps.sisalto.id IN (?1) OR eps.sisalto.id IN (?1) OR ls.id IN (?1) OR opp.sisalto.id IN (?1))")
    Set<Peruste> findPerusteetBySisaltoRoots(Iterable<? extends Number> rootIds, PerusteTila tila);

    @Query("SELECT DISTINCT p.id FROM Peruste p JOIN p.suoritustavat s JOIN s.tutkinnonOsat to WHERE p.tila = ?2 AND to.tutkinnonOsa.id = ?1")
    Set<Long> findByTutkinnonosaId(Long id, PerusteTila tila);

    @Query("SELECT p.tila from Peruste p WHERE p.id = ?1")
    PerusteTila getTila(Long id);

    @Query("select new fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto(v.aikaleima) from PerusteVersion v " +
            "   where v.peruste.id = ?1")
    PerusteVersionDto getGlobalPerusteVersion(long perusteId);

    @Query("select v from PerusteVersion v where v.peruste.id = ?1")
    PerusteVersion getPerusteVersionEntityByPeruste(long perusteId);

    @Query("SELECT p " +
            "FROM Peruste p " +
            "WHERE p.tila = 'VALMIS' AND p.tyyppi = 'NORMAALI' " +
            "   AND p.koulutustyyppi IN ('koulutustyyppi_1', 'koulutustyyppi_11', 'koulutustyyppi_12', 'koulutustyyppi_5', 'koulutustyyppi_18', 'koulutustyyppi_30')" +
            "   AND (p.voimassaoloLoppuu IS NULL " +
            "       OR p.voimassaoloLoppuu > NOW() " +
            "       OR (p.siirtymaPaattyy IS NOT NULL " +
            "           AND p.siirtymaPaattyy > NOW()))")
    List<Peruste> findAllAmosaa();

    @Query("SELECT DISTINCT p FROM Peruste p " +
            "JOIN FETCH p.suoritustavat s " +
            "JOIN FETCH s.tutkinnonOsat t " +
            "WHERE p.perusteprojekti.tila = :projektitila " +
            "AND p.globalVersion.aikaleima >= :aikaleima " +
            "AND p.tyyppi = :perustetyyppi " +
            "AND p.koulutustyyppi IN (:koulutustyypit) " +
            "AND s.suoritustapakoodi = :suoritustapakoodi " +
            "AND t.tutkinnonOsa.ammattitaitovaatimukset2019 IS NOT NULL")
    List<Peruste> findAmmattitaitovaatimusPerusteelliset(
            @Param("projektitila") ProjektiTila projektitila, @Param("aikaleima") Date aikaleima, @Param("perustetyyppi") PerusteTyyppi perustetyyppi,
            @Param("koulutustyypit") List<String> koulutustyypit, @Param("suoritustapakoodi") Suoritustapakoodi suoritustapakoodi);

    @Query("SELECT DISTINCT p FROM Peruste p " +
            "JOIN FETCH p.suoritustavat s " +
            "JOIN FETCH s.tutkinnonOsat t " +
            "WHERE p.perusteprojekti.tila = :projektitila " +
            "AND p.globalVersion.aikaleima >= :aikaleima " +
            "AND p.tyyppi = :perustetyyppi " +
            "AND p.koulutustyyppi IN (:koulutustyypit)")
    List<Peruste> findByTilaAikaTyyppiKoulutustyyppi(
            @Param("projektitila") ProjektiTila projektitila, @Param("aikaleima") Date aikaleima, @Param("perustetyyppi") PerusteTyyppi perustetyyppi,
            @Param("koulutustyypit") List<String> koulutustyypit);

    Peruste findByPerusteprojektiId(Long id);

    @Query("SELECT DISTINCT p.oppaanPerusteet FROM Peruste p JOIN p.oppaanPerusteet op WHERE size(p.oppaanPerusteet) > 0 AND op.tila = 'VALMIS'")
    List<Peruste> findOppaidenPerusteet();

    List<Peruste> findByTilaAndTyyppiAndKoulutusvienti(PerusteTila tila, PerusteTyyppi tyyppi, boolean koulutusvienti);

    @Query("SELECT DISTINCT p FROM Peruste p " +
            "JOIN p.perusteenAikataulut aikataulu " +
            "WHERE aikataulu.julkinen = true " +
            "AND p.koulutustyyppi IN(:koulutustyypit) " +
            "AND p.tila = 'LUONNOS' AND (SELECT COUNT(julkaisu) FROM JulkaistuPeruste julkaisu WHERE julkaisu.peruste.id = p.id) = 0)")
    Page<Peruste> findAllJulkaisuaikataulullisetPerusteet(@Param("koulutustyypit") List<String> koulutustyypit, Pageable pageable);
}
