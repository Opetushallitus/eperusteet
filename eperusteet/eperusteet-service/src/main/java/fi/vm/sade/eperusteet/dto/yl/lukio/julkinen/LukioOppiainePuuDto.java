package fi.vm.sade.eperusteet.dto.yl.lukio.julkinen;

import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LukioOppiainePuuDto implements Lokalisoitava {
    private Long perusteId;
    private List<LukioOppiaineOppimaaraNodeDto> oppiaineet = new ArrayList<>();

    public LukioOppiainePuuDto(Long perusteId) {
        this.perusteId = perusteId;
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Lokalisoitava.of(oppiaineet).lokalisoitavatTekstit();
    }
}
