package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysAsiasanatFetch;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiittyyTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTila;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTyyppi;
import fi.vm.sade.eperusteet.dto.Voimassaolo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaaraysRepository extends JpaRepository<Maarays, Long> {

    @Query("SELECT DISTINCT m, teksti.teksti as nimi, m.voimassaoloAlkaa " +
            "FROM Maarays m " +
            "LEFT JOIN m.nimi nimi " +
            "LEFT JOIN nimi.teksti teksti " +
            "LEFT JOIN m.koulutustyypit kt " +
            "LEFT JOIN m.asiasanat ast " +
            "LEFT JOIN ast.asiasana asiasana " +
            "WHERE 1 = 1 " +
            "AND teksti.kieli = :kieli " +
            "AND KEY(ast) = :kieli " +
            "AND (COALESCE(:koulutustyypit, null) is null OR kt in (:koulutustyypit)) " +
            "AND (:tyyppi is null OR m.tyyppi = :tyyppi) " +
            "AND (:nimi is null OR LOWER(teksti.teksti) LIKE LOWER(CONCAT('%',:nimi,'%')) OR LOWER(asiasana) LIKE LOWER(CONCAT('%',:nimi,'%'))) " +
            "AND (:tila is null OR m.tila = :tila) " +
            "AND (" +
            "       (:tuleva is false AND :voimassa is false AND :paattynyt is false) " +
            "       OR (:tuleva is true AND m.voimassaoloAlkaa > CURRENT_DATE)" +
            "       OR (:voimassa is true AND m.voimassaoloAlkaa <= CURRENT_DATE AND (m.voimassaoloLoppuu IS NULL OR m.voimassaoloLoppuu >= CURRENT_DATE))" +
            "       OR (:paattynyt is true AND m.voimassaoloLoppuu IS NOT NULL AND m.voimassaoloLoppuu < CURRENT_DATE)" +
            ")")
    Page<Object[]> haeMaaraykset(
            @Param("nimi") String nimi,
            @Param("kieli") Kieli kieli,
            @Param("tyyppi") MaaraysTyyppi tyyppi,
            @Param("koulutustyypit") List<String> koulutustyypit,
            @Param("tila") MaaraysTila tila,
            @Param("tuleva") boolean tuleva,
            @Param("voimassa") boolean voimassa,
            @Param("paattynyt") boolean paattynyt,
            Pageable pageable);

    @Query("SELECT DISTINCT k FROM Maarays m JOIN m.koulutustyypit k")
    List<String> findDistinctKoulutustyypit();

    Maarays findFirstByPerusteIdAndLiittyyTyyppiOrderByLuotuAsc(Long perusteId, MaaraysLiittyyTyyppi liittyyTyyppi);

}
