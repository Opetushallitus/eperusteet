package fi.vm.sade.eperusteet.dto.yl.lukio;

import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.TekstiOsaDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LukiokurssiTarkasteleDto implements Serializable, Lokalisoitava {
    @NotNull
    private Long id;
    @NotNull
    private LukiokurssiTyyppi tyyppi;
    private List<KurssinOppiaineTarkasteluDto> oppiaineet = new ArrayList<>();
    @NotNull
    private LokalisoituTekstiDto nimi;
    private Date muokattu;
    private String koodiArvo;
    private String koodiUri;
    private LokalisoituTekstiDto lokalisoituKoodi;
    private Optional<LokalisoituTekstiDto> kuvaus;
    private Optional<TekstiOsaDto> tavoitteet;
    private Optional<TekstiOsaDto> keskeinenSisalto;
    private Optional<TekstiOsaDto> tavoitteetJaKeskeinenSisalto;
    private Optional<TekstiOsaDto> arviointi;

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Lokalisoitava.of(oppiaineet).lokalisoitavatTekstit();
    }
}
