package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.vst.TavoiteAlueDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("taiteenalavaihe")
public class TaiteenalaVaiheDto extends PerusteenOsaDto.Laaja {

    private KoodiDto koodi;
    private Integer laajuus;
    private LokalisoituTekstiDto kuvaus;
    private List<TavoiteAlueDto> tavoitealueet = new ArrayList<>();
    private List<KevytTekstiKappaleDto> vapaatTekstit = new ArrayList<>();

    @Override
    public String getOsanTyyppi() {
        return "taiteenalavaihe";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.taiteenalavaihe;
    }

    @Override
    public LokalisoituTekstiDto getNimi() {
        if (koodi != null && koodi.getNimi() != null && !CollectionUtils.isEmpty(koodi.getNimi().getTekstit())) {
            return koodi.getNimi();
        } else {
            return super.getNimi();
        }
    }
}
