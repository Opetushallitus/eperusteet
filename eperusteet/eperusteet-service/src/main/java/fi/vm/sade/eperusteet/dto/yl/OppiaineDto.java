package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
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
public class OppiaineDto extends OppiaineBaseUpdateDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<OppiaineSuppeaDto> oppimaarat;
    private Set<OpetuksenKohdealueDto> kohdealueet;
    private Set<OppiaineenVuosiluokkaKokonaisuusDto> vuosiluokkakokonaisuudet;
    private KoodiDto koodi;
    private List<KevytTekstiKappaleDto> vapaatTekstit;

    public Optional<LokalisoituTekstiDto> getNimi() {
        if (super.getNimi() != null && super.getNimi().isPresent()) {
            return super.getNimi();
        }

        return Optional.ofNullable(koodi).map(KoodiDto::getNimi);
    }
}
