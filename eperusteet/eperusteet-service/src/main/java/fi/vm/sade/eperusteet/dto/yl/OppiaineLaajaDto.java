package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OppiaineLaajaDto extends OppiaineBaseDto {
    private Optional<TekstiOsaDto> tehtava;
    private List<KevytTekstiKappaleDto> vapaatTekstit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<OppiaineDto> oppimaarat;
    private Set<OpetuksenKohdealueDto> kohdealueet;
    private Set<OppiaineenVuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet;
    private String koodiUri;
    private String koodiArvo;
}
