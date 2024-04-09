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

    String julkaisutQuery =
            "FROM (SELECT * " +
            "  FROM (" +
            "   SELECT ROW_NUMBER() OVER(partition by id) as rownumber, * " +
            "   FROM julkaistu_peruste_data_store data" +
            "   WHERE (" +
            "           COALESCE(:koulutustyypit, NULL) = '' " +
            "           OR koulutustyyppi IN (:koulutustyypit) " +
            "           OR exists (select 1 from jsonb_array_elements(oppaankoulutustyypit) okt where okt->>0 in (:koulutustyypit))" +
            "         ) " +
            "   AND (:nimiTaiKoodi LIKE '' " +
            "           OR LOWER(nimi->>:kieli) LIKE LOWER(CONCAT('%',:nimiTaiKoodi,'%')) " +
            "           OR EXISTS (SELECT 1 FROM json_array_elements(osaamisalanimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimiTaiKoodi,'%'))) " +
            "           OR EXISTS (SELECT 1 FROM json_array_elements(tutkintonimikkeetnimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimiTaiKoodi,'%'))) " +
            "           OR EXISTS (SELECT 1 FROM json_array_elements(tutkinnonosatnimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimiTaiKoodi,'%'))) " +
            "           OR EXISTS (SELECT 1 FROM jsonb_array_elements(koodit) kd where kd->>0 LIKE CONCAT('%_',:nimiTaiKoodi,'%')) " +
            "       )" +
            "   AND (:nimi LIKE '' " +
            "           OR LOWER(nimi->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%')) " +
            "           OR EXISTS (SELECT 1 FROM json_array_elements(osaamisalanimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%'))) " +
            "           OR EXISTS (SELECT 1 FROM json_array_elements(tutkintonimikkeetnimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%'))) " +
            "           OR EXISTS (SELECT 1 FROM json_array_elements(tutkinnonosatnimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%'))) " +
            "       )" +
            "   AND CAST(kielet as text) LIKE LOWER(CONCAT('%',:kieli,'%')) " +
            "   AND :koulutusvienti = CAST(koulutusvienti as boolean) " +
            "   AND tyyppi = :tyyppi " +
            "   AND (:diaarinumero like '' OR LOWER(diaarinumero) LIKE LOWER(:diaarinumero)) " +
            "   AND (:koodi like '' OR exists (select 1 from jsonb_array_elements(koodit) kd where kd->>0 in (:koodi))) " +
            "   AND (" +
            "           (:tulevat = false AND :poistuneet = false AND :siirtymat = false AND :voimassa = false) " +
            "           OR (" +
            "               ((:tulevat = true AND CAST(data.\"voimassaoloAlkaa\" as bigint) > :nykyhetki) " +
            "               OR (:poistuneet = true AND CAST(data.\"voimassaoloLoppuu\" as bigint) < :nykyhetki AND COALESCE(CAST(data.\"siirtymaPaattyy\" as bigint), 0) < :nykyhetki)" +
            "               OR (:siirtymat = true " +
            "                       AND (data.\"voimassaoloLoppuu\" IS NOT NULL AND data.\"siirtymaPaattyy\" IS NOT NULL " +
            "                       AND CAST(data.\"voimassaoloLoppuu\" as bigint) < :nykyhetki AND CAST(data.\"siirtymaPaattyy\" as bigint) > :nykyhetki)) " +
            "               OR (:voimassa = true " +
            "                       AND (CAST(data.\"voimassaoloAlkaa\" as bigint) < :nykyhetki) " +
            "                       AND (data.\"voimassaoloLoppuu\" IS NULL OR CAST(data.\"voimassaoloLoppuu\" as bigint) > :nykyhetki))) " +
            "              )" +
            "       )" +
            "   AND (:sisaltotyyppi = 'kaikki' OR sisaltotyyppi = :sisaltotyyppi)" +
            "  ) subquery " +
            "  WHERE subquery.rownumber = 1 " +
            "   order by nimi->>:kieli asc " +
            ") t";

    @Query(nativeQuery = true,
            value = "SELECT CAST(row_to_json(t) as text) " + julkaisutQuery,
            countQuery = "SELECT count(*) " + julkaisutQuery
    )
    Page<String> findAllJulkisetJulkaisut(
            @Param("koulutustyypit") List<String> koulutustyypit,
            @Param("nimi") String nimi,
            @Param("nimiTaiKoodi") String nimiTaiKoodi,
            @Param("kieli") String kieli,
            @Param("nykyhetki") Long nykyhetki,
            @Param("tulevat") boolean tulevat,
            @Param("voimassa") boolean voimassa,
            @Param("siirtymat") boolean siirtymat,
            @Param("poistuneet") boolean poistuneet,
            @Param("koulutusvienti") boolean koulutusvienti,
            @Param("tyyppi") String tyyppi,
            @Param("diaarinumero") String diaarinumero,
            @Param("koodi") String koodi,
            @Param("sisaltotyyppi") String sisaltotyyppi,
            Pageable pageable);

    @Query(nativeQuery = true,
            value = "SELECT CAST(row_to_json(t) as text) " +
                    "FROM ( " +
                    "   SELECT * " +
                    "   FROM julkaistu_peruste_data_store data" +
                    "   WHERE exists (select 1 from jsonb_array_elements(koodit) kd where kd->>0 in (:koodit))" +
                    "   AND (" +
                    "           (:tulevat = false AND :poistuneet = false AND :siirtymat = false AND :voimassa = false) " +
                    "           OR (" +
                    "               ((:tulevat = true AND CAST(data.\"voimassaoloAlkaa\" as bigint) > :nykyhetki) " +
                    "               OR (:poistuneet = true AND CAST(data.\"voimassaoloLoppuu\" as bigint) < :nykyhetki AND COALESCE(CAST(data.\"siirtymaPaattyy\" as bigint), 0) < :nykyhetki)" +
                    "               OR (:siirtymat = true " +
                    "                       AND (data.\"voimassaoloLoppuu\" IS NOT NULL AND data.\"siirtymaPaattyy\" IS NOT NULL " +
                    "                       AND CAST(data.\"voimassaoloLoppuu\" as bigint) < :nykyhetki AND CAST(data.\"siirtymaPaattyy\" as bigint) > :nykyhetki)) " +
                    "               OR (:voimassa = true " +
                    "                       AND (CAST(data.\"voimassaoloAlkaa\" as bigint) < :nykyhetki) " +
                    "                       AND (data.\"voimassaoloLoppuu\" IS NULL OR CAST(data.\"voimassaoloLoppuu\" as bigint) > :nykyhetki))) " +
                    "              )" +
                    "       )" +
                    "   AND sisaltotyyppi = 'peruste' " +
                    ") t"
    )
    List<String> findAllJulkaistutPerusteetByKoodi(
            @Param("koodit") Set<String> koodit,
            @Param("nykyhetki") Long nykyhetki,
            @Param("tulevat") boolean tulevat,
            @Param("voimassa") boolean voimassa,
            @Param("siirtymat") boolean siirtymat,
            @Param("poistuneet") boolean poistuneet);

    @Query(nativeQuery = true,
            value = "SELECT CAST(row_to_json(t) as text) " +
                    "FROM ( " +
                    "   SELECT * " +
                    "   FROM julkaistu_peruste_data_store data" +
                    "   WHERE 1 = 1 " +
                    "   AND (" +
                    "           (:tulevat = false AND :poistuneet = false AND :siirtymat = false AND :voimassa = false) " +
                    "           OR (" +
                    "               ((:tulevat = true AND CAST(data.\"voimassaoloAlkaa\" as bigint) > :nykyhetki) " +
                    "               OR (:poistuneet = true AND CAST(data.\"voimassaoloLoppuu\" as bigint) < :nykyhetki AND COALESCE(CAST(data.\"siirtymaPaattyy\" as bigint), 0) < :nykyhetki)" +
                    "               OR (:siirtymat = true " +
                    "                       AND (data.\"voimassaoloLoppuu\" IS NOT NULL AND data.\"siirtymaPaattyy\" IS NOT NULL " +
                    "                       AND CAST(data.\"voimassaoloLoppuu\" as bigint) < :nykyhetki AND CAST(data.\"siirtymaPaattyy\" as bigint) > :nykyhetki)) " +
                    "               OR (:voimassa = true " +
                    "                       AND (CAST(data.\"voimassaoloAlkaa\" as bigint) < :nykyhetki) " +
                    "                       AND (data.\"voimassaoloLoppuu\" IS NULL OR CAST(data.\"voimassaoloLoppuu\" as bigint) > :nykyhetki))) " +
                    "              )" +
                    "       )" +
                    "   AND sisaltotyyppi = 'peruste' " +
                    ") t"
    )
    List<String> findAllJulkaistutPerusteetByVoimassaolo(
            @Param("nykyhetki") Long nykyhetki,
            @Param("tulevat") boolean tulevat,
            @Param("voimassa") boolean voimassa,
            @Param("siirtymat") boolean siirtymat,
            @Param("poistuneet") boolean poistuneet);


    @Query(nativeQuery = true,
            value = "SELECT data.koulutustyyppi, COUNT(*) " +
                    "FROM julkaistu_peruste_data_store data " +
                    "WHERE data.koulutustyyppi IS NOT NULL " +
                    "AND (data.\"voimassaoloAlkaa\" IS NULL OR CAST(data.\"voimassaoloAlkaa\" as bigint) < :nykyhetki) " +
                    "AND ((data.\"voimassaoloLoppuu\" IS NULL OR CAST(data.\"voimassaoloLoppuu\" as bigint) > :nykyhetki) " +
                    "   OR (data.\"siirtymaPaattyy\" IS NOT NULL AND CAST(data.\"siirtymaPaattyy\" as bigint) > :nykyhetki)) " +
                    "AND LOWER(CAST(kielet as text)) LIKE LOWER(CONCAT('%', :kieli,'%')) " +
                    "GROUP BY data.koulutustyyppi")
    List<KoulutustyyppiLukumaara> findJulkaistutKoulutustyyppiLukumaaratByKieli(
            @Param("kieli") String kieli,
            @Param("nykyhetki") Long nykyhetki
    );

    @Query(nativeQuery = true,
            value = "SELECT CAST(jsonb_path_query(jpd.data, CAST(:query AS jsonpath)) AS text) " +
                    "FROM julkaistu_peruste jp " +
                    "INNER JOIN julkaistu_peruste_data jpd ON jp.data_id = jpd.id " +
                    "WHERE jp.peruste_id = :perusteId " +
                    "AND luotu = (SELECT MAX(luotu) FROM julkaistu_peruste WHERE peruste_id = jp.peruste_id)")
    String findJulkaisutByJsonPath(@Param("perusteId") Long perusteId, @Param("query") String query);

    List<JulkaistuPeruste> findAllByPerusteId(Long id);

    List<JulkaistuPeruste> findAllByPerusteOrderByRevisionDesc(@Param("peruste") Peruste peruste);

    JulkaistuPeruste findFirstByPerusteAndJulkinenAndLuotuBeforeOrderByRevisionDesc(Peruste peruste, boolean julkinen, Date luotu);

    JulkaistuPeruste findFirstByPerusteOrderByRevisionDesc(@Param("peruste") Peruste peruste);

    JulkaistuPeruste findFirstByPerusteIdOrderByRevisionDesc(long id);

    long countByPeruste(Peruste peruste);

    long countByPerusteId(long perusteid);

    JulkaistuPeruste findOneByPerusteAndLuotu(Peruste peruste, Date aikaleima);

    JulkaistuPeruste findFirstByPerusteAndRevisionOrderByIdDesc(Peruste peruste, int revision);

    @Query("SELECT p " +
            "FROM JulkaistuPeruste jp " +
            "    INNER JOIN jp.peruste p " +
            "WHERE p.tyyppi = 'NORMAALI' " +
            "   AND p.tila != 'POISTETTU' " +
            "   AND p.koulutustyyppi IN ('koulutustyyppi_1', 'koulutustyyppi_11', 'koulutustyyppi_12', " +
            "       'koulutustyyppi_5', 'koulutustyyppi_18', " +
            "       'koulutustyyppi_10', 'koulutustyyppi_30', 'koulutustyyppi_40') " +
            "   AND (p.voimassaoloLoppuu IS NULL " +
            "       OR p.voimassaoloLoppuu > NOW() " +
            "       OR (p.siirtymaPaattyy IS NOT NULL " +
            "           AND p.siirtymaPaattyy > NOW()))")
    Set<Peruste> findAmosaaJulkaisut();
}
