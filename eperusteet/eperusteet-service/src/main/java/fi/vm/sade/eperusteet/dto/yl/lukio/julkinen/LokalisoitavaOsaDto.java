package fi.vm.sade.eperusteet.dto.yl.lukio.julkinen;

import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localizeLaterById;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LokalisoitavaOsaDto implements Serializable, Lokalisoitava {
    private LokalisoituTekstiDto otsikko;
    private LokalisoituTekstiDto teksti;

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Stream.of(otsikko, teksti);
    }

    public static LokalisoitavaOsaDto localizedLaterByIds(Long otsikkoId, Long tekstiId) {
        if (otsikkoId != null || tekstiId != null) {
            LokalisoitavaOsaDto dto = new LokalisoitavaOsaDto();
            dto.setOtsikko(localizeLaterById(otsikkoId));
            dto.setTeksti(localizeLaterById(tekstiId));
            return dto;
        }
        return null;
    }
}
