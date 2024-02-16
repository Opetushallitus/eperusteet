package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("taiteenala")
public class TaiteenalaDto extends PerusteenOsaDto.Laaja {

    public TaiteenalaDto(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        super(nimi, tila, tunniste);
    }

    private LokalisoituTekstiDto teksti;
    private KoodiDto koodi;
    private KevytTekstiKappaleDto kasvatus;
    private KevytTekstiKappaleDto yhteisetOpinnot;
    private KevytTekstiKappaleDto teemaopinnot;
    private KevytTekstiKappaleDto aikuistenOpetus;
    private KevytTekstiKappaleDto tyotavatOpetuksessa;
    private KevytTekstiKappaleDto oppimisenArviointiOpetuksessa;
    private List<KevytTekstiKappaleDto> vapaatTekstit;

    @Override
    public String getOsanTyyppi() {
        return "taiteenala";
    }

    public Map<String, KevytTekstiKappaleDto> getOsaavainMap() {
        return new LinkedHashMap<String, KevytTekstiKappaleDto>() {{
            put("aikuistenOpetus", getAikuistenOpetus());
            put("kasvatus", getKasvatus());
            put("oppimisenArviointiOpetuksessa", getOppimisenArviointiOpetuksessa());
            put("teemaopinnot", getTeemaopinnot());
            put("tyotavatOpetuksessa", getTyotavatOpetuksessa());
            put("yhteisetOpinnot", getYhteisetOpinnot());
        }};
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.taiteenala;
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
