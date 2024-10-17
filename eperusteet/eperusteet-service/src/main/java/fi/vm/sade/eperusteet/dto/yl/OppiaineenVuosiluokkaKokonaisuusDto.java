package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.yl.Vuosiluokka;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.PerusteRakenneOsa;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OppiaineenVuosiluokkaKokonaisuusDto implements ReferenceableDto {
    private Long id;
    private Optional<Reference> vuosiluokkaKokonaisuus;
    private Optional<TekstiOsaDto> tehtava;
    private Optional<TekstiOsaDto> tyotavat;
    private Optional<TekstiOsaDto> ohjaus;
    private Optional<TekstiOsaDto> arviointi;
    private Optional<TekstiOsaDto> sisaltoalueinfo;
    private Optional<LokalisoituTekstiDto> opetuksenTavoitteetOtsikko;
    private Optional<LokalisoituTekstiDto> vapaaTeksti;
    private List<OpetuksenTavoiteDto> tavoitteet;
    private List<KeskeinenSisaltoalueDto> sisaltoalueet;
    private List<KevytTekstiKappaleDto> vapaatTekstit;
    private OppiaineKevytDto oppiaine;
    private Set<Vuosiluokka> vuosiluokat = Set.of();

    public PerusteRakenneOsa getPerusteenOsa() {
        return new PerusteRakenneOsa(
                "oppiaineen_vuosiluokkakokonaisuus",
                oppiaine != null ? oppiaine.getNimi() : null,
                new HashMap<>(){{
                    put("nimi", vuosiluokat.stream()
                        .map(Vuosiluokka::toString)
                        .sorted()
                        .reduce("vuosiluokka_", (acc, next) -> acc + next.replaceAll("\\D+", "")));
                }});
    }
}
