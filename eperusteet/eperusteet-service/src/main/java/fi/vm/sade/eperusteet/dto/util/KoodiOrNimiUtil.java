package fi.vm.sade.eperusteet.dto.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class KoodiOrNimiUtil {
  public static LokalisoituTekstiDto getNimi(KoodiDto koodi, LokalisoituTekstiDto nimi) {
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
