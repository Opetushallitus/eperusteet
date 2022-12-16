package fi.vm.sade.eperusteet.dto.tuva;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
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
        if (nimiKoodi != null && nimiKoodi.getNimi() != null && !CollectionUtils.isEmpty(nimiKoodi.getNimi().getTekstit())) {
            return nimiKoodi.getNimi();
        } else {
            return super.getNimi();
        }
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.laajaalainenosaaminen;
    }
}
