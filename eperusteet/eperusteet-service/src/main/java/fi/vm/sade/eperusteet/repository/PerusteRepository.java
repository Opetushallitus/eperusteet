package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.KoulutustyyppiLukumaara;
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

@Repository
public interface PerusteRepository extends JpaWithVersioningRepository<Peruste, Long>, PerusteRepositoryCustom {

    @Query("SELECT s FROM Peruste p LEFT JOIN p.suoritustavat s WHERE p.id = ?1 AND s.suoritustapakoodi = ?2")
    Suoritustapa findSuoritustapaByIdAndSuoritustapakoodi(Long id, Suoritustapakoodi suoritustapakoodi);

    List<Peruste> findAllByKoulutustyyppi(String koulutustyyppi);

    @Query("SELECT p from Peruste p WHERE p.koulutustyyppi IS NOT NULL and p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS'")
    List<Peruste> findAllPerusteet();

    @Query("SELECT p from Peruste p " +
            "LEFT JOIN p.kielet k " +
            "WHERE p.paatospvm IS NOT NULL and p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS' and k IN (?1) ORDER BY p.paatospvm DESC")
    List<Peruste> findAllUusimmat(Set<Kieli> kielet, Pageable pageable);

    @Query("SELECT p from Peruste p WHERE p.tyyppi = 'NORMAALI' and p.diaarinumero = ?1")
    List<Peruste> findAllByDiaarinumero(Diaarinumero diaarinumero);

    @Query("SELECT p from Peruste p WHERE p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS' and p.diaarinumero = ?1")
    List<Peruste> findAllValmiitByDiaarinumero(Diaarinumero diaarinumero);

    @Query("SELECT p from Peruste p " +
            "WHERE p.tyyppi = 'AMOSAA_YHTEINEN' " +
            "and (p.tila = 'VALMIS') ")
    List<Peruste> findAllAmosaaYhteisetPohjat();

