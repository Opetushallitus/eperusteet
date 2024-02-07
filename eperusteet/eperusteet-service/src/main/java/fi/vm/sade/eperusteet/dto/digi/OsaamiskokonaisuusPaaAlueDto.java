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
@JsonTypeName("osaamiskokonaisuus_paa_alue")
public class OsaamiskokonaisuusPaaAlueDto extends PerusteenOsaDto.Laaja{
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private List<OsaamiskokonaisuusOsaAlueDto> osaAlueet;

    @Override
    public String getOsanTyyppi() {
        return "osaamiskokonaisuus_paa_alue";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.osaamiskokonaisuus_paa_alue;
    }
}
