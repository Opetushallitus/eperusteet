package fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuusDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("aihekokonaisuudet")
public class AihekokonaisuudetLaajaDto extends Laaja {
    private UUID uuidTunniste;
    private LokalisoituTekstiDto otsikko;
    private LokalisoituTekstiDto yleiskuvaus;
    private List<AihekokonaisuusDto> aihekokonaisuudet;

    public AihekokonaisuudetLaajaDto(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        super(nimi, tila, tunniste);
    }

    @Override
    public String getOsanTyyppi() {
        return "aihekokonaisuudet";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.aihekokonaisuudet;
    }
}
