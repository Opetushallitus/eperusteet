package fi.vm.sade.eperusteet.dto.yl.lukio;

import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.dto.IdHolder;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.TekstiOsaDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LukiokurssiMuokkausDto implements Serializable, IdHolder {
    @NotNull
    private Long id;
    @NotNull
    private LukiokurssiTyyppi tyyppi;
    @NotNull
    private LokalisoituTekstiDto nimi;
    private String koodiArvo;
    private String koodiUri;
    private LokalisoituTekstiDto lokalisoituKoodi;
    private LokalisoituTekstiDto kuvaus;
    private TekstiOsaDto tavoitteet;
    private TekstiOsaDto keskeinenSisalto;
    private TekstiOsaDto tavoitteetJaKeskeinenSisalto;
    private TekstiOsaDto arviointi;
    private String kommentti;
}
