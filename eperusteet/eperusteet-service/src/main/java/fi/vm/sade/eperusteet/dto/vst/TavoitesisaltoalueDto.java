package fi.vm.sade.eperusteet.dto.vst;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.peruste.OsanTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonTypeName("tavoitesisaltoalue")
public class TavoitesisaltoalueDto extends PerusteenOsaDto.Laaja {

    private KoodiDto nimiKoodi;
    private LokalisoituTekstiDto teksti;
    private List<TavoiteAlueDto> tavoitealueet = new ArrayList<>();

    @Override
    public OsanTyyppi getOsanTyyppi() {
        return OsanTyyppi.TAVOITESISALTOALUE;
    }
}
