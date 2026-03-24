package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.liite.Liitteellinen;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("tekstikappale")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TekstiKappaleDto extends PerusteenOsaDto.Laaja implements Liitteellinen {
    private LokalisoituTekstiDto teksti;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private KoodiDto osaamisala;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private KoodiDto tutkintonimike;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private KoodiDto koodi;
    private Boolean liite;

    public TekstiKappaleDto(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        super(nimi, tila, tunniste);
    }

    @Override
    public String getOsanTyyppi() {
        return "tekstikappale";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.viite;
    }

    public LokalisoituTekstiDto getNimi() {
        if (osaamisala != null && osaamisala.getNimi() != null) {
            return osaamisala.getNimi();
        }

        if (tutkintonimike != null && tutkintonimike.getNimi() != null) {
            return tutkintonimike.getNimi();
        }

        if (koodi != null && koodi.getNimi() != null) {
            return koodi.getNimi();
        }

        return super.getNimi();
    }

    @Override
    public boolean isLiite() {
        return liite != null && liite;
    }
}
