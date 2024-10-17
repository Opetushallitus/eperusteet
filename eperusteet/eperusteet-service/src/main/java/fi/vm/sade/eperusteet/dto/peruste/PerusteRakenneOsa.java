package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class PerusteRakenneOsa {
    private String osanTyyppi;
    private LokalisoituTekstiDto nimi;
    private Map<String, Object> meta;

    public PerusteRakenneOsa(String osanTyyppi, LokalisoituTekstiDto nimi) {
        this.osanTyyppi = osanTyyppi;
        this.nimi = nimi;
    }

    public PerusteRakenneOsa(String osanTyyppi) {
        this.osanTyyppi = osanTyyppi;
    }
}
