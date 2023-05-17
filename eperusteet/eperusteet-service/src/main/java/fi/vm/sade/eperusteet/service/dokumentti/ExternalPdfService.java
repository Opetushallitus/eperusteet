package fi.vm.sade.eperusteet.service.dokumentti;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;

public interface ExternalPdfService {
    void generatePdf(DokumenttiDto dto) throws JsonProcessingException;
}