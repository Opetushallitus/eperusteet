package fi.vm.sade.eperusteet.dto.kios;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.liite.Liitteellinen;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("kaantajatodistusmalli")
public class KaantajaTodistusmalliDto extends PerusteenOsaDto.Laaja implements Liitteellinen {

    private LokalisoituTekstiDto kuvaus;
    private KaantajaTodistusmalliTaitotasokuvausDto ylintaso;
    private KaantajaTodistusmalliTaitotasokuvausDto keskitaso;
    private KaantajaTodistusmalliTaitotasokuvausDto perustaso;
    private Boolean liite;

    @Override
    public String getOsanTyyppi() {
        return "kaantajatodistusmalli";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.kaantajatodistusmalli;
    }

    @Override
    public boolean isLiite() {
        return liite != null && liite;
    }
}

