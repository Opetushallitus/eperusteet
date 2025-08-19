package fi.vm.sade.eperusteet.dto.pdf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfData {
    private String data;
    private String html;
    private String tila;
}