package fi.vm.sade.eperusteet.dto.tuva;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Data;
import org.springframework.util.CollectionUtils;

@Data
@JsonTypeName("laajaalainenosaaminen")
public class TuvaLaajaAlainenOsaaminenDto extends PerusteenOsaDto.Laaja {

    private KoodiDto nimiKoodi;
    private LokalisoituTekstiDto teksti;
    private Boolean liite;

    @Override
    public String getOsanTyyppi() {
        return "laajaalainenosaaminen";
    }

    @Override
    public LokalisoituTekstiDto getNimi() {
        if (nimiKoodi != null && !CollectionUtils.isEmpty(nimiKoodi.getNimi())) {
            return new LokalisoituTekstiDto(nimiKoodi.getNimi());
        } else {
            return super.getNimi();
        }
    }
}
