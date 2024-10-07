package fi.vm.sade.eperusteet.dto.lops2019;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineBaseDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteRakenneOsa;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lops2019OppiaineKaikkiDto extends Lops2019OppiaineBaseDto {
    private LokalisoituTekstiDto pakollisetModuulitKuvaus;
    private LokalisoituTekstiDto valinnaisetModuulitKuvaus;
    private List<Lops2019ModuuliDto> moduulit = new ArrayList<>();
    private List<Lops2019OppiaineKaikkiDto> oppimaarat = new ArrayList<>();

    public PerusteRakenneOsa getPerusteenOsa() {
        return new PerusteRakenneOsa("lukio_oppiaine", getNimi());
    }
}
