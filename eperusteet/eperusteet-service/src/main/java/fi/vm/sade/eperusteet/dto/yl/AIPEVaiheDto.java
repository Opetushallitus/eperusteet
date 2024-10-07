package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteRakenneOsa;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIPEVaiheDto extends AIPEVaiheSuppeaDto {
    private Optional<TekstiOsaDto> siirtymaEdellisesta;
    private Optional<TekstiOsaDto> tehtava;
    private Optional<TekstiOsaDto> siirtymaSeuraavaan;
    private Optional<TekstiOsaDto> laajaalainenOsaaminen;
    private Optional<TekstiOsaDto> paikallisestiPaatettavatAsiat;
    private List<OpetuksenKohdealueDto> opetuksenKohdealueet;
    private List<AIPEOppiaineLaajaDto> oppiaineet;
    private List<KevytTekstiKappaleDto> vapaatTekstit;

    public PerusteRakenneOsa getPerusteenOsa() {
        return new PerusteRakenneOsa("aipe_vaihe", getNimi().get());
    }
}
