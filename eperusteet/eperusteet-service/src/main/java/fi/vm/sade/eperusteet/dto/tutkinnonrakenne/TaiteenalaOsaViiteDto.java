package fi.vm.sade.eperusteet.dto.tutkinnonrakenne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.PerusteRakenneOsa;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaiteenalaOsaViiteDto {
    private String taiteenOsa;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto teksti;
    private Integer jnro;
    private Long taiteenalaId;

    public static TaiteenalaOsaViiteDto of(String taiteenOsa, KevytTekstiKappaleDto osanTeksti, Long taiteenalaId) {
        if (osanTeksti == null) {
            return null;
        }

        TaiteenalaOsaViiteDto result = new TaiteenalaOsaViiteDto();
        result.setTaiteenOsa(taiteenOsa);
        result.setNimi(osanTeksti.getNimi());
        result.setTeksti(osanTeksti.getTeksti());
        result.setJnro(osanTeksti.getJnro());
        result.setTaiteenalaId(taiteenalaId);
        return result;
    }

    public PerusteRakenneOsa getPerusteenOsa() {
        return new PerusteRakenneOsa("taiteenala_taiteenosa", getNimi());
    }

}
