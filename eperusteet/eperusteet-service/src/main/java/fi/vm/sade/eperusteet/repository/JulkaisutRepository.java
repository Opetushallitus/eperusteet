package fi.vm.sade.eperusteet.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.dto.KoulutustyyppiLukumaara;

@Repository
public interface JulkaisutRepository extends JpaRepository<JulkaistuPeruste, Long> {
    List<JulkaistuPeruste> findAllByPeruste(Peruste peruste);

    String julkisetPerusteetEhdot = """
            FROM julkaistu_peruste_data_store data
            WHERE (:sisaltotyyppi = 'kaikki' OR data.sisaltotyyppi = :sisaltotyyppi)
            AND data.tyyppi = :tyyppi
            AND (:nimiTaiKoodi LIKE ''
                    OR LOWER(data.nimi->>:kieli) LIKE LOWER(CONCAT('%',:nimiTaiKoodi,'%'))
                    OR EXISTS (SELECT 1 FROM jsonb_array_elements(data.osaamisalanimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimiTaiKoodi,'%')))
                    OR EXISTS (SELECT 1 FROM jsonb_array_elements(data.tutkintonimikkeetnimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimiTaiKoodi,'%')))
                    OR EXISTS (SELECT 1 FROM jsonb_array_elements(data.tutkinnonosatnimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimiTaiKoodi,'%')))
                    OR EXISTS (SELECT 1 FROM jsonb_array_elements(data.koodit) kd where kd->>0 LIKE CONCAT('%_',:nimiTaiKoodi,'%'))
                )
            AND (:nimi LIKE ''
                    OR LOWER(data.nimi->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%'))
                    OR EXISTS (SELECT 1 FROM jsonb_array_elements(data.osaamisalanimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%')))
                    OR EXISTS (SELECT 1 FROM jsonb_array_elements(data.tutkintonimikkeetnimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%')))
                    OR EXISTS (SELECT 1 FROM jsonb_array_elements(data.tutkinnonosatnimet) elem WHERE LOWER(elem->>:kieli) LIKE LOWER(CONCAT('%',:nimi,'%')))
                )
            AND CAST(data.kielet as text) LIKE LOWER(CONCAT('%',:kieli,'%'))
            AND (:diaarinumero like '' OR LOWER(data.diaarinumero) LIKE LOWER(:diaarinumero))
            AND (:koodi like '' OR exists (select 1 from jsonb_array_elements(data.koodit) kd where kd->>0 in (:koodi)))
            AND (
                    (:tulevat = false AND :poistuneet = false AND :siirtymat = false AND :voimassa = false)
                    OR (
                        ((:tulevat = true AND CAST(data."voimassaoloAlkaa" as bigint) > :nykyhetki)
                        OR (:poistuneet = true AND CAST(data."voimassaoloLoppuu" as bigint) < :nykyhetki AND COALESCE(CAST(data."siirtymaPaattyy" as bigint), 0) < :nykyhetki)
                        OR (:siirtymat = true
                                AND (data."voimassaoloLoppuu" IS NOT NULL AND data."siirtymaPaattyy" IS NOT NULL
                                AND CAST(data."voimassaoloLoppuu" as bigint) < :nykyhetki AND CAST(data."siirtymaPaattyy" as bigint) > :nykyhetki))
                        OR (:voimassa = true
                                AND (CAST(data."voimassaoloAlkaa" as bigint) < :nykyhetki)
                                AND (data."voimassaoloLoppuu" IS NULL OR CAST(data."voimassaoloLoppuu" as bigint) > :nykyhetki)))
                       )
                )
            AND (
                    COALESCE(:koulutustyypit, null) IS NULL
                    OR data.koulutustyyppi IN (:koulutustyypit)
                    OR exists (select 1 from jsonb_array_elements(data.oppaankoulutustyypit) okt where okt->>0 in (:koulutustyypit))
                )
            """;

    @Query(nativeQuery = true,
            value = "SELECT sivut.id FROM (SELECT DISTINCT ON (data.id) data.id AS id, data.nimi AS nimi " + julkisetPerusteetEhdot + " ORDER BY data.id) sivut ORDER BY sivut.nimi->>:kieli ASC",
            countQuery = "SELECT count(DISTINCT data.id) " + julkisetPerusteetEhdot
    )
    Page<String> findJulkisetPerusteIdt(
            @Param("koulutustyypit") List<String> koulutustyypit,
            @Param("nimi") String nimi,
            @Param("nimiTaiKoodi") String nimiTaiKoodi,
            @Param("kieli") String kieli,
            @Param("nykyhetki") Long nykyhetki,
            @Param("tulevat") boolean tulevat,
            @Param("voimassa") boolean voimassa,
            @Param("siirtymat") boolean siirtymat,
            @Param("poistuneet") boolean poistuneet,
            @Param("tyyppi") String tyyppi,
            @Param("diaarinumero") String diaarinumero,
            @Param("koodi") String koodi,
            @Param("sisaltotyyppi") String sisaltotyyppi,
            Pageable pageable);

