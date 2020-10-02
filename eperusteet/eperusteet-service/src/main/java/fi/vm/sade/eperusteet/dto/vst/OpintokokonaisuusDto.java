package fi.vm.sade.eperusteet.dto.vst;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonTypeName("opintokokonaisuus")
public class OpintokokonaisuusDto extends PerusteenOsaDto.Laaja {

    private KoodiDto nimiKoodi;
    private Integer laajuus;
    private LokalisoituTekstiDto kuvaus;
    private LokalisoituTekstiDto opetuksenTavoiteOtsikko;
    private List<KoodiDto> opetuksenTavoitteet = new ArrayList<>();
    private List<LokalisoituTekstiDto> arvioinnit;

    @Override
    public String getOsanTyyppi() {
        return "opintokokonaisuus";
    }
}
