package fi.vm.sade.eperusteet.service.dokumentti;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;

public interface ExternalPdfService {
    void generatePdf(DokumenttiDto dto) throws JsonProcessingException;
    void generatePdf(DokumenttiDto dto, PerusteKaikkiDto perusteDto) throws JsonProcessingException;
}