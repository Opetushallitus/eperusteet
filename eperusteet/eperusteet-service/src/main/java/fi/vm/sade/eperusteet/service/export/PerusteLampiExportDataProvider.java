package fi.vm.sade.eperusteet.service.export;

import org.springframework.stereotype.Component;

@Component
public class PerusteLampiExportDataProvider implements LampiExportDataProvider {

    @Override
    public String getCsvFileName() {
        return "peruste.csv";
    }

    @Override
    public String getQuery() {
        return """
            SELECT jp.peruste_id, jpd.data
            FROM julkaistu_peruste jp
            INNER JOIN peruste pe on pe.id = jp.peruste_id
            INNER JOIN julkaistu_peruste_data jpd ON jp.data_id = jpd.id
            WHERE jp.revision = (SELECT MAX(revision) FROM julkaistu_peruste WHERE peruste_id = jp.peruste_id)
              AND pe.tila != 'POISTETTU'
              AND pe.tyyppi = 'NORMAALI'
              AND (
                pe.voimassaolo_loppuu IS NULL
                OR (pe.voimassaolo_loppuu > current_timestamp)
                OR (pe.voimassaolo_loppuu < current_timestamp AND pe.siirtyma_paattyy IS NOT NULL AND pe.siirtyma_paattyy > current_timestamp)
            )
            """;
    }
}
