package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