    @Query(nativeQuery = true,
            value = """
                    SELECT CAST(jsonb_build_object(
                        'id', data.id,
                        'nimi', data.nimi,
                        'voimassaoloAlkaa', data."voimassaoloAlkaa",
                        'voimassaoloLoppuu', data."voimassaoloLoppuu",
                        'siirtymaPaattyy', data."siirtymaPaattyy",
                        'paatospvm', data.paatospvm,
                        'diaarinumero', data.diaarinumero,
                        'osaamisalat', data.osaamisalat,
                        'tutkintonimikkeet', data.tutkintonimikkeet,
                        'koulutustyyppi', data.koulutustyyppi,
                        'laajuus', data.laajuus,
                        'koulutukset', data.koulutukset,
                        'suoritustavat', CASE
                            WHEN jsonb_typeof(data.suoritustavat) = 'array' THEN COALESCE((
                                SELECT jsonb_agg(jsonb_build_object(
                                    'suoritustapakoodi', st->>'suoritustapakoodi',
                                    'laajuusYksikko', st->>'laajuusYksikko'
                                ))
                                FROM jsonb_array_elements(data.suoritustavat) st
                            ), '[]'::jsonb)
                            ELSE data.suoritustavat
                        END,
                        'koodit', data.koodit,
                        'julkaistu', data.julkaistu,
                        'luotu', data.luotu,
                        'tyyppi', data.tyyppi,
                        'kielet', data.kielet,
                        'tutkinnonosa', data.tutkinnonosa,
                        'sisaltotyyppi', data.sisaltotyyppi
                    ) AS text)
                    FROM julkaistu_peruste_data_store data
                    WHERE data.id IN (:idt)
                    """
    )
    List<String> findJulkisetPerusteJsonByIdt(@Param("idt") List<String> idt);

    @Query(nativeQuery = true,
            value = """
                    SELECT CAST(row_to_json(t) as text)
                    FROM (
                       SELECT *
                       FROM julkaistu_peruste_data_store data
                       WHERE exists (select 1 from jsonb_array_elements(koodit) kd where kd->>0 in (:koodit))
                       AND (
                               (:tulevat = false AND :poistuneet = false AND :siirtymat = false AND :voimassa = false)
                               OR (
                                   ((:tulevat = true AND CAST(data."voimassaoloAlkaa" as bigint) > :nykyhetki)
                                   OR (:poistuneet = true AND CAST(data."voimassaoloLoppuu" as bigint) < :nykyhetki AND COALESCE(CAST(data."siirtymaPaattyy" as bigint), 0) < :nykyhetki)
                                   OR (:siirtymat = true
                                           AND (data."voimassaoloLoppuu" IS NOT NULL AND data."siirtymaPaattyy" IS NOT NULL
                                           AND CAST(data."voimassaoloLoppuu" as bigint) < :nykyhetki AND CAST(data."siirtymaPaattyy" as bigint) > :nykyhetki))
                                   OR (:voimassa = true
                                           AND (CAST(data."voimassaoloAlkaa" as bigint) < :nykyhetki)
                                           AND (data."voimassaoloLoppuu" IS NULL OR CAST(data."voimassaoloLoppuu" as bigint) > :nykyhetki)))
                                  )
                           )
                       AND sisaltotyyppi = 'peruste'
                       AND tyyppi IN ('normaali', 'opas', 'digitaalinen_osaaminen')
                    ) t
                    """
    )
    List<String> findAllJulkaistutPerusteetByKoodi(
            @Param("koodit") Set<String> koodit,
            @Param("nykyhetki") Long nykyhetki,
            @Param("tulevat") boolean tulevat,
            @Param("voimassa") boolean voimassa,
            @Param("siirtymat") boolean siirtymat,
            @Param("poistuneet") boolean poistuneet);

    @Query(nativeQuery = true,
            value = """
                    SELECT CAST(row_to_json(t) as text)
                    FROM (
                       SELECT *
                       FROM julkaistu_peruste_data_store data
                       WHERE 1 = 1
                       AND (
                               (:tulevat = false AND :poistuneet = false AND :siirtymat = false AND :voimassa = false)
                               OR (
                                   ((:tulevat = true AND CAST(data."voimassaoloAlkaa" as bigint) > :nykyhetki)
                                   OR (:poistuneet = true AND CAST(data."voimassaoloLoppuu" as bigint) < :nykyhetki AND COALESCE(CAST(data."siirtymaPaattyy" as bigint), 0) < :nykyhetki)
                                   OR (:siirtymat = true
                                           AND (data."voimassaoloLoppuu" IS NOT NULL AND data."siirtymaPaattyy" IS NOT NULL
                                           AND CAST(data."voimassaoloLoppuu" as bigint) < :nykyhetki AND CAST(data."siirtymaPaattyy" as bigint) > :nykyhetki))
                                   OR (:voimassa = true
                                           AND (CAST(data."voimassaoloAlkaa" as bigint) < :nykyhetki)
                                           AND (data."voimassaoloLoppuu" IS NULL OR CAST(data."voimassaoloLoppuu" as bigint) > :nykyhetki)))
                                  )
                           )
                       AND sisaltotyyppi = 'peruste'
                       AND tyyppi IN ('normaali', 'opas', 'digitaalinen_osaaminen')
                    ) t
                    """
    )
    List<String> findAllJulkaistutPerusteetByVoimassaolo(
            @Param("nykyhetki") Long nykyhetki,
            @Param("tulevat") boolean tulevat,
            @Param("voimassa") boolean voimassa,
            @Param("siirtymat") boolean siirtymat,
            @Param("poistuneet") boolean poistuneet
        );


