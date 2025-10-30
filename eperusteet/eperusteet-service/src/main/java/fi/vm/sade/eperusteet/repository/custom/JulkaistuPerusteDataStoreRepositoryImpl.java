package fi.vm.sade.eperusteet.repository.custom;

import fi.vm.sade.eperusteet.repository.JulkaistuPerusteDataStoreRepository;
import fi.vm.sade.eperusteet.service.MaintenanceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Profile("!test")
@Service
@Transactional
public class JulkaistuPerusteDataStoreRepositoryImpl implements JulkaistuPerusteDataStoreRepository {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void syncPeruste(Long perusteId) {
        deleteFromJulkaisu(perusteId);
        createJulkaisu(perusteId);
        maintenanceService.clearPerusteCaches(perusteId);
    }

    @Override
    public List<Long> findPerusteIdsByKoulutustyypit(List<String> koulutustyypit) {
        String sql = "SELECT DISTINCT perusteid FROM julkaistu_peruste_data_store WHERE koulutustyyppi IN (:koulutustyypit)";

        return (List<Long>) entityManager.createNativeQuery(sql)
                .setParameter("koulutustyypit", koulutustyypit)
                .getResultList()
                .stream()
                .map(o -> Long.parseLong(o.toString()))
                .collect(Collectors.toList());
    }

    private void deleteFromJulkaisu(Long perusteId) {
        String sql = "DELETE FROM julkaistu_peruste_data_store WHERE perusteid = :perusteid";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("perusteid", perusteId.toString());
        query.executeUpdate();
    }

    private void createJulkaisu(Long perusteId) {
        String sql = """
                INSERT INTO julkaistu_peruste_data_store
                SELECT
                        data->'nimi' as nimi,
                        data->'kielet' as kielet,
                        data->>'voimassaoloAlkaa' as "voimassaoloAlkaa",
                        data->>'voimassaoloLoppuu' as "voimassaoloLoppuu",
                        data->>'siirtymaPaattyy' as "siirtymaPaattyy",
                        data->>'paatospvm' as paatospvm,
                        data->>'id' as id,
                        data->>'id' as perusteid,
                        data->>'diaarinumero' as diaarinumero,
                        data->'osaamisalat' as osaamisalat,
                        data->'tutkintonimikkeet' as tutkintonimikkeet,
                        data->'tutkinnonOsat' as tutkinnonosat,
                        p.tila,
                        data->>'koulutusvienti' as koulutusvienti,
                        data->>'koulutustyyppi' as koulutustyyppi,
                        data->>'tyyppi' as tyyppi,
                        data->'oppaanKoulutustyypit' as oppaankoulutustyypit,
                        data->'suoritustavat'->0->'rakenne'->'muodostumisSaanto'->'laajuus'->>'minimi' as laajuus,
                        CAST((SELECT json_agg(tdata->'nimi') from jsonb_array_elements(data->'osaamisalat') tdata) AS jsonb) as osaamisalanimet,
                        CAST((SELECT json_agg(tdata->'nimi') from jsonb_array_elements(data->'tutkintonimikkeet') tdata) AS jsonb) as tutkintonimikkeetnimet,
                        CAST((SELECT json_agg(tdata->'nimi') from jsonb_array_elements(data->'tutkinnonOsat') tdata) AS jsonb) as tutkinnonosatnimet,
                        data->'koulutukset' as koulutukset,
                        jp.luotu as julkaistu,
                        data->'suoritustavat' as suoritustavat,
                        data->>'luotu' as luotu,
                        COALESCE(CAST((SELECT json_agg(tdata->'nimiKoodi'->'uri') from jsonb_array_elements(data->'koulutuksenOsat') tdata) AS jsonb), CAST('[]' AS jsonb)) ||
                        COALESCE(CAST((SELECT json_agg(tdata->'koodi'->'uri') from jsonb_array_elements(data->'tutkinnonOsat') tdata) AS jsonb), CAST('[]' AS jsonb)) ||
                        COALESCE(CAST((SELECT json_agg(tdata->'koulutuskoodiUri') from jsonb_array_elements(data->'koulutukset') tdata) AS jsonb), CAST('[]' AS jsonb)) ||
                        COALESCE(CAST((SELECT json_agg(tdata->'uri') from jsonb_array_elements(data->'osaamisalat') tdata) AS jsonb), CAST('[]' AS jsonb)) ||
                        COALESCE(CAST((SELECT json_agg(tdata->'tutkintonimikeUri') from jsonb_array_elements(data->'tutkintonimikkeet') tdata) AS jsonb), CAST('[]' AS jsonb)) as "koodit",
                        null as tutkinnonosa,
                        'peruste' as sisaltotyyppi
                    FROM julkaistu_peruste jp
                        INNER JOIN julkaistu_peruste_data d on d.id = jp.data_id
                        INNER JOIN peruste p on p.id = jp.peruste_id
                    where revision = (SELECT MAX(revision) FROM julkaistu_peruste j2 WHERE jp.peruste_id = j2.peruste_id)
                      AND p.tila != 'POISTETTU'
                      AND jp.peruste_id = :perusteId
                    union all
                    select
                        tutkinnonosa->'nimi',
                        kielet,
                        "voimassaoloAlkaa",
                        "voimassaoloLoppuu",
                        "siirtymaPaattyy",
                        paatospvm,
                        tutkinnonosa->> 'id',
                        perusteid,
                        null,null,null,null,null,
                        koulutusvienti, koulutustyyppi, tyyppi,
                        null,null,null,null,null,null,null,null,null,
                        CAST(('[' || CAST((tutkinnonosa->'koodi'->'uri') AS varchar) || ']') AS jsonb),
                        tutkinnonosa,
                        'tutkinnonosa' as sisaltotyyppi
                    from (SELECT
                               data->'kielet' as kielet,
                                data->>'voimassaoloAlkaa' as "voimassaoloAlkaa",
                                data->>'voimassaoloLoppuu' as "voimassaoloLoppuu",
                                data->>'siirtymaPaattyy' as "siirtymaPaattyy",
                                data->>'paatospvm' as paatospvm,
                                data->>'tyyppi' as tyyppi,
                                data->>'koulutusvienti' as koulutusvienti,
                                data->>'koulutustyyppi' as koulutustyyppi,
                                data->>'id' as perusteid,
                                jsonb_array_elements(data->'tutkinnonOsat') as tutkinnonosa
                            FROM julkaistu_peruste jp
                                INNER JOIN julkaistu_peruste_data d on d.id = jp.data_id
                                INNER JOIN peruste p on p.id = jp.peruste_id
                            where revision = (SELECT MAX(revision) FROM julkaistu_peruste j2 WHERE jp.peruste_id = j2.peruste_id)
                              AND p.tila != 'POISTETTU'
                              AND jp.peruste_id = :perusteId
                            ) subquery
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("perusteId", perusteId);
        query.executeUpdate();
    }

}
