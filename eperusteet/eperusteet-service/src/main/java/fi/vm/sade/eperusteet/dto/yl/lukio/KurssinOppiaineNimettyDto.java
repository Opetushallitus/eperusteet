package fi.vm.sade.eperusteet.dto.yl.lukio;

import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localizeLaterById;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class KurssinOppiaineNimettyDto extends KurssinOppiaineDto implements Lokalisoitava {
    protected LokalisoituTekstiDto oppiaineNimi;

    public KurssinOppiaineNimettyDto(Long oppiaineId, Integer jarjestys, Long oppiaineNimiId) {
        super(oppiaineId, jarjestys);
        this.oppiaineNimi = localizeLaterById(oppiaineNimiId);
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Stream.of(oppiaineNimi);
    }
}
