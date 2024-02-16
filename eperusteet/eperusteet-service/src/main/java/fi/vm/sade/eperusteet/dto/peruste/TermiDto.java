package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermiDto {
    private Long id;
    private String avain;
    private LokalisoituTekstiDto termi;
    private LokalisoituTekstiDto selitys;
    private boolean alaviite;
}