    @Query(nativeQuery = true,
            value = """
                    SELECT data.koulutustyyppi, COUNT(*) as lukumaara
                    FROM julkaistu_peruste_data_store data
                    WHERE data.koulutustyyppi IS NOT NULL
                    AND (data."voimassaoloAlkaa" IS NULL OR CAST(data."voimassaoloAlkaa" as bigint) < :nykyhetki)
                    AND ((data."voimassaoloLoppuu" IS NULL OR CAST(data."voimassaoloLoppuu" as bigint) > :nykyhetki)
                       OR (data."siirtymaPaattyy" IS NOT NULL AND CAST(data."siirtymaPaattyy" as bigint) > :nykyhetki))
                    AND LOWER(CAST(kielet as text)) LIKE LOWER(CONCAT('%', :kieli,'%'))
                    AND tyyppi IN ('normaali', 'opas', 'digitaalinen_osaaminen')
                    AND sisaltotyyppi = 'peruste'
                    GROUP BY data.koulutustyyppi
                    """
    )
    List<KoulutustyyppiLukumaara> findJulkaistutKoulutustyyppiLukumaaratByKieli(
            @Param("kieli") String kieli,
            @Param("nykyhetki") Long nykyhetki
    );

    List<JulkaistuPeruste> findAllByPerusteId(Long id);

    List<JulkaistuPeruste> findAllByPerusteOrderByRevisionDesc(@Param("peruste") Peruste peruste);

    JulkaistuPeruste findFirstByPerusteAndJulkinenAndLuotuBeforeOrderByRevisionDesc(Peruste peruste, boolean julkinen, Date luotu);

    JulkaistuPeruste findFirstByPerusteOrderByRevisionDesc(@Param("peruste") Peruste peruste);

    JulkaistuPeruste findFirstByPerusteIdOrderByRevisionDesc(long id);

    long countByPeruste(Peruste peruste);

    long countByPerusteId(long perusteid);

    JulkaistuPeruste findOneByPerusteAndLuotu(Peruste peruste, Date aikaleima);

    JulkaistuPeruste findFirstByPerusteAndRevisionOrderByIdDesc(Peruste peruste, int revision);

    @Query("""
            SELECT p
            FROM JulkaistuPeruste jp
                INNER JOIN jp.peruste p
            WHERE p.tyyppi = 'NORMAALI'
               AND p.tila != 'POISTETTU'
               AND p.koulutustyyppi IN ('koulutustyyppi_1', 'koulutustyyppi_11', 'koulutustyyppi_12',
                   'koulutustyyppi_5', 'koulutustyyppi_18',
                   'koulutustyyppi_10', 'koulutustyyppi_30', 'koulutustyyppi_31', 'koulutustyyppi_40')
               AND (p.voimassaoloLoppuu IS NULL
                   OR p.voimassaoloLoppuu > CURRENT_TIMESTAMP
                   OR (p.siirtymaPaattyy IS NOT NULL
                       AND p.siirtymaPaattyy > CURRENT_TIMESTAMP ))
            """
    )
    Set<Peruste> findAmosaaJulkaisut();

    JulkaistuPeruste findOneByDokumentitIn(Set<Long> dokumentit);

    List<JulkaistuPeruste> findByMuutosmaarays(Maarays maarays);

    @Query(
            nativeQuery = true,
            value = """
                    SELECT jsonb_path_query(data,concat('$.tutkinnonOsat[*] ? (@.koodi.uri == "', :tutkinnonOsaKoodiUri, '")')::jsonpath)
                    FROM julkaistu_peruste jp
                    INNER JOIN julkaistu_peruste_data d ON d.id = jp.data_id
                    INNER JOIN peruste p ON p.id = jp.peruste_id
                    WHERE jsonb_path_exists(
                        data,
                        concat('$.tutkinnonOsat[*].koodi ? (@.uri == "', :tutkinnonOsaKoodiUri, '")')::jsonpath
                    )
                    AND p.tyyppi = 'NORMAALI'
                    ORDER BY jp.luotu DESC
                    limit 1
                    """
    )
    Object findTutkinnonOsaByTutkinnonOsaKoodi(@Param("tutkinnonOsaKoodiUri") String tutkinnonOsaKoodiUri);
}
