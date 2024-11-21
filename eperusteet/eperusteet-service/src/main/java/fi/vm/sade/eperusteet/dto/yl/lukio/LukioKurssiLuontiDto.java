package fi.vm.sade.eperusteet.dto.yl.lukio;

import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.TekstiOsaDto;
import lombok.*;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LukioKurssiLuontiDto implements Serializable {
    @NotNull
    private LukiokurssiTyyppi tyyppi;
    private List<KurssinOppiaineDto> oppiaineet;
    @NotNull
    private LokalisoituTekstiDto nimi;
    private String koodiArvo;
    private String koodiUri;
    private LokalisoituTekstiDto lokalisoituKoodi;
    private Optional<LokalisoituTekstiDto> kuvaus;
    private Optional<TekstiOsaDto> tavoitteet;
    private Optional<TekstiOsaDto> keskeinenSisalto;
    private Optional<TekstiOsaDto> tavoitteetJaKeskeinenSisalto;
    private Optional<TekstiOsaDto> arviointi;
    private String kommentti;
}
