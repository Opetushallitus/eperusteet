package fi.vm.sade.eperusteet.service.export;

import org.springframework.stereotype.Component;

@Component
public class PerusteArkistoituLampiExportDataProvider implements LampiExportDataProvider {

    @Override
    public String getCsvFileName() {
        return "peruste_arkistoitu.csv";
    }

    @Override
    public String getQuery() {
        return """
            SELECT jp.peruste_id, pe.tila, jpd.data
            FROM julkaistu_peruste jp
            INNER JOIN peruste pe on pe.id = jp.peruste_id
            INNER JOIN julkaistu_peruste_data jpd ON jp.data_id = jpd.id
            WHERE jp.revision = (SELECT MAX(revision) FROM julkaistu_peruste WHERE peruste_id = jp.peruste_id)
              AND pe.tyyppi = 'NORMAALI'
              AND (
                pe.tila = 'POISTETTU'
                OR (
                (pe.voimassaolo_loppuu IS NOT NULL AND pe.voimassaolo_loppuu < current_timestamp)
                  AND (pe.siirtyma_paattyy IS NULL OR pe.siirtyma_paattyy < current_timestamp)
                )
            )
            """;
    }
}
