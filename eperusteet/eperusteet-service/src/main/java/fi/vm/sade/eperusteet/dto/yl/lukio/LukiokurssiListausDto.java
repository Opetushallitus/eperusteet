package fi.vm.sade.eperusteet.dto.yl.lukio;

import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localizeLaterById;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LukiokurssiListausDto implements Serializable, Lokalisoitava {
    private List<KurssinOppiaineNimettyDto> oppiaineet = new ArrayList<>();
    private Long id;
    private String koodiArvo;
    private LokalisoituTekstiDto lokalisoituKoodi;
    private LukiokurssiTyyppi tyyppi;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private Date muokattu;

    public LukiokurssiListausDto(Long id, LukiokurssiTyyppi tyyppi,
                                 String koodiArvo, Long lokalisoituKoodiId,
                                 Long nimiId, Long kuvausId, Date muokattu) {
        this.id = id;
        this.tyyppi = tyyppi;
        this.koodiArvo = koodiArvo;
        this.lokalisoituKoodi = localizeLaterById(lokalisoituKoodiId);
        this.nimi = localizeLaterById(nimiId);
        this.kuvaus = localizeLaterById(kuvausId);
        this.muokattu = muokattu;
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Lokalisoitava.of(nimi, lokalisoituKoodi, kuvaus).and(oppiaineet).lokalisoitavatTekstit();
    }
}
