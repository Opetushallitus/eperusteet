package fi.vm.sade.eperusteet.repository;

import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.KoulutustyyppiLukumaara;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JulkaisutRepository extends JpaRepository<JulkaistuPeruste, Long> {
    List<JulkaistuPeruste> findAllByPeruste(Peruste peruste);

    String julkaisutQuery = "FROM ( " +
            "   SELECT * " +
            "   FROM julkaistu_peruste_Data_view data" +
            "   WHERE koulutustyyppi IN (:koulutustyypit) " +
            "   AND (:nimi LIKE '' " +
            "           OR LOWER(nimi->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%')) " +
            "           OR EXISTS (SELECT 1 FROM json_array_elements(osaamisalanimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%'))) " +
            "           OR EXISTS (SELECT 1 FROM json_array_elements(tutkintonimikkeetnimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%'))) " +
            "           OR EXISTS (SELECT 1 FROM json_array_elements(tutkinnonosatnimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%'))) " +
            "       )" +
            "   AND CAST(kielet as text) LIKE LOWER(CONCAT('%',:kieli,'%')) " +
            "   AND (:koulutusvienti = false OR CAST(koulutusvienti as boolean) = true) " +
            "   AND ((:tulevat = true " +
            "                       AND CAST(data.\"voimassaoloAlkaa\" as bigint) > :nykyhetki) " +
            "       OR (:poistuneet = true AND CAST(data.\"voimassaoloLoppuu\" as bigint) < :nykyhetki " +
            "           AND CAST(COALESCE(data.\"siirtymaPaattyy\", 0) as bigint) < :nykyhetki)" +
            "       OR (:siirtymat = true " +
            "                       AND (data.\"voimassaoloLoppuu\" IS NOT NULL AND data.\"siirtymaPaattyy\" IS NOT NULL " +
            "                           AND CAST(data.\"voimassaoloLoppuu\" as bigint) < :nykyhetki AND CAST(data.\"siirtymaPaattyy\" as bigint) > :nykyhetki)) " +
            "       OR (:voimassa = true " +
            "                       AND (CAST(data.\"voimassaoloAlkaa\" as bigint) < :nykyhetki) " +
            "                           AND (data.\"voimassaoloLoppuu\" IS NULL OR CAST(data.\"voimassaoloLoppuu\" as bigint) > :nykyhetki))) " +
            "   order by nimi->>:kieli asc, ?#{#pageable} " +
            ") t";

    @Query(nativeQuery = true,
            value = "SELECT CAST(row_to_json(t) as text) " + julkaisutQuery,
            countQuery = "SELECT count(*) " + julkaisutQuery
    )
    Page<String> findAllJulkisetJulkaisut(
            @Param("koulutustyypit") List<String> koulutustyypit,
            @Param("nimi") String nimi,
            @Param("kieli") String kieli,
            @Param("nykyhetki") Long nykyhetki,
            @Param("tulevat") boolean tulevat,
            @Param("voimassa") boolean voimassa,
            @Param("siirtymat") boolean siirtymat,
            @Param("poistuneet") boolean poistuneet,
            @Param("koulutusvienti") boolean koulutusvienti,
            Pageable pageable);

    @Query(nativeQuery = true,
            value = "SELECT data.koulutustyyppi, COUNT(*) " +
                    "FROM julkaistu_peruste_Data_view data " +
                    "WHERE data.koulutustyyppi IS NOT NULL " +
                    "AND ((data.\"voimassaoloLoppuu\" IS NULL OR CAST(data.\"voimassaoloAlkaa\" as bigint) > :nykyhetki) " +
                    "   OR (data.\"siirtymaPaattyy\" IS NOT NULL AND CAST(data.\"siirtymaPaattyy\" as bigint) > :nykyhetki)) " +
                    "AND LOWER(CAST(kielet as text)) LIKE LOWER(CONCAT('%', :kieli,'%')) " +
                    "GROUP BY data.koulutustyyppi")
    List<KoulutustyyppiLukumaara> findJulkaistutKoulutustyyppiLukumaaratByKieli(
            @Param("kieli") String kieli,
            @Param("nykyhetki") Long nykyhetki
    );

    List<JulkaistuPeruste> findAllByPerusteId(Long id);

    List<JulkaistuPeruste> findAllByPerusteOrderByRevisionDesc(@Param("peruste") Peruste peruste);

    JulkaistuPeruste findFirstByPerusteOrderByRevisionDesc(@Param("peruste") Peruste peruste);

    JulkaistuPeruste findFirstByPerusteIdOrderByRevisionDesc(long id);

    long countByPeruste(Peruste peruste);

    JulkaistuPeruste findOneByPerusteAndLuotu(Peruste peruste, Date aikaleima);

    JulkaistuPeruste findFirstByPerusteAndRevisionOrderByIdDesc(Peruste peruste, int revision);

    @Query("SELECT p " +
            "FROM JulkaistuPeruste jp " +
            "    INNER JOIN jp.peruste p " +
            "WHERE p.tyyppi = 'NORMAALI' " +
            "   AND p.koulutustyyppi IN ('koulutustyyppi_1', 'koulutustyyppi_11', 'koulutustyyppi_12', " +
            "       'koulutustyyppi_5', 'koulutustyyppi_18', 'koulutustyyppi_10', 'koulutustyyppi_40') " +
            "   AND (p.voimassaoloLoppuu IS NULL " +
            "       OR p.voimassaoloLoppuu > NOW() " +
            "       OR (p.siirtymaPaattyy IS NOT NULL " +
            "           AND p.siirtymaPaattyy > NOW()))")
    Set<Peruste> findAmosaaJulkaisut();
}
