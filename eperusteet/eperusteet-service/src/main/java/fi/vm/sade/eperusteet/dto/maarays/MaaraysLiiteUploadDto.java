package fi.vm.sade.eperusteet.dto.maarays;

import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiiteTyyppi;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaaraysLiiteUploadDto {
    private LokalisoituTekstiDto nimi;
    private String tiedostonimi;
    private MaaraysLiiteTyyppi tyyppi;
    private String fileB64;
}
