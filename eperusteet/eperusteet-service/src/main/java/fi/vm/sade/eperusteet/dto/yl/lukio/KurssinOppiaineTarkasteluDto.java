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
public class KurssinOppiaineTarkasteluDto extends KurssinOppiaineNimettyDto {
    private OppiaineVanhempiDto vanhempi;
    private LokalisoituTekstiDto kurssiTyyppinKuvaus;

    public KurssinOppiaineTarkasteluDto(Long oppiaineId, Integer jarjestys, Long oppiaineNimiId,
                                        OppiaineVanhempiDto vanhempi, Long kurssiTyypinKuvausId) {
        super(oppiaineId, jarjestys, oppiaineNimiId);
        this.vanhempi = vanhempi;
        this.kurssiTyyppinKuvaus = localizeLaterById(kurssiTyypinKuvausId);
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Lokalisoitava.of(vanhempi).and(kurssiTyyppinKuvaus)
                .and(super.lokalisoitavatTekstit()).lokalisoitavatTekstit();
    }
}
