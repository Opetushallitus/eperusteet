package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class TutkinnonOsaKevytDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private KoodiDto koodi;
    private Date muokattu;
 
    public LokalisoituTekstiDto getNimi() {
        if (koodi != null && koodi.getNimi() != null && !CollectionUtils.isEmpty(koodi.getNimi().getTekstit())) {
            Map<String, String> kielet = new HashMap<>();
            Map<Kieli, String> tutkinnonOsaNimi = nimi != null ? nimi.getTekstit() : new HashMap<Kieli, String>();
            kielet.computeIfAbsent("fi", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.FI, tutkinnonOsaNimi.get(Kieli.FI)));
            kielet.computeIfAbsent("sv", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.SV, tutkinnonOsaNimi.get(Kieli.SV)));
            kielet.computeIfAbsent("en", val -> koodi.getNimi().getTekstit().getOrDefault(Kieli.EN, tutkinnonOsaNimi.get(Kieli.EN)));
            return new LokalisoituTekstiDto(kielet);
        } else {
            return nimi;
        }
    }
}
