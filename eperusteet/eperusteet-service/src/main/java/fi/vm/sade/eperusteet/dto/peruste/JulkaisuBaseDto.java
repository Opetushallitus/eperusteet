package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JulkaisuBaseDto {
    private int revision;
    private PerusteBaseDto peruste;
    private LokalisoituTekstiDto tiedote;
    private Date luotu;
    private String luoja;

    private KayttajanTietoDto kayttajanTieto;
}
