package fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.dto.peruste.PerusteRakenneOsa;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lops2019LaajaAlainenOsaaminenDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private KoodiDto koodi;
    private LokalisoituTekstiDto kuvaus;

    public LokalisoituTekstiDto getNimi() {
        if (koodi != null ) {
            return koodi.getNimi();
        }

        return nimi;
    }

    public PerusteRakenneOsa getPerusteenOsa() {
        return new PerusteRakenneOsa("lukio_laajaalainen_osaaminen", getNimi());
    }
}
