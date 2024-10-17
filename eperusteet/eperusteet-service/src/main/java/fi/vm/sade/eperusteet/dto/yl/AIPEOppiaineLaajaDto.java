package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.dto.peruste.PerusteRakenneOsa;
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
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private AIPEVaiheSuppeaDto vaihe;

    public PerusteRakenneOsa getPerusteenOsa() {
        return new PerusteRakenneOsa("aipe_oppiaine", getNimi().get());
    }

    public void setVaihe(AIPEVaiheSuppeaDto vaihe) {
        this.vaihe = vaihe;
        if (this.kurssit != null) {
            this.kurssit.forEach(kurssi -> kurssi.setVaihe(vaihe));
        }
    }
}