    @Query("SELECT p from Peruste p WHERE p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS' and p.diaarinumero IN (?1)")
    Set<Peruste> findAllByDiaarinumerot(Set<Diaarinumero> diaarinumero);

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
            "   AND p.koulutustyyppi IN ('koulutustyyppi_1', 'koulutustyyppi_11', 'koulutustyyppi_12', 'koulutustyyppi_5', 'koulutustyyppi_18', 'koulutustyyppi_10')" +
            "   AND (p.voimassaoloLoppuu IS NULL " +
            "       OR p.voimassaoloLoppuu > CURRENT_TIMESTAMP " +
            "       OR (p.siirtymaPaattyy IS NOT NULL " +
            "           AND p.siirtymaPaattyy > CURRENT_TIMESTAMP))")
    List<Peruste> findAllAmosaa();

    @Query("SELECT DISTINCT p FROM Peruste p " +
            "JOIN FETCH p.suoritustavat s " +
            "JOIN FETCH s.tutkinnonOsat t " +
            "WHERE p.perusteprojekti.tila = :projektitila " +
            "AND p.tyyppi = :perustetyyppi " +
            "AND p.koulutustyyppi IN (:koulutustyypit) " +
            "AND s.suoritustapakoodi = :suoritustapakoodi " +
            "AND t.tutkinnonOsa.ammattitaitovaatimukset2019 IS NOT NULL")
    List<Peruste> findAmmattitaitovaatimusPerusteelliset(
            @Param("projektitila") ProjektiTila projektitila, @Param("perustetyyppi") PerusteTyyppi perustetyyppi,
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

    @Query("SELECT distinct p FROM Peruste p " +
            "LEFT JOIN p.julkaisut j " +
            "WHERE p.tyyppi = :tyyppi " +
            "and (p.tila = 'VALMIS' OR j.id IS NOT NULL) " +
            "AND koulutusvienti = :koulutusvienti ")
    List<Peruste> findValmiitByTyyppi(@Param("tyyppi") PerusteTyyppi tyyppi, @Param("koulutusvienti") boolean koulutusvienti);


    @Query("SELECT distinct p FROM Peruste p " +
            "JOIN p.julkaisut j " +
            "WHERE p.tyyppi = :tyyppi " +
            "AND j.id IS NOT NULL " +
            "AND tila != 'POISTETTU' " +
            "AND (p.voimassaoloAlkaa IS NULL OR p.voimassaoloAlkaa < CURRENT_TIMESTAMP) " +
            "AND ((p.voimassaoloLoppuu IS NULL OR p.voimassaoloLoppuu > CURRENT_TIMESTAMP) OR (p.siirtymaPaattyy IS NOT NULL AND p.siirtymaPaattyy > CURRENT_TIMESTAMP))")
    List<Peruste> findJulkaistutVoimassaolevatPerusteetByTyyppi(@Param("tyyppi") PerusteTyyppi tyyppi);

    @Query("SELECT distinct p FROM Peruste p " +
            "LEFT JOIN p.julkaisut j " +
            "WHERE p.koulutustyyppi IS NOT NULL " +
            "and p.tyyppi = 'NORMAALI' " +
            "AND tila != 'POISTETTU' " +
            "and (p.tila = 'VALMIS' OR j.id IS NOT NULL) ")
    List<Peruste> findJulkaistutPerusteet();

    @Query("SELECT distinct p " +
            "FROM Peruste p " +
            "JOIN p.julkaisut j " +
            "WHERE p.koulutustyyppi IN(:koulutustyypit) " +
            "and p.tyyppi = 'NORMAALI' " +
            "AND tila != 'POISTETTU' " )
    List<Peruste> findJulkaistutPerusteet(@Param("koulutustyypit") List<String> koulutustyypit);

    @Query("SELECT distinct p FROM Peruste p " +
            "LEFT JOIN p.julkaisut j " +
            "WHERE ((:koulutustyyppi IS NULL AND p.koulutustyyppi IS NOT NULL) OR p.koulutustyyppi = :koulutustyyppi) " +
            "and p.tyyppi = :tyyppi " +
            "AND tila != 'POISTETTU' " +
            "and (p.tila = 'VALMIS' OR j.id IS NOT NULL) ")
    List<Peruste> findJulkaistutPerusteet(@Param("tyyppi") PerusteTyyppi tyyppi, @Param("koulutustyyppi") String koulutustyyppi);

    @Query("SELECT DISTINCT p FROM Peruste p " +
            "JOIN p.perusteenAikataulut aikataulu " +
            "WHERE aikataulu.julkinen = true " +
            "AND p.koulutustyyppi IN(:koulutustyypit) " +
            "AND p.tila = 'LUONNOS' AND (SELECT COUNT(julkaisu) FROM JulkaistuPeruste julkaisu WHERE julkaisu.peruste.id = p.id) = 0")
    Page<Peruste> findAllJulkaisuaikataulullisetPerusteet(@Param("koulutustyypit") List<String> koulutustyypit, Pageable pageable);

    @Query("SELECT p.koulutustyyppi, COUNT(p) AS lukumaara FROM Peruste p " +
            "LEFT JOIN p.julkaisut j " +
            "WHERE p.koulutustyyppi IS NOT NULL " +
            "AND p.koulutustyyppi IN(:koulutustyypit) " +
            "AND p.tyyppi = 'NORMAALI' " +
            "AND (p.tila = 'VALMIS' OR j.id IS NOT NULL) " +
            "AND (p.voimassaoloAlkaa IS NULL OR p.voimassaoloAlkaa < CURRENT_TIMESTAMP) " +
            "AND ((p.voimassaoloLoppuu IS NULL OR p.voimassaoloLoppuu > CURRENT_TIMESTAMP) OR (p.siirtymaPaattyy IS NOT NULL AND p.siirtymaPaattyy > CURRENT_TIMESTAMP)) " +
            "GROUP BY p.koulutustyyppi")
    List<KoulutustyyppiLukumaara> findVoimassaolevatJulkaistutPerusteLukumaarat(@Param("koulutustyypit") List<String> koulutustyypit);

    @Query("SELECT DISTINCT p.koulutustyyppi " +
            "FROM Peruste p " +
            "LEFT JOIN p.kielet k " +
            "LEFT JOIN p.julkaisut j " +
            "WHERE p.koulutustyyppi IS NOT NULL " +
            "AND p.tyyppi = 'NORMAALI' " +
            "AND (p.tila = 'VALMIS' OR j.id IS NOT NULL) " +
            "AND ((p.voimassaoloLoppuu IS NULL OR p.voimassaoloLoppuu > CURRENT_TIMESTAMP) OR (p.siirtymaPaattyy IS NOT NULL AND p.siirtymaPaattyy > CURRENT_TIMESTAMP)) " +
            "AND k = :kieli")
    List<String> findJulkaistutDistinctKoulutustyyppiByKieli(@Param("kieli") Kieli kieli);

    @Query("SELECT distinct p " +
            "FROM Peruste p " +
            "JOIN p.oppaanSisalto os " +
            "JOIN os.oppaanKiinnitetytKoodit okk " +
            "JOIN okk.koodi k " +
            "LEFT JOIN p.julkaisut j " +
            "WHERE p.tyyppi = 'OPAS' " +
            "and (p.tila = 'VALMIS' OR j.id IS NOT NULL) " +
            "AND (p.voimassaoloAlkaa IS NULL OR p.voimassaoloAlkaa < CURRENT_TIMESTAMP) " +
            "AND ((p.voimassaoloLoppuu IS NULL OR p.voimassaoloLoppuu > CURRENT_TIMESTAMP) OR (p.siirtymaPaattyy IS NOT NULL AND p.siirtymaPaattyy > CURRENT_TIMESTAMP))" +
            "AND k.uri = :koodiUri")
    List<Peruste> findAllByJulkaisutOppaatKiinnitettyKoodilla(@Param("koodiUri") String koodiUri);

    @Query("SELECT p " +
            "FROM Peruste p " +
            "WHERE p.tila != 'POISTETTU' " +
            "AND p.tyyppi = 'NORMAALI' " +
            "AND NOT EXISTS (SELECT m FROM Maarays m WHERE m.peruste = p)")
    List<Peruste> findAllByEiMaaraystaEiPoistettu();

    List<Peruste> findByOpasTyyppi(OpasTyyppi opasTyyppi);
}
