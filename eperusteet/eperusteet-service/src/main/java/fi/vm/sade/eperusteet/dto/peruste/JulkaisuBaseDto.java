package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JulkaisuBaseDto {
    private int revision;
    private PerusteBaseDto peruste;
    private LokalisoituTekstiDto tiedote;
    private Date luotu;
    private String luoja;

    private KayttajanTietoDto kayttajanTieto;
}
