package fi.vm.sade.eperusteet.dto.vst;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Data;

@Data
@JsonTypeName("koto_laajaalainenosaaminen")
public class KotoLaajaAlainenOsaaminenDto extends PerusteenOsaDto.Laaja {

    private LokalisoituTekstiDto yleiskuvaus;

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.koto_laajaalainenosaaminen;
    }

    @Override
    public String getOsanTyyppi() {
        return "koto_laajaalainenosaaminen";
    }
}
