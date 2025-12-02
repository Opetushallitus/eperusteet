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

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("kaantajataitotasoasteikko")
public class KaantajaTaitotasoasteikkoDto extends PerusteenOsaDto.Laaja implements Liitteellinen {

    private LokalisoituTekstiDto kuvaus;
    private List<TaitotasoasteikkoKategoriaDto> taitotasoasteikkoKategoriat = new ArrayList<>();
    private Boolean liite;

    @Override
    public String getOsanTyyppi() {
        return "kaantajataitotasoasteikko";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.kaantajataitotasoasteikko;
    }

    @Override
    public boolean isLiite() {
        return liite != null && liite;
    }
}

