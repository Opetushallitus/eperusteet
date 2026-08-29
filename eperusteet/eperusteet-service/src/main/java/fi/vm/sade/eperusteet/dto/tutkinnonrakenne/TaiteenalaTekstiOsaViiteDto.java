package fi.vm.sade.eperusteet.dto.tutkinnonrakenne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteRakenneOsa;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaiteenalaTekstiOsaViiteDto {
    private String taiteenTekstiOsa;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto teksti;
    private Integer jnro;
    private Long taiteenalaId;

    public static TaiteenalaTekstiOsaViiteDto of(String taiteenTekstiOsa, KevytTekstiKappaleDto osanTeksti, Long taiteenalaId) {
        if (osanTeksti == null) {
            return null;
        }

        TaiteenalaTekstiOsaViiteDto result = new TaiteenalaTekstiOsaViiteDto();
        result.setTaiteenTekstiOsa(taiteenTekstiOsa);
        result.setNimi(osanTeksti.getNimi());
        result.setTeksti(osanTeksti.getTeksti());
        result.setJnro(osanTeksti.getJnro());
        result.setTaiteenalaId(taiteenalaId);
        return result;
    }

    public PerusteRakenneOsa getPerusteenOsa() {
        return new PerusteRakenneOsa("taiteenala_taiteentekstiosa", getNimi());
    }

}
