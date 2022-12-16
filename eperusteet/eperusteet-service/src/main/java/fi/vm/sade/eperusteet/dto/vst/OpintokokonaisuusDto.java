package fi.vm.sade.eperusteet.dto.vst;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("opintokokonaisuus")
public class OpintokokonaisuusDto extends PerusteenOsaDto.Laaja {

    private KoodiDto nimiKoodi;
    private Integer minimilaajuus;
    private LokalisoituTekstiDto kuvaus;
    private LokalisoituTekstiDto opetuksenTavoiteOtsikko;
    private List<KoodiDto> opetuksenTavoitteet = new ArrayList<>();
    private List<LokalisoituTekstiDto> arvioinnit;

    @Override
    public String getOsanTyyppi() {
        return "opintokokonaisuus";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.opintokokonaisuus;
    }
}
