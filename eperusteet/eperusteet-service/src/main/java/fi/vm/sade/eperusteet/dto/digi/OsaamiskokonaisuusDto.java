package fi.vm.sade.eperusteet.dto.digi;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("osaamiskokonaisuus")
public class OsaamiskokonaisuusDto extends PerusteenOsaDto.Laaja {
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private LokalisoituTekstiDto keskeinenKasitteisto;
    private List<OsaamiskokonaisuusKasitteistoDto> kasitteistot;

    @Override
    public String getOsanTyyppi() {
        return "osaamiskokonaisuus";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.osaamiskokonaisuus;
    }
}
