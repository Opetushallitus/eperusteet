package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
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
public class AIPEOppiaineLaajaDto extends AIPEOppiaineSuppeaDto {
    private Optional<TekstiOsaDto> tehtava;
    private Optional<TekstiOsaDto> arviointi;
    private Optional<TekstiOsaDto> tyotavat;
    private Optional<TekstiOsaDto> ohjaus;
    private Optional<TekstiOsaDto> sisaltoalueinfo;

    private Optional<LokalisoituTekstiDto> pakollinenKurssiKuvaus;
    private Optional<LokalisoituTekstiDto> syventavaKurssiKuvaus;
    private Optional<LokalisoituTekstiDto> soveltavaKurssiKuvaus;

    private Optional<LokalisoituTekstiDto> kielikasvatus;

    private List<OpetuksenTavoiteDto> tavoitteet;

    private List<KeskeinenSisaltoalueDto> sisaltoalueet;

    private List<AIPEKurssiDto> kurssit;

    private List<AIPEOppiaineLaajaDto> oppimaarat;
}
