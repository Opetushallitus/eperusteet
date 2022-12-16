package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.JulkaisuTila;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JulkaisuBaseDto {
    private int revision;
    private PerusteBaseDto peruste;
    private LokalisoituTekstiDto tiedote;
    private Date luotu;
    private String luoja;

    private KayttajanTietoDto kayttajanTieto;
    private JulkaisuTila tila = JulkaisuTila.JULKAISTU;

}
